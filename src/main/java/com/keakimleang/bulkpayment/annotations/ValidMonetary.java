package com.keakimleang.bulkpayment.annotations;

import jakarta.validation.*;
import java.lang.annotation.*;

@Constraint(validatedBy = MonetaryValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMonetary {
    String message() default "Monetary value must be positive number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean optional() default false;

    long value() default 0L;
}
