package com.keakimleang.bulkpayment.payloads;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class ApiValidationException extends BulkPaymentServiceException {
    private final List<ApiError> errors;

    public ApiValidationException(final ApiError error) {
        this.errors = Collections.singletonList(error);
    }
}
