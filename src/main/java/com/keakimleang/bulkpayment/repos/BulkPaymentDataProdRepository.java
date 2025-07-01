package com.keakimleang.bulkpayment.repos;

import com.keakimleang.bulkpayment.entities.BulkPaymentDataProd;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BulkPaymentDataProdRepository extends R2dbcRepository<BulkPaymentDataProd, Long> {

    @Modifying
    @Query("""
                INSERT INTO bulk_payment_data_prod (
                    bulk_payment_info_id,
                    beneficiary_account,
                    beneficiary_name,
                    amount,
                    fee,
                    status,
                    failure_reason,
                    created_at,
                    updated_at
                )
                SELECT
                    bulk_payment_info_id,
                    beneficiary_account,
                    beneficiary_name,
                    amount,
                    fee,
                    'CONFIRMED',
                    failure_reason,
                    created_at,
                    now()
                FROM bulk_payment_data_staging
                WHERE bulk_payment_info_id = :bulkPaymentInfoId
                  AND failure_reason IS NULL
            """)
    Mono<Long> moveValidRecordFromStagingToProd(Long bulkPaymentInfoId);

    Flux<BulkPaymentDataProd> findByBulkPaymentInfoId(Long bulkPaymentInfoId);
}
