package com.keakimleang.bulkpayment.entities;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(BulkPaymentConstant.BULK_PAYMENT_DATA_PROD)
@Getter
@Setter
@ToString
public class BulkPaymentDataProd {
    @Id
    private Long id;

    private Long bulkPaymentInfoId;

    private String beneficiaryAccount;
    private String beneficiaryName;
    private BigDecimal amount;
    private BigDecimal fee;
    private String status;

    private String failureReason;
    private String transactionId;
    private String paymentReference;

    private LocalDateTime executedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
