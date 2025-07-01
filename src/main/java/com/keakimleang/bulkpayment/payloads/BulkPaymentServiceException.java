package com.keakimleang.bulkpayment.payloads;

public class BulkPaymentServiceException extends RuntimeException {

    public BulkPaymentServiceException() {
    }

    public BulkPaymentServiceException(final String message) {
        super(message);
    }

    public BulkPaymentServiceException(final String message,
                                       final Throwable cause) {
        super(message, cause);
    }
}
