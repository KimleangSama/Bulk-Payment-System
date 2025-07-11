package com.keakimleang.bulkpayment.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public final class AppUtil {

    private AppUtil() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER.copy();
    }

    public static <T> T convertJson(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<T>() {
            });
        } catch (final IOException e) {
            log.error("Unable to convert json to object: {}", json);
            throw new RuntimeException("Unable to read json...");
        }
    }

    public static String toJson(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to convert object to json...");
        }
    }

    public static BigDecimal rounding(final BigDecimal bigDecimal,
                                  final int scale) {
        if (Objects.isNull(bigDecimal)) {
            return BigDecimal.valueOf(0.0);
        }
        return bigDecimal.setScale(scale, RoundingMode.DOWN);
    }

    public static List<String> rsplit(final String str,
                                      final String separateChars) {
        return rsplit(str, separateChars, 0);
    }

    public static List<String> rsplit(final String str,
                                      final String separateChars,
                                      final int max) {
        final var result = new ArrayList<String>();
        final var maxSplit = Math.max(0, max);
        if (Objects.isNull(str)) {
            return result;
        }
        if (str.isBlank()) {
            result.add(str);
            return result;
        }

        final var split = StringUtils.splitByWholeSeparatorPreserveAllTokens(str, separateChars);
        if (split.length == 1) {
            result.add(split[0]);
            return result;
        }

        final var elements = Arrays.asList(split);
        if (maxSplit == 0) {
            result.addAll(elements);
            return result;
        }

        final var numberOfRightElement = maxSplit - 1;
        final var startRightIndex = elements.size() - numberOfRightElement;
        final var rightElements = elements.subList(startRightIndex, elements.size());
        final var firstElements = elements.subList(0, startRightIndex);
        result.add(String.join(separateChars, firstElements));
        result.addAll(rightElements);
        return result;
    }

    public static boolean isTrue(final Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public static boolean isFalse(final Boolean value) {
        return !isTrue(value);
    }
}
