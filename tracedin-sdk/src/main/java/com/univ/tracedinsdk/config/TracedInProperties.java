package com.univ.tracedinsdk.config;

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

    /**
     * Exporter 엔드포인트 URL
     */
    private String endpoint = "http://tracedin.p-e.kr:8080";


    private String serviceName = "service-" + UUID.randomUUID();

    private String exporter = "traced-in";

    private double sampling = 1.0;

    @Getter
    private String basePackage;

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

    public String getPointcutExpression() {
        return "execution(* " + basePackage + "..*(..))";
    }

}
