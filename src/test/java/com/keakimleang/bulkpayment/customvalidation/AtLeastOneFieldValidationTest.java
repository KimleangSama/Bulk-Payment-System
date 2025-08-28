package com.keakimleang.bulkpayment.customvalidation;

import com.keakimleang.bulkpayment.annotations.AtLeastOneField;
import lombok.Getter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AtLeastOneFieldValidationTest {

    @Getter
    @AtLeastOneField(fields = { "p1", "p2"}, message = "At least p1 or p2 provided value")
    private static class POJO {
        private String p1;
        private String p2;
    }

    @Test
    void testAtLeastOneFieldValidation() {
        final var validator = ValidationUtil.instance();

        final var pojo = new POJO();
        var constraintViolations = validator.validate(pojo);

        assertEquals(1, constraintViolations.size());
        assertEquals("At least p1 or p2 provided value", constraintViolations.iterator().next().getMessage());

        pojo.p1 = "P1";
        constraintViolations = validator.validate(pojo);
        assertEquals(0, constraintViolations.size());

        pojo.p2 = "P2";
        pojo.p1 = null;
        constraintViolations = validator.validate(pojo);
        assertEquals(0, constraintViolations.size());

        pojo.p2 = "P2";
        pojo.p1 = "P1";
        constraintViolations = validator.validate(pojo);
        assertEquals(0, constraintViolations.size());
    }
}