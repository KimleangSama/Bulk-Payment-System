package com.keakimleang.bulkpayment.batches;

import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.*;
import lombok.extern.slf4j.*;
import org.springframework.batch.core.*;
import org.springframework.stereotype.*;

@Component
@Slf4j
public class BulkPaymentUploadJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        final var jobParams = jobExecution.getJobParameters();
        final var filePath = jobParams.getString(UPLOAD_FILE);
        final var batchUploadId = jobParams.getLong("UPLOAD_BATCH_ID");
        final var jobId = jobExecution.getJobId();
        log.info("JobId {} is starting for processing batchUploadId {} of with uploadFile {}",
                jobId, batchUploadId, filePath);
    }
}
