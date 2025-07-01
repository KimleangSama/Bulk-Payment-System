package com.keakimleang.bulkpayment.utils;

import java.util.*;
import org.apache.commons.lang3.*;

public class AppUtil {
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
}
