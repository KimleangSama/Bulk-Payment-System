package com.keakimleang.bulkpayment.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String message, T data) {

    public static ApiResponse<String> okResponse(final String message) {
        return new ApiResponse<>(200, message, null);
    }

    public static <T> ApiResponse<T> okResponse(final T data) {
        return new ApiResponse<>(200, null, data);
    }

    public static <T> ApiResponse<T> okResponse(final T data,
                                                final String message) {
        return new ApiResponse<>(200, message, data);
    }

    public static ApiResponse<Map<String, Long>> cratedResourceResponse(final Long data,
                                                                        final String message) {
        final var dataMap = Map.of("resourceId", data);
        return new ApiResponse<>(200, message, dataMap);
    }
}
