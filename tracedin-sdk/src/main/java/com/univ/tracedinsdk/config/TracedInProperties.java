package com.univ.tracedinsdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "traced-in")
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

    public String getServiceName() {
        return serviceName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getExporter() {
        return exporter;
    }

    public void setExporter(String exporter) {
        this.exporter = exporter;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }
}
