package com.keakimleang.bulkpayment.config.props;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
public class CORSProperties {
    private List<String> allowedOrigins;
    private List<String> allowedHeaders;
    private List<String> allowedMethods;
    private Boolean allowedCredentials = false;
    private Boolean allowPrivateNetwork;
    private Long maxAge;
}
