package com.keakimleang.bulkpayment.annotations;

import jakarta.validation.*;
import java.lang.annotation.*;

@Constraint(validatedBy = AtLeastOneFieldValidation.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneField {

    String message() default "At least one of the fields must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] fields();
}
