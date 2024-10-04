package io.github.tracedin.config;

import io.github.tracedin.OpenTelemetryInitializer;
import io.github.tracedin.http.TracedInFeignClientInterceptor;
import io.github.tracedin.http.TracedInRestTemplateInterceptor;
import io.github.tracedin.aspect.TracingAspectConfig;
import io.github.tracedin.filter.ContextPropagateFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@Slf4j
@ConditionalOnProperty(prefix = "traced-in", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TracedInProperties.class)
public class TracedInAutoConfig {

    private final TracedInProperties properties;

    public TracedInAutoConfig(TracedInProperties properties) {
        this.properties = properties;
        OpenTelemetryInitializer.initialize(properties);
        log.info("TracedIn initialized with service name: {}", properties.getServiceName());
    }

    @Bean
    public Advisor tracingAspectConfiguration() {
        return new TracingAspectConfig(properties).tracingAdvisor();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .additionalInterceptors(new TracedInRestTemplateInterceptor(properties))
                .build();
    }

    @Bean
    public FilterRegistrationBean<ContextPropagateFilter> loggingFilter() {
        FilterRegistrationBean<ContextPropagateFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ContextPropagateFilter(properties));
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public TracedInFeignClientInterceptor tracedInFeignClientInterceptor() {
        return new TracedInFeignClientInterceptor(properties);
    }
}
