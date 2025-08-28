package com.keakimleang.bulkpayment.customvalidation;

import com.keakimleang.bulkpayment.annotations.ValidMonetary;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MonetaryValidationTest {
    private static class Money1 {
        @ValidMonetary(value = 1, message = "Amount must be greater 1")
        public String amount;
    }

    private static class Money2 {
        @ValidMonetary(optional = true, message = "If provided value, it must be greater than 0")
        public BigDecimal amount;
    }

    private Validator validator;

    @BeforeEach
    void setup() {
        this.validator = ValidationUtil.instance();
    }

    @Test
    void testRequiredValue() {
        final var expectedMsg = "Amount must be greater 1";
        final var pojo = new Money1();
        var violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.amount = "invalid";
        violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.amount = "-1.0";
        violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.amount = "0.99";
        violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.amount = "1.00";
        violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.amount = "1.1";
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.amount = "2";
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());
    }

    @Test
    void testOptionalValue() {
        final var expectedMsg = "If provided value, it must be greater than 0";
        final var pojo = new Money2();

        var violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.amount = BigDecimal.valueOf(-1.0);
        violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.amount = BigDecimal.valueOf(0.0);
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.amount = BigDecimal.valueOf(0.01);
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.amount = BigDecimal.valueOf(2);
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());
    }

    @Test
    void testValidateMoneyValueFormat() {
        final var pojo = new Money1();
        pojo.amount = "1,000.00";
        var violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.amount = "2,983,000.00";
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());
    }
}