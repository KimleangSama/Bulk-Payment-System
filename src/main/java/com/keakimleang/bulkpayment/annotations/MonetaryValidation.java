package com.keakimleang.bulkpayment.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.regex.Pattern;

public class MonetaryValidation implements ConstraintValidator<ValidMonetary, Object> {

    private final Pattern digitReg = Pattern.compile("^\\d+(\\.\\d+)?$");

    private boolean optional;
    private long value;

    @Override
    public void initialize(final ValidMonetary constraintAnnotation) {
        optional = constraintAnnotation.optional();
        value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(final Object amount,
                           final ConstraintValidatorContext validatorContext) {
        if (Objects.isNull(amount) && !optional) {
            return false;
        }

        if (Objects.isNull(amount)) {
            return true;
        }

        if (!(amount instanceof String) &&
                !(amount instanceof Number)) {
            throw new IllegalArgumentException("Wrong validation type. It must String or Number type.");
        }
        if (amount instanceof String amountStr) {
            final var match = digitReg.matcher(amountStr.replace(",", ""));
            if (!match.matches()) {
                return false;
            }
        }

        final var conditionValue = BigDecimal.valueOf(value);
        final var inputValue = new BigDecimal(amount.toString().replace(",", ""), MathContext.DECIMAL64);
        return optional ? inputValue.compareTo(conditionValue) >= 0 : inputValue.compareTo(conditionValue) > 0;
    }
}
