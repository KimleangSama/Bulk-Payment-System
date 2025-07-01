package com.keakimleang.bulkpayment.entities;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(BulkPaymentConstant.BULK_PAYMENT_DATA_STAGING)
@Getter
@Setter
@ToString
public class BulkPaymentDataStaging {
    @Id
    private Long id;

    private Long bulkPaymentInfoId;

    private String beneficiaryAccount;
    private String beneficiaryName;
    private String currency;
    private String amount;
    private String fee;
    private String status;

    private String failureReason;
    private String sequenceNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
