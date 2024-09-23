package com.univ.tracedinsdk.config;

import com.univ.tracedinsdk.OpenTelemetryInitializer;
import com.univ.tracedinsdk.aspect.TracedInRestTemplateInterceptor;
import com.univ.tracedinsdk.aspect.TracingAspectConfig;
import com.univ.tracedinsdk.filter.ContextPropagateFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@ConditionalOnProperty(prefix = "traced-in", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TracedInProperties.class)
public class TracedInAutoConfig {

    private final TracedInProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(TracedInAutoConfig.class);

    public TracedInAutoConfig(TracedInProperties properties) {
        this.properties = properties;
        OpenTelemetryInitializer.initialize(properties);
        logger.info("TracedIn initialized with service name: {}", properties.getServiceName());
    }

    @Bean
    public Advisor tracingAspectConfiguration() {
        return new TracingAspectConfig(properties).tracingAdvisor();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .additionalInterceptors(new TracedInRestTemplateInterceptor())
                .build();
    }

    @Bean
    public DataSourceBeanPostProcessor dataSourceBeanPostProcessor() {
        return new DataSourceBeanPostProcessor();
    }

    @Bean
    public FilterRegistrationBean<ContextPropagateFilter> loggingFilter() {
        FilterRegistrationBean<ContextPropagateFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ContextPropagateFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
