package com.keakimleang.bulkpayment.utils;

import java.math.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public final class CastObjectUtil {

    private CastObjectUtil() {
    }

    public static BigDecimal getBigDecimal(final String value,
                                           final int scale) {
        return getBigDecimal(value, scale, RoundingMode.HALF_EVEN, null);
    }

    public static BigDecimal getBigDecimal(final String value,
                                           final int scale,
                                           final RoundingMode roundingMode,
                                           final BigDecimal defaultValue) {
        try {
            return new BigDecimal(value.replace(",", ""), MathContext.DECIMAL64).setScale(scale, roundingMode);
        } catch (final Exception ignore) {
            return Optional.ofNullable(defaultValue)
                    .map(b -> b.setScale(scale, roundingMode))
                    .orElse(null);
        }
    }

    public static LocalDate getLocalDate(final String value,
                                         final String format) {
        try {
            return DateUtil.parseDate(value, format);
        } catch (final DateTimeParseException ignore) {
            return null;
        }
    }

    public static LocalDateTime getLocalDateTime(final String value,
                                                 final String format) {
        try {
            return DateUtil.parseDateTime(value, format);
        } catch (final DateTimeParseException ignore) {
            return null;
        }
    }
}
