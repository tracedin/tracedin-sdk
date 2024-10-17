package io.github.tracedin.config;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "traced-in")
@Getter
@Setter
public class TracedInProperties implements ApplicationContextAware {

    /**
     * 라이브러리 활성화 여부 (기본값: true)
     */
    private boolean enabled = true;

    private String endPoint = "http://tracedin.p-e.kr";

    private boolean debug = false;

    private String serviceName = "service-" + UUID.randomUUID();

    private double sampling = 1.0;

    public String projectKey;

    private String basePackage;

    private long metricInterval = 10;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        setDefaultBasePackage();
    }

    /**
     * basePackage가 null이거나 빈 문자열인 경우, @SpringBootApplication이 있는 패키지로 설정
     */
    private void setDefaultBasePackage() {
        if (!StringUtils.hasText(this.basePackage)) {
            String mainApplicationClass = applicationContext.getEnvironment().getProperty("sun.java.command");
            if (mainApplicationClass != null) {
                try {
                    Class<?> mainClass = Class.forName(mainApplicationClass.split(" ")[0]);
                    this.basePackage = mainClass.getPackage().getName();
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Cannot determine base package", e);
                }
            } else {
                throw new IllegalStateException("Cannot determine base package. Please set basePackage property.");
            }
        }
    }

    @PostConstruct
    public void validateProjectKey() {
        if (!StringUtils.hasText(this.projectKey)) {
            throw new IllegalStateException("프로젝트 키가 등록되지 않았습니다. 프로젝트 키를 발급받고 traced-in.project-key 속성을 설정해주세요.");
        }
    }

    public String getSpanEndpoint() {
        return this.endPoint + "/api/v1/spans";
    }

    public String getMetricEndpoint() {
        return this.endPoint + "/api/v1/service-metrics";
    }

    public String getPointcutExpression() {
        return "execution(* " + basePackage + "..*(..))";
    }

}
