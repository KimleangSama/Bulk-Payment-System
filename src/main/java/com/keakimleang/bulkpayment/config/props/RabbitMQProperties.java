package com.keakimleang.bulkpayment.config.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "spring.rabbitmq")
@Getter
@Setter
@ToString
@Configuration
public class RabbitMQProperties {
    private String host;
    private int port;
    private String username;
    private String password;
}
