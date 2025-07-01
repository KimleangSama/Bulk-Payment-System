package com.keakimleang.bulkpayment.config;

import com.zaxxer.hikari.*;
import io.r2dbc.spi.*;
import javax.sql.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.r2dbc.connection.*;
import org.springframework.transaction.*;

@Configuration(proxyBeanMethods = false)
public class DatasourceConfig {

    @Bean(name = "jdbcDatasource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource jdbcDatasource() {
        return new HikariDataSource();
    }

    @Bean
    public R2dbcTransactionManager r2dbcTransactionManager(final ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public PlatformTransactionManager jdbcTransactionManager(final DataSource jdbcDatasource) {
        return new DataSourceTransactionManager(jdbcDatasource);
    }
}
