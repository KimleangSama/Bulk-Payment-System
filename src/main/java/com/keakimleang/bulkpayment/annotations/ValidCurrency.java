package com.keakimleang.bulkpayment.annotations;

import jakarta.validation.*;
import java.lang.annotation.*;

@Constraint(validatedBy = CurrencyValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "Currency must be one of the supported currencies";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] supportedCurrencies() default {"KHR", "USD"}; // Default supported currencies
}
