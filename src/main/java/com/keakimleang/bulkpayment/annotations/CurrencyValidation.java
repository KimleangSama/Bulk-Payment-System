package com.keakimleang.bulkpayment.annotations;

import com.keakimleang.bulkpayment.utils.*;
import jakarta.validation.*;
import java.util.*;

public class CurrencyValidation implements ConstraintValidator<ValidCurrency, String> {

    private String[] supportedCurrencies;

    @Override
    public void initialize(final ValidCurrency constraintAnnotation) {
        supportedCurrencies = constraintAnnotation.supportedCurrencies();
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext validatorContext) {
        if (StringWrapperUtils.isBlank(value)) {
            return false;
        }
        if (Objects.isNull(supportedCurrencies) || StringWrapperUtils.isAnyBlank(supportedCurrencies)) {
            throw new IllegalArgumentException("supportedCurrencies should not contains empty string.");
        }
        for (final var ccy : supportedCurrencies) {
            if (ccy.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
