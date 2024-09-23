package com.univ.tracedinsdk;

import com.univ.tracedinsdk.config.TracedInProperties;
import com.univ.tracedinsdk.exporter.LoggingSpanExporter;
import com.univ.tracedinsdk.exporter.TracedInExporter;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
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
                        .build()));

        Sampler sampler = Sampler.traceIdRatioBased(properties.getSampling());

        SdkTracerProvider tracerProvider = switch (properties.getExporter().toLowerCase()) {
            case "traced-in" -> {
                log.info("Initialized OpenTelemetry with TracedInExporter to endpoint: {}", properties.getEndpoint());
                TracedInExporter tracedInExporter = new TracedInExporter(properties.getEndpoint());
                yield SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(tracedInExporter).build())
                        .setResource(resource)
                        .setSampler(sampler)
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
