package io.github.tracedin.exporter.dto;

import java.util.List;

public record AppendServiceMetricsRequest(
        String projectKey,
        String serviceName,
        List<MetricRequest> metrics
) {
    public static AppendServiceMetricsRequest from(List<MetricRequest> metrics) {
        return new AppendServiceMetricsRequest(
                metrics.get(0).attributes().get("project.key"),
                metrics.get(0).attributes().get("service.name"),
                metrics
        );
    }
}
