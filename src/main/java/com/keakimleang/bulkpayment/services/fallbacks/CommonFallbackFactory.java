package com.keakimleang.bulkpayment.services.fallbacks;

import com.keakimleang.bulkpayment.exceptions.ErrorCode;
import com.keakimleang.bulkpayment.exceptions.FeignServiceException;
import com.keakimleang.bulkpayment.utils.AppUtil;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.net.ConnectException;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactivefeign.FallbackFactory;
import reactivefeign.client.ReactiveFeignException;
import reactivefeign.publisher.retry.OutOfRetriesException;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class CommonFallbackFactory<T> implements FallbackFactory<T> {
    private final String resource;

    public CommonFallbackFactory(final String resource) {
        this.resource = resource;
    }

    public <R> Mono<R> handleException(final Throwable error) {
        if (error instanceof CallNotPermittedException cne) {
            log.warn("Skip request of {} during CircuitBreaker in OPEN state", cne.getCausingCircuitBreakerName());
            return Mono.error(new FeignServiceException(503, ErrorCode.FEIGN_OPEN_STATE, "%s service is unavailable".formatted(resource)));
        }
        if (error instanceof ReactiveFeignException rfe) {
            final var req = rfe.getRequest();
            final var cause = getCauseException(error);
            if (cause instanceof ConnectException) {
                log.warn("Cannot connect to {} {} - {}", req.method(), req.uri(), req.methodKey());
            } else if (cause instanceof FeignException fe) {
                return Mono.error(new FeignServiceException(fe.status(), getFeignExceptionMsg(fe)));
            } else if (cause instanceof OutOfRetriesException ore) {
                log.warn("Error after reach of retry {}", ore.getMessage());
            }
            return Mono.error(new FeignServiceException(503, "%s service is unavailable".formatted(resource)));
        }
        if (error instanceof FeignException fe) {
            return Mono.error(new FeignServiceException(fe.status(), getFeignExceptionMsg(fe)));
        }

        log.warn("Unhandled error of type={} with msg={}", error.getClass(), error.getMessage());

        return Mono.error(new FeignServiceException(500, "%s server internal server error".formatted(resource)));
    }

    private Throwable getCauseException(final Throwable e) {
        var cause = e.getCause();
        if (Objects.isNull(cause)) {
            return e;
        }
        var previousCause = cause;
        while (Objects.nonNull(cause)) {
            previousCause = cause;
            cause = cause.getCause();
        }
        return previousCause;
    }

    private String getFeignExceptionMsg(final FeignException fe) {
        final var errorRep = AppUtil.<Map<String, Object>>convertJson(fe.contentUTF8());
        final var msg = (String) errorRep.get("errorMessage");
        if (StringUtils.isBlank(msg)) {
            log.warn("Unknown error msg response from downstream server. response={}", errorRep);
            return fe.getMessage();
        }
        return msg;
    }
}
