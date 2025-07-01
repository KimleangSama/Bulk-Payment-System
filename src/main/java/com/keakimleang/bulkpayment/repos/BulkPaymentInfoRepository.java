package com.keakimleang.bulkpayment.repos;

import com.keakimleang.bulkpayment.entities.BulkPaymentInfo;
import com.keakimleang.bulkpayment.payloads.ProcessingStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BulkPaymentInfoRepository extends R2dbcRepository<BulkPaymentInfo, Long> {

    Mono<BulkPaymentInfo> findByIdAndStatus(Long id, ProcessingStatus status);
}
