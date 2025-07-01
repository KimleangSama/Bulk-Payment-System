package com.keakimleang.bulkpayment.payloads;

import java.util.*;
import lombok.*;

@Getter
public class ApiValidationException extends BulkPaymentServiceException {
    private final List<ApiError> errors;

    public ApiValidationException(final ApiError error) {
        this.errors = Collections.singletonList(error);
    }
}
