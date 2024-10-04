package io.github.tracedin;

import io.github.tracedin.config.TracedInProperties;
import io.github.tracedin.exporter.LoggingSpanExporter;
import io.github.tracedin.exporter.TracedInExporter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenTelemetryInitializer {


    public static void initialize(TracedInProperties properties) {

        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.builder()
                        .put("service.name", properties.getServiceName())
                        .put("project.key", properties.getProjectKey())
                        .build()));

        SdkTracerProvider tracerProvider = switch (properties.getExporter().toLowerCase()) {
            case "traced-in" -> {
                log.info("Initialized OpenTelemetry with TracedInExporter to endpoint: {}", properties.getEndpoint());
                TracedInExporter tracedInExporter = new TracedInExporter(properties.getEndpoint());
                yield SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(tracedInExporter).build())
                        .setResource(resource)
                        .setSampler(Sampler.traceIdRatioBased(properties.getSampling()))
                        .build();
            }
            case "logging" -> {
                log.info("Initialized OpenTelemetry with LoggingSpanExporter");
                LoggingSpanExporter loggingExporter = new LoggingSpanExporter();
                yield SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(loggingExporter).build())
                        .setResource(resource)
                        .build();
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported exporter type: " + properties.getExporter());
        };

        // OpenTelemetry SDK 구성 및 글로벌 등록
        OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();

    }


    public static OpenTelemetry getOpenTelemetry() {
        return GlobalOpenTelemetry.get();
    }

}
