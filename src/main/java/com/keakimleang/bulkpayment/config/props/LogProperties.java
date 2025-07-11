package com.keakimleang.bulkpayment.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "log")
public record LogProperties(Loki loki, Logstash logstash) {

    public record Loki(String url) {
    }

    public record Logstash(String url) {
    }
}