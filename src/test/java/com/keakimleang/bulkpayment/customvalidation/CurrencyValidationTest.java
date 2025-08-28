package com.keakimleang.bulkpayment.customvalidation;

import com.keakimleang.bulkpayment.annotations.ValidCurrency;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CurrencyValidationTest {

    private static class POJO {
        @ValidCurrency(message = "Currency must KHR or USD")
        public String currency;
    }

    @Test
    void testValidateCurrency() {
        final var expectedMsg = "Currency must KHR or USD";
        final var validator = ValidationUtil.instance();
        final var pojo = new POJO();

        var violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        pojo.currency = "THB";
        violations = validator.validate(pojo);
        assertEquals(1, violations.size());
        assertEquals(expectedMsg, violations.iterator().next().getMessage());

        // lower or upper or mix case, it should consider valid value
        pojo.currency = "KHR";
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.currency = "khr";
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());

        pojo.currency = "Usd";
        violations = validator.validate(pojo);
        assertEquals(0, violations.size());
    }

}