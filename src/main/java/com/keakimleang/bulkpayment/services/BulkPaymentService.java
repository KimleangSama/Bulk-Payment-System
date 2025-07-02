package com.keakimleang.bulkpayment.services;

import com.keakimleang.bulkpayment.annotations.ReactiveTransaction;
import com.keakimleang.bulkpayment.batches.BulkPaymentUploadValidator;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.RUN_ASYNC_FLOW;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOADED_BULK_PAYMENT_CURRENCY;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOADED_BULK_PAYMENT_ID;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOADED_BULK_PAYMENT_SOURCE_ACCOUNT;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOAD_FILE;
import com.keakimleang.bulkpayment.entities.BulkPaymentInfo;
import com.keakimleang.bulkpayment.payloads.BulkPaymentJobFailedException;
import com.keakimleang.bulkpayment.payloads.BulkPaymentServiceException;
import com.keakimleang.bulkpayment.payloads.BulkPaymentUploadRequest;
import com.keakimleang.bulkpayment.payloads.ProcessingStatus;
import com.keakimleang.bulkpayment.payloads.requests.BulkPaymentUploadInfoRequest;
import com.keakimleang.bulkpayment.quartz.BulkPaymentRabbitMQService;
import com.keakimleang.bulkpayment.quartz.BulkPaymentSchedulerService;
import com.keakimleang.bulkpayment.repos.BulkPaymentDataProdRepository;
import com.keakimleang.bulkpayment.repos.BulkPaymentDataStagingRepository;
import com.keakimleang.bulkpayment.repos.BulkPaymentInfoRepository;
import com.keakimleang.bulkpayment.utils.AppUtil;
import com.keakimleang.bulkpayment.utils.DateUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Service
@Slf4j
public class BulkPaymentService {
    private final BulkPaymentUploadValidator bulkPaymentUploadValidator;
    private final BulkPaymentInfoRepository bulkPaymentInfoRepository;
    private final BulkPaymentDataStagingRepository bulkPaymentStagingRepository;
    private final BulkPaymentDataProdRepository bulkPaymentDataProdRepository;

    private final JobLauncher jobLauncher;
    private final Job uploadBatchJob;

    private final BulkPaymentRabbitMQService bulkPaymentRabbitMQService;
    private final BulkPaymentSchedulerService bulkPaymentSchedulerService;

    public Mono<Long> upload(final Mono<BulkPaymentUploadRequest> requestMono) {
        return requestMono
                .flatMap(bulkPaymentUploadValidator::validateFile)
                .flatMap(request -> saveUploadToTempDir(request)
                        .map(temp -> Tuples.of(request, temp)))
                .flatMap(tuple2 -> Mono.zip(Mono.just(tuple2.getT2()), saveToBulkUploadInfo(tuple2.getT1()), Mono.just(tuple2.getT1())))
                .flatMap(tuple3 -> {
                    final var runAsync = tuple3.getT3().runJobAsync();
                    if (runAsync) {
                        // Run the job in the background and immediately return the upload ID
                        Mono.defer(() -> processBatchUploadJob(tuple3, true))
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe(null, e ->
                                        log.error("Error processing batch upload job asynchronously: {}", e.getMessage(), e));
                        // Return the upload ID immediately without waiting for job completion
                        return Mono.just(tuple3.getT2().getId());
                    } else {
                        return processBatchUploadJob(tuple3, false);
                    }
                });
    }

