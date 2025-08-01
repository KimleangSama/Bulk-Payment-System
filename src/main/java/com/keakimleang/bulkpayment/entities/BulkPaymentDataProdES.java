package com.keakimleang.bulkpayment.entities;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = BulkPaymentConstant.BULK_PAYMENT_DATA_PROD)
@Getter
@Setter
@ToString
public class BulkPaymentDataProdES {
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

    public BulkPaymentDataProdES(BulkPaymentDataProd bulkPaymentDataProd) {
        this.id = bulkPaymentDataProd.getId();
        this.bulkPaymentInfoId = bulkPaymentDataProd.getBulkPaymentInfoId();
        this.beneficiaryAccount = bulkPaymentDataProd.getBeneficiaryAccount();
        this.beneficiaryName = bulkPaymentDataProd.getBeneficiaryName();
        this.amount = bulkPaymentDataProd.getAmount();
        this.fee = bulkPaymentDataProd.getFee();
        this.status = bulkPaymentDataProd.getStatus();
        this.failureReason = bulkPaymentDataProd.getFailureReason();
        this.transactionId = bulkPaymentDataProd.getTransactionId();
        this.paymentReference = bulkPaymentDataProd.getPaymentReference();
        this.executedAt = bulkPaymentDataProd.getExecutedAt();
        this.createdAt = bulkPaymentDataProd.getCreatedAt();
        this.updatedAt = bulkPaymentDataProd.getUpdatedAt();
    }
}
