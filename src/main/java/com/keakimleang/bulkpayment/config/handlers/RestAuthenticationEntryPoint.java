package com.keakimleang.bulkpayment.config.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RestAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        logger.error("Error Message - {}", ex.getMessage());
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap("{\"statusCode\": 401, \"success\": false,  \"status\": \"UNAUTHORIZED\", \"error\": \"User is not authorized to access.\"}".getBytes())));
    }
}