    private Mono<Long> processBatchUploadJob(final Tuple2<Path, BulkPaymentInfo> tuple2,
                                             final boolean runJobAsync) {
        return Mono.fromCallable(() -> {
                    final var jobParams = new JobParametersBuilder()
                            .addLong(UPLOADED_BULK_PAYMENT_ID, tuple2.getT2().getId())
                            .addString(UPLOADED_BULK_PAYMENT_SOURCE_ACCOUNT, tuple2.getT2().getSourceAccount())
                            .addString(UPLOADED_BULK_PAYMENT_CURRENCY, tuple2.getT2().getCurrency())
                            .addString(UPLOAD_FILE, tuple2.getT1().toAbsolutePath().toString())
                            .addString(RUN_ASYNC_FLOW, String.valueOf(runJobAsync))
                            .toJobParameters();
                    final var job = jobLauncher.run(uploadBatchJob, jobParams);
                    if (!BatchStatus.COMPLETED.equals(job.getStatus())) {
                        log.info("batchUploadId {} process by jobId {} failed with status={} and existStatus={}",
                                tuple2.getT2().getId(), job.getJobId(), job.getStatus(), job.getExitStatus().getExitCode());
                        final var msg = "Fail to upload batcher due to " + job.getAllFailureExceptions().getFirst().getMessage();
                        throw new BulkPaymentJobFailedException(tuple2.getT2().getId(), msg);
                    }
                    return tuple2.getT2().getId();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    // Keep record for user confirm if run async
                    if (runJobAsync) {
                        return Mono.error(e);
                    } else {
                        // Rollback previous save record
                        final var jobError = (BulkPaymentJobFailedException) e;
                        final var rollbackStaging = bulkPaymentStagingRepository.deleteByBatchUploadId(jobError.getBulkPaymentInfoId());
                        final var rollBackBatchUpload = bulkPaymentInfoRepository.deleteById(jobError.getBulkPaymentInfoId());
                        return Mono.when(rollbackStaging, rollBackBatchUpload)
                                .then(Mono.error(e));
                    }
                });
    }

    private Mono<Path> saveUploadToTempDir(final BulkPaymentUploadRequest request) {
        return Mono.fromCallable(() -> {
                    final var filePart = request.file();
                    final var ext = "." + AppUtil.rsplit(filePart.filename(), ".", 2).getLast();
                    return Files.createTempFile("batch", ext);
                })
                .subscribeOn(Schedulers.boundedElastic()) // Run blocking call on a bounded elastic thread
                .flatMap(temp -> request.file().transferTo(temp).thenReturn(temp))
                .onErrorMap(IOException.class, RuntimeException::new); // Wrap IOException into unchecked
    }

    private Mono<BulkPaymentInfo> saveToBulkUploadInfo(final BulkPaymentUploadRequest request) {
        return Mono.defer(() -> {
            final LocalDateTime now = DateUtil.now();
            final BulkPaymentUploadInfoRequest info = request.request();
            BulkPaymentInfo batchUpload = new BulkPaymentInfo()
                    .setCreatedAt(now)
                    .setUpdatedAt(now)
                    .setStatus(ProcessingStatus.AWAIT_CONFIRM)
                    .setSourceAccount(info.getSourceAccount())
                    .setCurrency(info.getCurrency())
                    .setRemark(info.getRemark())
                    .setEffectiveAt(info.getEffectiveAt())
                    .setTotalRecords(0)
                    .setValidRecords(0)
                    .setInvalidRecords(0)
                    .setTotalAmount(BigDecimal.ZERO)
                    .setTotalFee(BigDecimal.ZERO);
            return bulkPaymentInfoRepository.save(batchUpload);
        });
    }

    @ReactiveTransaction
    public Mono<Long> confirm(Long bulkPaymentInfoId) {
        return bulkPaymentInfoRepository.findById(bulkPaymentInfoId)
                .switchIfEmpty(Mono.error(new BulkPaymentServiceException(
                        "Bulk payment info not found with Id: " + bulkPaymentInfoId)))
                .flatMap(bulkPaymentInfo -> validateStatus(bulkPaymentInfo, bulkPaymentInfoId)
                        .then(moveRecords(bulkPaymentInfoId))
                        .flatMap(movedCount -> updateBulkPaymentInfo(bulkPaymentInfo, movedCount))
                        .flatMap(savedInfo -> scheduleOrSendImmediatelyForProcessing(savedInfo.getId(), savedInfo))
                );
    }

    private Mono<Void> validateStatus(BulkPaymentInfo info, Long id) {
        if (!ProcessingStatus.AWAIT_CONFIRM.equals(info.getStatus())) {
            return Mono.error(new BulkPaymentServiceException(
                    "Bulk payment info with id: " + id +
                            " is not in a valid state for confirmation. Current status: " + info.getStatus()));
        }
        return Mono.empty();
    }

