package com.keakimleang.bulkpayment.payloads.requests;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BulkPaymentUploadInfoRequest {
    private String sourceAccount;
    private String currency;
    private String remark;

    private LocalDateTime effectiveAt;
}
