package com.univ.tracedinsdk.config;

import com.univ.tracedinsdk.OpenTelemetryInitializer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;


@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        logger.info("Initializing OpenTelemetry DataSource");

        OpenTelemetry openTelemetry = OpenTelemetryInitializer.getOpenTelemetry();
        DataSource dataSource = properties.initializeDataSourceBuilder().build();
        return JdbcTelemetry.create(openTelemetry).wrap(dataSource);
    }
}
