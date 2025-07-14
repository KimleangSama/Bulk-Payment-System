package com.keakimleang.bulkpayment.controllers;

import com.keakimleang.bulkpayment.payloads.ApiResponse;
import com.keakimleang.bulkpayment.payloads.BulkPaymentUploadRequest;
import com.keakimleang.bulkpayment.payloads.requests.BulkPaymentUploadInfoRequest;
import com.keakimleang.bulkpayment.services.BulkPaymentService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/bulk-payments")
@RequiredArgsConstructor
@Slf4j
public class BulkPaymentController {
    private final BulkPaymentService bulkPaymentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<ApiResponse<Map<String, Long>>>> upload(
            @RequestPart("file") final Mono<FilePart> filePartMono,
            @RequestPart("sourceAccount") final Mono<String> sourceAccountMono,
            @RequestPart("currency") final Mono<String> currencyMono,
            @RequestPart("remark") final Mono<String> remarkMono,
            @RequestPart("effectiveAt") final Mono<String> effectiveAtMono
    ) {
        Mono<BulkPaymentUploadInfoRequest> requestMono = Mono.zip(
                sourceAccountMono,
                currencyMono,
                remarkMono,
                effectiveAtMono
        ).map(tuple -> {
            BulkPaymentUploadInfoRequest request = new BulkPaymentUploadInfoRequest();
            request.setSourceAccount(tuple.getT1());
            request.setCurrency(tuple.getT2());
            request.setRemark(tuple.getT3());
            request.setEffectiveAt(LocalDateTime.parse(tuple.getT4()));
            return request;
        });

        return bulkPaymentService
                .upload(Mono.zip(filePartMono, requestMono)
                        .map(tuple -> new BulkPaymentUploadRequest(tuple.getT1(), tuple.getT2(), false)))
                .map(batchId -> ResponseEntity.ok(ApiResponse.cratedResourceResponse(batchId, null)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(value = "{bulkPaymentInfoId}/confirm")
    public Mono<ResponseEntity<ApiResponse<Map<String, Long>>>> confirm(@PathVariable("bulkPaymentInfoId") final Long bulkPaymentInfoId) {
        return bulkPaymentService.confirm(bulkPaymentInfoId)
                .map(data -> ResponseEntity.ok(ApiResponse.cratedResourceResponse(data, "Bulk payment confirmed successfully!")));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<String> getAllBulkPaymentInfo() {
        return Mono.just("Bulk Payment Info for Admin");
    }

//    @GetMapping(value = "/{batchUploadId}/excel/export", produces = MediaTypeConstant.EXCEL)
//    public Mono<ResponseEntity<Resource>> exportToExcel(@PathVariable("batchUploadId") final Long batchUploadId) {
//        return bulkPaymentService.downloadExcelBatchRecords(batchUploadId)
//                .map(fileDownload -> ResponseEntity.ok()
//                        .headers(fileDownload.headers())
//                        .body(fileDownload.file()));
//    }
}
