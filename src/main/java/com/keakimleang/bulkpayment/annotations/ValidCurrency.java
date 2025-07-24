package com.keakimleang.bulkpayment.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CurrencyValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Currency must be one of the supported currencies";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] supportedCurrencies() default {"KHR", "USD"}; // Default supported currencies
}
