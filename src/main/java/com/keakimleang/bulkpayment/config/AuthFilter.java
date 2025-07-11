package com.keakimleang.bulkpayment.config;

import com.keakimleang.bulkpayment.securities.CustomReactiveUserDetailsService;
import com.keakimleang.bulkpayment.utils.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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
        return Mono.defer(() -> {
            try {
                final var bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
                final String token = bearerToken != null && bearerToken.startsWith("Bearer ")
                        ? bearerToken.substring(7)
                        : null;
                if (token == null) {
                    log.error("No token found in request headers");
                    return chain.filter(exchange);
                }
                final String username = tokenUtil.getUsernameFromToken(token);
                if (username != null && tokenUtil.isTokenNotExpired(token) && tokenUtil.validateToken(token)) {
                    return service.findByUsername(username)
                            .flatMap(userDetails -> {
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                                SecurityContextImpl securityContext = new SecurityContextImpl(auth);
                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                            });
                }
                return chain.filter(exchange);
            } catch (Exception e) {
                log.error("Error processing authentication filter", e);
                return chain.filter(exchange);
            }
        });
    }
}
