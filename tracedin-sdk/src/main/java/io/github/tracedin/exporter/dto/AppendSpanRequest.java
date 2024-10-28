package io.github.tracedin.exporter.dto;

import io.github.tracedin.exporter.grpc.SpanProto;
import io.github.tracedin.exporter.grpc.SpanProto.Attributes;
import io.github.tracedin.exporter.grpc.SpanProto.Event;
import io.github.tracedin.exporter.grpc.SpanProto.Span;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public record AppendSpanRequest(
        String serviceName,
        String projectKey,
        String traceId,
        String spanId,
        String parentSpanId,
        String spanType,
        String name,
        String kind,
        String spanStatus,
        long startEpochNanos,
        long endEpochNanos,
        Attributes attributes,
        List<Event> events) {



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

    public record Event(String name, Map<String, Object> attributes, long epochNanos) {
        public static Event from(EventData eventData) {
            Map<String, Object> attributeMap = new HashMap<>();
            eventData.getAttributes().forEach((key, value) -> attributeMap.put(key.getKey(), value));
            return new Event(eventData.getName(), attributeMap, eventData.getEpochNanos());
        }
    }

    public static AppendSpanRequest from(SpanData spanData) {

        String serviceName = spanData.getResource().getAttribute(AttributeKey.stringKey("service.name"));
        Attributes attributes = Attributes.from(spanData);
        List<Event> events = spanData.getEvents().stream()
                .map(Event::from)
                .toList();
        return new AppendSpanRequest(
                serviceName,
                spanData.getResource().getAttribute(AttributeKey.stringKey("project.key")),
                spanData.getTraceId(),
                spanData.getSpanId(),
                spanData.getParentSpanId().isEmpty() ? null : spanData.getParentSpanId(),
                attributes.data().get("span.type") == null ? null : (String) attributes.data().get("span.type"),
                spanData.getName(),
                spanData.getKind().name(),
                spanData.getStatus().getStatusCode().name(),
                spanData.getStartEpochNanos(),
                spanData.getEndEpochNanos(),
                attributes,
                events
        );
    }

    public Span toGrpcSpan() {
        return Span.newBuilder()
                .setServiceName(serviceName)
                .setProjectKey(projectKey)
                .setTraceId(traceId)
                .setSpanId(spanId)
                .setParentSpanId(parentSpanId)
                .setSpanType(Optional.ofNullable(spanType).orElse(""))
                .setName(name)
                .setKind(kind)
                .setSpanStatus(spanStatus)
                .setStartEpochNanos(startEpochNanos)
                .setEndEpochNanos(endEpochNanos)
                .setAttributes(
                        SpanProto.Attributes.newBuilder()
                                .putAllData(attributes.data().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Entry::getKey,
                                                e -> String.valueOf(e.getValue()))))
                                .setCapacity(attributes.capacity())
                                .setTotalAddedValues(attributes.totalAddedValues())
                                .build())
                .addAllEvents(events.stream()
                        .map(event -> SpanProto.Event.newBuilder()
                                .setName(event.name())
                                .putAllAttributes(event.attributes().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Entry::getKey,
                                                e -> String.valueOf(e.getValue()))))
                                .setEpochNanos(event.epochNanos())
                                .build())
                        .toList())
                .build();
    }
}
