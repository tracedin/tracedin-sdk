package io.github.tracedin.config;

import io.github.tracedin.OpenTelemetryInitializer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@Slf4j
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        log.info("Initializing OpenTelemetry DataSource");
        OpenTelemetry openTelemetry = OpenTelemetryInitializer.getOpenTelemetry();
        DataSource dataSource = properties.initializeDataSourceBuilder().build();
        return JdbcTelemetry.create(openTelemetry).wrap(dataSource);
    }
}
