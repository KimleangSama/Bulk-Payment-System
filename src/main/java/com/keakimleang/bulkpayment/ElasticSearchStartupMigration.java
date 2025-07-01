package com.keakimleang.bulkpayment;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import com.keakimleang.bulkpayment.entities.BulkPaymentDataProd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class ElasticSearchStartupMigration implements CommandLineRunner {
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Override
    public void run(String... args) {
        try {
            final var indexOps = reactiveElasticsearchOperations.indexOps(IndexCoordinates.of(BulkPaymentConstant.BULK_PAYMENT_DATA_PROD));
            indexOps.exists()
                    .flatMap(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            log.info("Index already exists");
                            return Mono.empty();
                        } else {
                            log.info("Indexing uploaded files");
                            return indexOps.create().then(indexOps.putMapping(BulkPaymentDataProd.class));
                        }
                    })
                    .subscribe();
        } catch (Exception exception) {
            log.error("Elastic Service: {}", exception.getMessage());
        }
    }
}
