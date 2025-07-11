package com.keakimleang.bulkpayment.services;

import com.keakimleang.bulkpayment.services.fallbacks.CommonFallbackFactory;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class AuthServiceClientFallbackFactory extends CommonFallbackFactory<AuthServiceClient> {
    public AuthServiceClientFallbackFactory() {
        super("Auth");
    }

    @Override
    public AuthServiceClient apply(final Throwable error) {
        return new AuthServiceClientFallback(error);
    }

    public class AuthServiceClientFallback implements AuthServiceClient {

        private final Throwable error;

        public AuthServiceClientFallback(final Throwable error) {
            this.error = error;
        }

        @Override
        public Mono<Map<String, Object>> getUserById(Long userId) {
            log.info("Invoked fallback getUserDetailsById({})", userId);
            return handleException(error);
        }
    }
}
