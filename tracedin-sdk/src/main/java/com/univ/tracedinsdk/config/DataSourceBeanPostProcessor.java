package com.univ.tracedinsdk.config;

import com.univ.tracedinsdk.OpenTelemetryInitializer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.OpenTelemetryDataSource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class DataSourceBeanPostProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TracedInAutoConfig.class);

    private final OpenTelemetry openTelemetry;

    public DataSourceBeanPostProcessor() {
        this.openTelemetry = OpenTelemetryInitializer.getOpenTelemetry();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource originalDataSource && !(bean instanceof OpenTelemetryDataSource)) {

            JdbcTelemetry jdbcTelemetry = JdbcTelemetry.create(openTelemetry);
            logger.info("Wrapping DataSource bean: {}", beanName);
            return jdbcTelemetry.wrap(originalDataSource);
        }
        return bean;
    }
}
