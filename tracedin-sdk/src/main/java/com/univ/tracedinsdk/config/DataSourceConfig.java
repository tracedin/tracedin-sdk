package com.univ.tracedinsdk.config;

import com.univ.tracedinsdk.OpenTelemetryInitializer;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@AutoConfiguration
public class DataSourceConfig {


    private final DataSource originalDataSource;

    public DataSourceConfig(@Lazy DataSource originalDataSource) {
        this.originalDataSource = originalDataSource;
    }

    @Bean
    public DataSource dataSource() {
        return JdbcTelemetry.create(OpenTelemetryInitializer.getOpenTelemetry()).wrap(originalDataSource);
    }
}
