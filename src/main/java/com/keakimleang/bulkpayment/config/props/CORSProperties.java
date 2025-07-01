package com.keakimleang.bulkpayment.config.props;

import java.util.*;
import lombok.*;
import org.springframework.boot.context.properties.*;

@ConfigurationProperties(prefix = "security.cors")
@Getter
@Setter
public class CORSProperties {
    private List<String> allowedOrigins = List.of("*");
    private List<String> allowedHeaders = List.of("*");
    private List<String> allowedMethods = List.of("*");
    private Boolean allowedCredentials = false;
    private Boolean allowPrivateNetwork;
    private Long maxAge;
}
