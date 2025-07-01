package com.keakimleang.bulkpayment.batches;

import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.*;
import com.keakimleang.bulkpayment.utils.*;
import java.nio.file.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.*;
import org.springframework.batch.core.step.tasklet.*;
import org.springframework.batch.repeat.*;
import org.springframework.lang.*;
import org.springframework.stereotype.*;

@Component
@Slf4j
public class CleanupBulkPaymentUploadFileTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(final StepContribution contribution,
                                @NonNull final ChunkContext chunkContext) throws Exception {
        final var jobParams = contribution.getStepExecution().getJobParameters();
        final var filePath = jobParams.getString(UPLOAD_FILE);
        final var runAsyncFlow = jobParams.getString(RUN_ASYNC_FLOW);

        // Handle status for job depend on previous step
        final var currentStep = contribution.getStepExecution();
        final var jobExecution = contribution.getStepExecution().getJobExecution();
        final var previousExitStatus = jobExecution.getStepExecutions()
                .stream()
                .filter(step -> !Objects.equals(currentStep.getStepName(), step.getStepName()))
                .reduce((first, second) -> second)
                .map(StepExecution::getExitStatus)
                .orElse(new ExitStatus("UNKNOWN"));
        currentStep.setExitStatus(previousExitStatus);

        final var jobId = contribution.getStepExecution().getJobExecutionId();
        assert filePath != null;
        final var uploadedFilePath = Path.of(filePath);
        if (Boolean.parseBoolean(runAsyncFlow) && ExitStatus.COMPLETED.equals(previousExitStatus)) {
            if (StringWrapperUtils.isNotBlank(filePath)) {
                Files.deleteIfExists(uploadedFilePath);
            }

            log.info("Cleanup resource for jobId={}. resource file={} with runAsyncFlow=true and exitStatus={}",
                    jobId, filePath, previousExitStatus.getExitCode());
        } else {
            if (StringWrapperUtils.isNotBlank(filePath)) {
                Files.deleteIfExists(uploadedFilePath);
            }

            log.info("Cleanup resource for jobId={}. resource file={} with runAsyncFlow=false and exitStatus={}",
                    jobId, filePath, previousExitStatus.getExitCode());
        }

        return RepeatStatus.FINISHED;
    }
}
