package com.keakimleang.bulkpayment.batches;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.extensions.excel.poi.PoiItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BulkPaymentUploadConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    @Bean
    @StepScope
    public FlatFileItemReader<BulkPaymentDataItem> csvBatchUploadReader(@Value("file:#{jobParameters['uploadFile']}") final Resource resource) {
        final var lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");

        lineTokenizer.setNames(BulkPaymentConstant.BATCH_UPLOAD_FIELD_NAMES);

        final var fieldSet = new BeanWrapperFieldSetMapper<BulkPaymentDataItem>();
        fieldSet.setTargetType(BulkPaymentDataItem.class);

        return new FlatFileItemReaderBuilder<BulkPaymentDataItem>()
                .name("csvBatchUploadReader")
                .resource(resource)
                .linesToSkip(1)
                .lineTokenizer(lineTokenizer)
                .fieldSetMapper(fieldSet)
                .build();
    }

    @Bean
    @StepScope
    public PoiItemReader<BulkPaymentDataItem> excelBatchUploadReader(@Value("file:#{jobParameters['uploadFile']}") final Resource resource,
                                                                     final ExcelBulkPaymentUploadMapper excelBulkPaymentUploadMapper) {
        final var poiItemReader = new PoiItemReader<BulkPaymentDataItem>();
        poiItemReader.setName("excelBatchUploadReader");
        poiItemReader.setLinesToSkip(1);
        poiItemReader.setResource(resource);
        poiItemReader.setRowMapper(excelBulkPaymentUploadMapper);
        return poiItemReader;
    }

    @Bean
    @StepScope
    public BulkPaymentUploadWriter batchUploadWriter(final DSLContext create) {
        return new BulkPaymentUploadWriter(create, BulkPaymentConstant.BULK_PAYMENT_DATA_STAGING);
    }

    @Bean
    public Step importExcelToDbStep(final PoiItemReader<BulkPaymentDataItem> excelBatchUploadReader,
                                    final BulkPaymentUploadProcessor bulkPaymentUploadProcessor,
                                    final BulkPaymentUploadWriter bulkPaymentUploadWriter) {
        return new StepBuilder("importExcelToDbStep", jobRepository)
                .<BulkPaymentDataItem, Map<String, Object>>chunk(10, platformTransactionManager)
                .reader(excelBatchUploadReader)
                .processor(bulkPaymentUploadProcessor)
                .writer(bulkPaymentUploadWriter)
                .build();
    }

    @Bean
    public Step importCsvToDbStep(final FlatFileItemReader<BulkPaymentDataItem> csvBatchUploadReader,
                                  final BulkPaymentUploadProcessor bulkPaymentUploadProcessor,
                                  final BulkPaymentUploadWriter bulkPaymentUploadWriter) {
        return new StepBuilder("importCsvToDbStep", jobRepository)
                .<BulkPaymentDataItem, Map<String, Object>>chunk(10, platformTransactionManager)
                .reader(csvBatchUploadReader)
                .processor(bulkPaymentUploadProcessor)
                .writer(bulkPaymentUploadWriter)
                .build();
    }


    @Bean
    public Step cleanupResourceStep(final CleanupBulkPaymentUploadFileTasklet cleanupBulkPaymentUploadFileTasklet) {
        return new StepBuilder("cleanupResourceStep", jobRepository)
                .tasklet(cleanupBulkPaymentUploadFileTasklet, platformTransactionManager)
                .build();
    }

    @Bean
    public Flow uploadExcelFlow(final Step importExcelToDbStep) {
        return new FlowBuilder<SimpleFlow>("uploadExcelFlow")
                .start(importExcelToDbStep)
                .on("COMPLETED")
                .end()
                .build();
    }

    @Bean
    public Flow uploadCsvFlow(final Step importCsvToDbStep) {
        return new FlowBuilder<SimpleFlow>("uploadCsvFlow")
                .start(importCsvToDbStep)
                .on("COMPLETED")
                .end()
                .build();
    }

    @Bean
    public Job uploadedBulkPaymentJob(final BulkPaymentUploadJobDecider bulkPaymentUploadJobDecider,
                                      final Flow uploadExcelFlow,
                                      final Flow uploadCsvFlow,
                                      final Step cleanupResourceStep,
                                      final JobExecutionListener batchUploadJobExecutionListener) {
        return new JobBuilder("uploadedBulkPaymentJob", jobRepository)
                .listener(batchUploadJobExecutionListener)

                // Start with the decider to determine the next step
                .start(bulkPaymentUploadJobDecider)

                // If the decider returns a specific value, execute the Excel import step
                .from(bulkPaymentUploadJobDecider)
                .on("START_UPLOAD_EXCEL_STEP")
                .to(uploadExcelFlow)

                // If the decider returns a different value, execute the CSV import step
                .from(bulkPaymentUploadJobDecider)
                .on("START_UPLOAD_CSV_STEP")
                .to(uploadCsvFlow)

                // Always cleanup resource and determine job success or failed based on previous step
                .from(uploadExcelFlow).on("*").to(cleanupResourceStep)
                .from(uploadCsvFlow).on("*").to(cleanupResourceStep)

                .end()
                .build();
    }
}
