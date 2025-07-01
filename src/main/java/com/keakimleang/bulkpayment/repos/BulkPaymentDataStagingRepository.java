package com.keakimleang.bulkpayment.repos;

import com.keakimleang.bulkpayment.entities.BulkPaymentDataStaging;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BulkPaymentDataStagingRepository extends R2dbcRepository<BulkPaymentDataStaging, Long> {

    @Query("DELETE FROM bulk_payment_data_staging WHERE bulk_payment_info_id = :bulkPaymentInfoId")
    Mono<Long> deleteByBatchUploadId(Long bulkPaymentInfoId);

}
