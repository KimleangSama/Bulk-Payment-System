package com.keakimleang.bulkpayment.config;

import co.elastic.apm.attach.ElasticApmAttacher;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
public class APMConfig {
    @Value("${elastic.apm.enabled:true}")
    private boolean enabled;
    @Value("${elastic.apm.server-url}")
    private String serverUrl;
    @Value("${elastic.apm.service-name}")
    private String serviceName;
    @Value("${elastic.apm.secret-token}")
    private String secretToken;
    @Value("${elastic.apm.environment:dev}")
    private String environment;
    @Value("${elastic.apm.application-packages:com.keakimleang.bulkpayment}")
    private String applicationPackages;
    @Value("${elastic.apm.log-level:INFO}")
    private String logLevel;
    @Value("${elastic.apm.universal-profiling-integration-enabled:true}")
    private boolean universalProfilingIntegrationEnabled;

    @PostConstruct
    public void init() {
        Map<String, String> apmProps = new HashMap<>();
        apmProps.put("server_url", serverUrl);
        apmProps.put("service_name", serviceName);
        apmProps.put("secret_token", secretToken);
        apmProps.put("environment", environment);
        apmProps.put("application_packages", applicationPackages);
        apmProps.put("log_level", logLevel);

        if (universalProfilingIntegrationEnabled) {
            apmProps.put("universal_profiling_integration_enabled", "true");
        }

        ElasticApmAttacher.attach(apmProps);
    }
}