    private final DatabaseClient databaseClient;

    private Mono<Long> moveRecords(Long bulkPaymentInfoId) {
        String sql = """
                    INSERT INTO bulk_payment_data_prod (
                        bulk_payment_info_id,
                        beneficiary_account,
                        beneficiary_name,
                        amount,
                        fee,
                        status,
                        failure_reason,
                        created_at,
                        updated_at
                    )
                    SELECT
                        bulk_payment_info_id,
                        beneficiary_account,
                        beneficiary_name,
                        amount,
                        fee,
                        'CONFIRMED',
                        failure_reason,
                        created_at,
                        now()
                    FROM bulk_payment_data_staging
                    WHERE bulk_payment_info_id = :bulkPaymentInfoId
                      AND failure_reason IS NULL
                """;

//        return bulkPaymentDataProdRepository.moveValidRecordFromStagingToProd(bulkPaymentInfoId)
//                .subscribeOn(Schedulers.boundedElastic())
//                .flatMap(movedCount -> {
//                    if (movedCount <= 0) {
//                        return Mono.error(new BulkPaymentServiceException(
//                                "No valid records found to move for bulk payment info ID: " + bulkPaymentInfoId));
//                    }
//                    log.info("Moved {} valid records from staging to production for bulk payment info ID: {}", movedCount, bulkPaymentInfoId);
//                    return Mono.just(movedCount);
//                });
        return databaseClient.sql(sql)
                .bind("bulkPaymentInfoId", bulkPaymentInfoId)
                .fetch()
                .rowsUpdated();
    }

    private Mono<BulkPaymentInfo> updateBulkPaymentInfo(BulkPaymentInfo info, Long movedCount) {
        boolean isScheduled = isScheduled(info.getEffectiveAt());

        info.setStatus(isScheduled ? ProcessingStatus.SCHEDULED : ProcessingStatus.PROCESSING);
        info.setTotalRecords(movedCount.intValue());
        info.setValidRecords(movedCount.intValue());
        info.setInvalidRecords(0);
        info.setSubmittedAt(DateUtil.now());

        return bulkPaymentStagingRepository.deleteByBatchUploadId(info.getId())
                .then(bulkPaymentInfoRepository.save(info))
                .doOnSuccess(saved -> log.info("BulkPaymentInfo ID {} processed successfully", saved.getId()));
    }

    private boolean isScheduled(LocalDateTime effectiveAt) {
        return effectiveAt != null && effectiveAt.isAfter(LocalDateTime.now());
    }

