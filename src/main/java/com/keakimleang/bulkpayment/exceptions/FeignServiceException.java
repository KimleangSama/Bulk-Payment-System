package com.keakimleang.bulkpayment.exceptions;

import lombok.Getter;

@Getter
public class FeignServiceException extends RuntimeException {
    private final int statusCode;
    private final ErrorCode errorCode;

    public FeignServiceException(final int statusCode,
                                 final String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = ErrorCode.FEIGN_SERVICE_ERROR;
    }

    public FeignServiceException(final int statusCode,
                                 final ErrorCode errorCode,
                                 final String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}
