package com.keakimleang.bulkpayment.services;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@ReactiveFeignClient(
        name = "${feign-client.auth.service-name}",
        url = "${feign-client.auth.url}",
        fallbackFactory = AuthServiceClientFallbackFactory.class)
public interface AuthServiceClient {

    @GetMapping("/api/v1/users/{userId}/details")
    Mono<Map<String, Object>> getUserById(@PathVariable("userId") Long userId);
}
