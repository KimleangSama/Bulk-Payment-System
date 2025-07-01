package com.keakimleang.bulkpayment.utils;

import java.time.*;
import java.time.format.*;

public final class DateUtil {

    private DateUtil() {
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDate parseDate(final String value,
                                      final String fmt) {
        if (StringWrapperUtils.isBlank(value)) {
            return null;
        }
        final var dft = DateTimeFormatter.ofPattern(fmt);
        return LocalDate.parse(value, dft);
    }

    public static LocalDateTime parseDateTime(final String value,
                                              final String fmt) {
        if (StringWrapperUtils.isBlank(value)) {
            return null;
        }
        final var dft = DateTimeFormatter.ofPattern(fmt);
        return LocalDateTime.parse(value, dft);
    }

    public static String formatDate(final LocalDate date,
                                    final String fmt) {
        final var dtf = DateTimeFormatter.ofPattern(fmt);
        return date.format(dtf);
    }

    public static String formatDateTime(final LocalDateTime dateTime,
                                        final String fmt) {
        final var dtf = DateTimeFormatter.ofPattern(fmt);
        return dateTime.format(dtf);
    }
}
