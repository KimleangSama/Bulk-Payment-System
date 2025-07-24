package com.keakimleang.bulkpayment.config;

import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.spi.ConnectionFactory;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

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
