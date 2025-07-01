package com.keakimleang.bulkpayment.entities;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import com.keakimleang.bulkpayment.payloads.ProcessingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(BulkPaymentConstant.BULK_PAYMENT_INFO)
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class BulkPaymentInfo {
    @Id
    private Long id;

    private Integer totalRecords;
    private Integer validRecords;
    private Integer invalidRecords;

    private BigDecimal totalAmount;
    private BigDecimal totalFee;

    private String sourceAccount;
    private String currency;
    private ProcessingStatus status;
    private String remark;

    private LocalDateTime effectiveAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
}
