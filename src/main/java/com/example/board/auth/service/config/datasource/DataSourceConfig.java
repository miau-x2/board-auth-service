package com.example.board.auth.service.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.read-write")
    public DataSourceProperties readWriteDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.read-write.hikari")
    public DataSource readWriteDataSource(@Qualifier("readWriteDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.read-only")
    public DataSourceProperties readOnlyDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.read-only.hikari")
    public DataSource readOnlyDataSource(@Qualifier("readOnlyDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("readWriteDataSource") DataSource readWriteDataSource,
            @Qualifier("readOnlyDataSource") DataSource readOnlyDataSource,
            DataSourceContextHolder dataSourceContextHolder) {

        var routingDataSource = new ReplicationRoutingDataSource(dataSourceContextHolder);
        var dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.READ_WRITE, readWriteDataSource);
        dataSourceMap.put(DataSourceType.READ_ONLY, readOnlyDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(readWriteDataSource);

        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}