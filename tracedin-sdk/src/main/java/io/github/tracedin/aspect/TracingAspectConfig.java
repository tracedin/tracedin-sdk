package io.github.tracedin.aspect;

import io.github.tracedin.config.TracedInProperties;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;

public class TracingAspectConfig {

    private final TracedInProperties properties;

    public TracingAspectConfig(TracedInProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Advisor tracingAdvisor() {
        String pointcutExpression = properties.getPointcutExpression();

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(pointcutExpression);

        Advice advice = new TracingInterceptor(properties);

        return new DefaultPointcutAdvisor(pointcut, advice);
    }

}
