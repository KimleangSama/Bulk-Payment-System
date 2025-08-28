package com.keakimleang.bulkpayment.customvalidation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static Validator instance() {
        try (final var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
