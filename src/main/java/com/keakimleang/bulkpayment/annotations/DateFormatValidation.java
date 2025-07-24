package com.keakimleang.bulkpayment.annotations;

import com.keakimleang.bulkpayment.utils.StringWrapperUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateFormatValidation implements ConstraintValidator<ValidDateFormat, String> {

    private ValidDateFormat.ISO iso;
    private String pattern;
    private boolean optional;

    @Override
    public void initialize(final ValidDateFormat constraintAnnotation) {
        iso = constraintAnnotation.iso();
        pattern = constraintAnnotation.pattern();
        optional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(final String value,
                           final ConstraintValidatorContext validatorContext) {
        if (StringWrapperUtils.isBlank(value) && !optional) {
            return false;
        }
        if (StringWrapperUtils.isBlank(value) && optional) {
            return true;
        }
        try {
            final var dtf = DateTimeFormatter.ofPattern(pattern);
            switch (iso) {
                case DATE -> LocalDate.parse(value, dtf);
                case TIME -> LocalTime.parse(value, dtf);
                case DATE_TIME -> LocalDateTime.parse(value, dtf);
            }
            return true;
        } catch (final Exception ignore) {
            return false;
        }
    }
}
