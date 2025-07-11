package com.keakimleang.bulkpayment.config.props;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "token")
public class TokenProperties {
    private Long accessTokenExpiresHours;
    private Long refreshTokenExpiresHours;
    private String domain;
}