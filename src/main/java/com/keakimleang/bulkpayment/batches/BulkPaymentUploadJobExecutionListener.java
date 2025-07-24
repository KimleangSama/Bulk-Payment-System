package com.keakimleang.bulkpayment.batches;

import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOADED_BULK_PAYMENT_ID;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOAD_FILE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BulkPaymentUploadJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        final var jobParams = jobExecution.getJobParameters();
        final var filePath = jobParams.getString(UPLOAD_FILE);
        final var bulkPaymentInfoId = jobParams.getLong(UPLOADED_BULK_PAYMENT_ID);
        final var jobId = jobExecution.getJobId();
        log.info("JobId {} is starting for processing bulkPaymentInfoId {} of with uploadFile {}",
                jobId, bulkPaymentInfoId, filePath);
    }
}
