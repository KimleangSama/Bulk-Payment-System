package com.keakimleang.bulkpayment.payloads;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String errorCode, String message, String field) {

    public static ApiError validateInput(final String field,
                                         final String message) {
        return new ApiError(ErrorCode.INPUT_VALIDATION.getCode(), field, message);
    }
}
