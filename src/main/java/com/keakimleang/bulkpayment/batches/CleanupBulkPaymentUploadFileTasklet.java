package com.keakimleang.bulkpayment.batches;

import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.RUN_ASYNC_FLOW;
import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.UPLOAD_FILE;
import com.keakimleang.bulkpayment.utils.StringWrapperUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
