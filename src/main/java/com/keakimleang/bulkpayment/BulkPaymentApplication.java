package com.keakimleang.bulkpayment;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.properties.*;
import org.springframework.data.r2dbc.repository.config.*;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@EnableReactiveFeignClients
@EnableR2dbcRepositories
@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.keakimleang.bulkpayment.config.props")
public class BulkPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(BulkPaymentApplication.class, args);
    }

}
