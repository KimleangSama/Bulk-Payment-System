package com.keakimleang.bulkpayment.batches;

import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.START_UPLOAD_CSV_STEP;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.START_UPLOAD_EXCEL_STEP;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOAD_FILE;
import com.keakimleang.bulkpayment.utils.StringWrapperUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BulkPaymentUploadJobDecider implements JobExecutionDecider {

    @NonNull
    @Override
    public FlowExecutionStatus decide(final JobExecution jobExecution,
                                      final StepExecution stepExecution) {
        final var fileName = jobExecution.getJobParameters()
                .getString(UPLOAD_FILE);
        if (StringWrapperUtils.endsWithIgnoreCase(fileName, ".xlsx")) {
            return new FlowExecutionStatus(START_UPLOAD_EXCEL_STEP);
        }
        if (StringWrapperUtils.endsWithIgnoreCase(fileName, ".csv")) {
            return new FlowExecutionStatus(START_UPLOAD_CSV_STEP);
        }
        return FlowExecutionStatus.FAILED;
    }
}
