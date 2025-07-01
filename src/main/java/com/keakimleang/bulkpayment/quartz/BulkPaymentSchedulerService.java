package com.keakimleang.bulkpayment.quartz;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkPaymentSchedulerService {
    private final Scheduler scheduler;

    public void schedulePaymentProcessing(Long bulkPaymentInfoId, LocalDateTime effectiveAt) {
        try {
            String jobName = "bulkPayment-" + bulkPaymentInfoId;
            String groupName = "bulk-payments";

            JobDetail jobDetail = JobBuilder.newJob(BulkPaymentSchedulingJob.class)
                    .withIdentity(jobName, groupName)
                    .usingJobData("bulkPaymentInfoId", bulkPaymentInfoId)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "-trigger", groupName)
                    .startAt(Date.from(effectiveAt.atZone(ZoneId.systemDefault()).toInstant()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Scheduled bulk payment processing job for ID {} at {}", bulkPaymentInfoId, effectiveAt);

        } catch (SchedulerException e) {
            log.error("Failed to schedule bulk payment job for ID {}: {}", bulkPaymentInfoId, e.getMessage());
            throw new RuntimeException("Failed to schedule payment processing", e);
        }
    }

    public void cancelScheduledPayment(Long bulkPaymentInfoId) {
        try {
            String jobName = "bulkPayment-" + bulkPaymentInfoId;
            String groupName = "bulk-payments";

            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Cancelled scheduled job for bulk payment ID: {}", bulkPaymentInfoId);
            }
        } catch (SchedulerException e) {
            log.error("Failed to cancel scheduled job for ID {}: {}", bulkPaymentInfoId, e.getMessage());
        }
    }
}

