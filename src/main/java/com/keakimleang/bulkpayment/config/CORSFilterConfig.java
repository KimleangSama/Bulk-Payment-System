package com.keakimleang.bulkpayment.config;


import com.keakimleang.bulkpayment.config.props.*;
import java.util.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import org.springframework.web.cors.reactive.*;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class CORSFilterConfig {
    private final CORSProperties corsProperties;

    @Bean
    public CorsWebFilter corsWebFilter() {
        final var config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedHeaders(corsProperties.getAllowedHeaders());
        config.setAllowedMethods(corsProperties.getAllowedMethods());
        config.setAllowCredentials(corsProperties.getAllowedCredentials());
        if (Objects.nonNull(corsProperties.getMaxAge())) {
            config.setMaxAge(corsProperties.getMaxAge());
        }
        if (Objects.nonNull(corsProperties.getAllowPrivateNetwork())) {
            config.setAllowPrivateNetwork(config.getAllowPrivateNetwork());
        }

        final var urlCors = new UrlBasedCorsConfigurationSource();
        urlCors.registerCorsConfiguration("/api/v1/**", config);

        return new CorsWebFilter(urlCors);
    }
}
