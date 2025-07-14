package com.keakimleang.bulkpayment.config;

import com.keakimleang.bulkpayment.securities.CustomReactiveUserDetailsService;
import com.keakimleang.bulkpayment.utils.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter implements WebFilter {
    private final TokenUtil tokenUtil;
    private final CustomReactiveUserDetailsService service;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(auth -> auth.startsWith("Bearer "))
                .map(auth -> auth.substring(7))
                .filter(tokenUtil::validateToken)
                .flatMap(token -> {
                    String username = tokenUtil.getUsernameFromToken(token);
                    return service.findByUsername(username)
                            .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            ));
                })
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                )
                .onErrorResume(e -> {
                    log.error("Authentication error", e);
                    return chain.filter(exchange);
                });
    }
}