    private Mono<Long> scheduleOrSendImmediatelyForProcessing(Long savedId, BulkPaymentInfo info) {
        if (isScheduled(info.getEffectiveAt())) {
            bulkPaymentSchedulerService.schedulePaymentProcessing(savedId, info.getEffectiveAt());
            log.info("Scheduled bulk payment ID {} for processing at {}", savedId, info.getEffectiveAt());
            return Mono.just(savedId);
        } else {
            return bulkPaymentDataProdRepository.findByBulkPaymentInfoId(savedId)
                    .collectList()
                    .flatMap(records -> {
                        log.info("Sent bulk payment ID {} to RabbitMQ for immediate processing", savedId);
                        return bulkPaymentRabbitMQService.sendBulkPaymentForProcessing(savedId, records);
                    })
                    .thenReturn(savedId);
        }
    }

//    @ReactiveTransaction(readOnly = true)
//    public Flux<BatchUploadProd> getBatchRecordsByBatchUploadId(Long batchUploadId) {
//        return redisCacheService.getListValue("batchUploadId:" + batchUploadId)
//                .map(obj -> objectMapper.convertValue(obj, BatchUploadProd.class))
//                .switchIfEmpty(bulkPaymentDataProdRepository.findByBatchUploadId(batchUploadId)
//                        .doOnNext(record -> log.info("Cache miss for bulk payment info ID {}, fetching from database", batchUploadId)))
//                .cast(BatchUploadProd.class)
//                .doOnError(e -> log.error("Error retrieving records for bulk payment info ID {}: {}", batchUploadId, e.getMessage()));
//    }
//
//    @ReactiveTransaction(readOnly = true)
//    public Flux<BatchUploadProd> getBatchRecordsByFilter(BatchUploadRecordFilterRequest request) {
//        String sql = """
//                    SELECT bp.*
//                    FROM batches_uploads_prod bp
//                    JOIN batches_uploads bu ON bp.batch_upload_id = bu.id
//                    WHERE (:batchUploadId IS NULL OR bp.batch_upload_id = :batchUploadId)
//                      AND (:batchOwnerName IS NULL OR bu.batch_owner_name = :batchOwnerName)
//                """;
//        return databaseClient.sql(sql)
//                .bind("batchUploadId", request.getBatchUploadId())
//                .bind("batchOwnerName", request.getBatchOwnerName())
//                .map((row, metadata) -> new BatchUploadProd(
//                        row.get("id", Long.class),
//                        row.get("batch_upload_id", Long.class),
//                        row.get("customer_code", String.class),
//                        row.get("invoice_date", LocalDate.class),
//                        row.get("due_amount", BigDecimal.class),
//                        row.get("currency", String.class),
//                        row.get("created_at", LocalDateTime.class)
//                ))
//                .all()
//                .doOnNext(record -> log.info("Fetched joined record: {}", record));
//    }

//    @ReactiveTransaction(readOnly = true)
//    public Flux<BatchUploadProd> getBatchRecordsByFilter(BatchUploadRecordFilterRequest request) {
//        final var filter = new BatchUploadRecordFilter();
//        filter.setBatchUploadId(request.getBatchUploadId());
//        filter.setBatchOwnerName(request.getBatchOwnerName());
//        return Flux.defer(() -> {
//            final var criteria = filter.getCriteria();
//            final Query query = Query.query(criteria);
//            return template.select(query, BatchUploadProd.class)
//                    .doOnNext(record -> log.info("Fetched record: {}", record));
//        });
//    }

//    public Mono<FileDownload> downloadExcelBatchRecords(final Long id) {
//        final var template = new ClassPathResource("/templates/batch-record-template.xlsx");
//
//        return bulkPaymentDataProdRepository.findByBatchUploadId(id)
//                .collectList()
//                .flatMap(records -> Mono.fromCallable(() -> {
//                    // Convert to row data
//                    List<BatchUploadProdElasticsearch> batchUploadProds = new ArrayList<>();
//                    for (final BatchUploadProd record : records) {
//                        batchUploadProds.add(new BatchUploadProdElasticsearch(record));
//                    }
//                    final var objectMapper = new ObjectMapper();
//                    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//                    final var rows = objectMapper.convertValue(batchUploadProds, new TypeReference<List<Map<String, Object>>>() {
//                    });
//                    final var params = generateExcelParam(template, rows);
//
//                    // Generate Excel (blocking)
//                    final var output = ExportExcelHelper.generateExcelWithTemplate(params);
//
//                    // Build headers
//                    final var headers = new HttpHeaders();
//                    headers.add(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
//                            .filename("batch_records_%s_%s.xlsx".formatted(id + "", LocalDate.now()))
//                            .build()
//                            .toString());
//                    headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
//                    headers.setContentType(MediaType.parseMediaType(MediaTypeConstant.EXCEL));
//
//                    // Return FileDownload response
//                    return new FileDownload(
//                            new InputStreamResource(new ByteArrayInputStream(output.toByteArray())),
//                            headers
//                    );
//                }).subscribeOn(Schedulers.boundedElastic()));
//    }
//
//    private static GenerateExcelParam generateExcelParam(ClassPathResource template, List<Map<String, Object>> rows) {
//        final var params = new GenerateExcelParam();
//        params.setTemplateResource(template);
//        params.setOrderedColumnName(ORDERED_COLUMN);
//        params.setRows(rows);
//        params.setStartRowDataAt(4);
//        params.setCopyFirstRowCellStyle(true);
//        return params;
//    }

}
