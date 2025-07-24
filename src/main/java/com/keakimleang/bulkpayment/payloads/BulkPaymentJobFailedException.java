package com.keakimleang.bulkpayment.payloads;

import lombok.Getter;

@Getter
public class BulkPaymentJobFailedException extends BulkPaymentServiceException {
    private final ErrorCode errorCode = ErrorCode.BATCH_JOB_ERROR;
    private final Long bulkPaymentInfoId;

    public BulkPaymentJobFailedException(final Long bulkPaymentInfoId,
                                         final String message) {
        super(message);
        this.bulkPaymentInfoId = bulkPaymentInfoId;
    }
}
