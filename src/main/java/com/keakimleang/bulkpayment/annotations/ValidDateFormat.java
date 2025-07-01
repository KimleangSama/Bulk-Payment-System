package com.keakimleang.bulkpayment.annotations;

import jakarta.validation.*;
import java.lang.annotation.*;

@Constraint(validatedBy = DateFormatValidation.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {

    String message() default "Invalid date format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    ISO iso() default ISO.DATE;

    String pattern();

    boolean optional() default false;

    enum ISO {
        DATE,
        TIME,
        DATE_TIME
    }
}
