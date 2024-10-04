package io.github.tracedin.exporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.HashMap;
import java.util.Map;

public record AppendSpanRequest(
        String serviceName,
        String projectKey,
        String traceId,
        String spanId,
        String parentSpanId,
        String spanType,
        String name,
        String kind,
        long startEpochNanos,
        long endEpochNanos,
        Attributes attributes) {
    public record Attributes(Map<String, Object> data, int capacity, int totalAddedValues) {
        public static Attributes from(SpanData spanData) {
            HashMap<String, Object> attributeMap = new HashMap<>() {{
                spanData.getAttributes().forEach((key, value) -> put(key.getKey(), value));
            }};
            return new Attributes(
                    attributeMap,
                    spanData.getAttributes().size(),
                    spanData.getTotalAttributeCount()
            );
        }
    }

    public static AppendSpanRequest from(SpanData spanData) {

        String serviceName = spanData.getResource().getAttribute(AttributeKey.stringKey("service.name"));
        Attributes attributes = Attributes.from(spanData);


        return new AppendSpanRequest(
                serviceName,
                spanData.getResource().getAttribute(AttributeKey.stringKey("project.key")),
                spanData.getTraceId(),
                spanData.getSpanId(),
                spanData.getParentSpanId().isEmpty() ? null : spanData.getParentSpanId(),
                attributes.data().get("span.type") == null ? null : (String) attributes.data().get("span.type"),
                spanData.getName(),
                spanData.getKind().name(),
                spanData.getStartEpochNanos(),
                spanData.getEndEpochNanos(),
                attributes
        );
    }
}
