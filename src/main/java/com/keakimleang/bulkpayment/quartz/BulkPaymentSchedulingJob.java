package com.keakimleang.bulkpayment.quartz;

import com.keakimleang.bulkpayment.repos.BulkPaymentDataProdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkPaymentSchedulingJob implements Job {
    private final BulkPaymentDataProdRepository bulkPaymentDataProdRepository;
    private final BulkPaymentRabbitMQService bulkPaymentRabbitMQService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long bulkPaymentInfoId = dataMap.getLong("bulkPaymentInfoId");

        log.info("Executing bulk payment processing job for ID: {}", bulkPaymentInfoId);

        try {
            // Fetch records from production table
            bulkPaymentDataProdRepository.findByBulkPaymentInfoId(bulkPaymentInfoId)
                    .collectList()
                    .flatMap(records -> {
                        log.info("Found {} records to process for bulk payment ID: {}", records.size(), bulkPaymentInfoId);
                        return bulkPaymentRabbitMQService.sendBulkPaymentForProcessing(bulkPaymentInfoId, records);
                    })
                    .doOnSuccess(v -> log.info("Successfully sent records to RabbitMQ for bulk payment ID {}", bulkPaymentInfoId))
                    .doOnError(e -> log.error("Error processing bulk payment ID {}", bulkPaymentInfoId, e))
                    .block();
        } catch (Exception e) {
            log.error("Failed to execute bulk payment job for ID {}: {}", bulkPaymentInfoId, e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
