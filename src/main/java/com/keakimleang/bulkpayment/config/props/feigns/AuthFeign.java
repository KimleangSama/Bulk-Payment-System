package com.keakimleang.bulkpayment.config.props.feigns;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "feign-client.auth")
public class AuthFeign {
    private String url;
    private String serviceName;
}
