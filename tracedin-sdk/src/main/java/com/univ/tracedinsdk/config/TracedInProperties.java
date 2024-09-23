package com.univ.tracedinsdk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "traced-in")
@Getter
@Setter
public class TracedInProperties {

    /**
     * 라이브러리 활성화 여부 (기본값: true)
     */
    private boolean enabled = true;

    /**
     * Exporter 엔드포인트 URL
     */
    private String endpoint = "http://tracedin.p-e.kr:8080";


    private String serviceName = "tracedin-service";

    private String exporter = "traced-in";

    /**
     * 메서드 추적을 위한 베이스 패키지 (지정하지 않으면 모든 메서드 추적)
     */
    private String basePackage;

    public String getPointcutExpression() {
        if (basePackage != null && !basePackage.isEmpty()) {
            return "execution(* " + basePackage + "..*(..))";
        } else {
            return "execution(* *(..))";
        }
    }
}
