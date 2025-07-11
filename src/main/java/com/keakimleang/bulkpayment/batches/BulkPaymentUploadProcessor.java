package com.keakimleang.bulkpayment.batches;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import com.keakimleang.bulkpayment.payloads.ProcessingStatus;
import com.keakimleang.bulkpayment.services.AuthServiceClient;
import com.keakimleang.bulkpayment.utils.CastObjectUtil;
import com.keakimleang.bulkpayment.utils.DateUtil;
import com.keakimleang.bulkpayment.utils.StringWrapperUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class BulkPaymentUploadProcessor implements ItemProcessor<BulkPaymentDataItem, Map<String, Object>>, StepExecutionListener {

    private final BulkPaymentUploadValidator validator;
    private final DSLContext create;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final AuthServiceClient client;

    private Long uploadedBulkPaymentId;
    private String uploadedBulkPaymentCurrency;

    private final AtomicInteger totalRecords = new AtomicInteger(1);
    private int validRecords;
    private int invalidRecords;

    @Override
    public Map<String, Object> process(@NonNull final BulkPaymentDataItem item) {
        log.info("Bulk Payment upload processing item: {}", item.getBeneficiaryAccount());
        final int rowNum = totalRecords.getAndIncrement();
        String errorMsg = validator.validateItem(item);
        boolean isValid = StringWrapperUtils.isBlank(errorMsg);

        client.getUserById(1L)
                .doOnNext(user -> log.info("User details: {}", user))
                .doOnError(error -> log.error("Error fetching user details", error))
                .subscribe();

        if (isValid) {
            validRecords++;
        } else {
            invalidRecords++;
        }

        // Account Name, Currency, should get from API
        final var now = DateUtil.now();
        final var map = new LinkedHashMap<String, Object>();
        map.put("bulk_payment_info_id", uploadedBulkPaymentId);
        map.put("beneficiary_account", item.getBeneficiaryAccount());
        map.put("beneficiary_name", StringWrapperUtils.random(6));
        map.put("amount", parseAmount(item.getAmount()));
        map.put("currency", uploadedBulkPaymentCurrency);
        map.put("fee", BigDecimal.ZERO);
        map.put("failure_reason", isValid ? null : errorMsg);
        map.put("sequence_number", rowNum);
        map.put("created_at", now);
        return map;
    }

    private BigDecimal parseAmount(final String amount) {
        return CastObjectUtil.getBigDecimal(amount, 2, RoundingMode.DOWN, null);
    }

    @Override
    public void beforeStep(final StepExecution stepExecution) {
        log.info("Processing step {}", stepExecution.getStepName());
        final var jobParams = stepExecution.getJobExecution().getJobParameters();
        uploadedBulkPaymentId = jobParams.getLong(BulkPaymentConstant.UPLOADED_BULK_PAYMENT_ID);
        uploadedBulkPaymentCurrency = jobParams.getString(BulkPaymentConstant.UPLOADED_BULK_PAYMENT_CURRENCY);
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        log.info("Bulk Payment upload completed");
        final var completedAt = stepExecution.getEndTime();
        final var total = stepExecution.getWriteCount();
        final var data = new LinkedHashMap<String, Object>();
        data.put("total_records", total);
        data.put("valid_records", validRecords);
        data.put("invalid_records", invalidRecords);
        data.put("status", ProcessingStatus.AWAIT_CONFIRM.name());
        data.put("completed_at", completedAt);
        create.update(DSL.table(BulkPaymentConstant.BULK_PAYMENT_INFO))
                .set(data)
                .where(DSL.field("id").eq(uploadedBulkPaymentId))
                .execute();
        return ExitStatus.COMPLETED;
    }

}
