package com.keakimleang.bulkpayment.annotations;

import com.keakimleang.bulkpayment.utils.*;
import jakarta.validation.*;
import java.util.*;
import org.springframework.beans.*;

public class AtLeastOneFieldValidation implements ConstraintValidator<AtLeastOneField, Object> {
    private String[] fields;

    @Override
    public void initialize(final AtLeastOneField constraintAnnotation) {
        fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(final Object value,
                           final ConstraintValidatorContext validatorContext) {
        if (Objects.isNull(value)) {
            return false;
        }
        final var bean = new BeanWrapperImpl(value);
        for (final var field : fields) {
            final var fieldValue = bean.getPropertyValue(field);
            if (Objects.nonNull(fieldValue) && StringWrapperUtils.isNotBlank(fieldValue.toString())) {
                return true;
            }
        }
        return false;
    }
}
