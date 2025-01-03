package io.github.tracedin;

import io.github.tracedin.config.TracedInProperties;
import io.github.tracedin.exporter.LoggingSpanExporter;
import io.github.tracedin.exporter.TracedInMetricExporter;
import io.github.tracedin.exporter.TracedInSpanExporter;
import io.github.tracedin.exporter.grpc.GrpcClient;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OpenTelemetryInitializer {

    public static void initialize(TracedInProperties properties) {
        if (!properties.isEnabled()) return;

        if (properties.isDebug()) {
            setUpLoggingExporter();
        } else {
            setUpTracedinExporter(properties);
        }
    }

    private static void setUpTracedinExporter(TracedInProperties properties) {
        GrpcClient grpcClient = new GrpcClient(properties.getHost());
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.builder()
                        .put("service.name", properties.getServiceName())
                        .put("project.key", properties.getProjectKey())
                        .build()));

        TracedInSpanExporter tracedInSpanExporter = new TracedInSpanExporter(grpcClient);

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(tracedInSpanExporter).build())
                .setResource(resource)
                .setSampler(Sampler.traceIdRatioBased(properties.getSampling()))
                .build();

        TracedInMetricExporter tracedInMetricExporter = new TracedInMetricExporter(grpcClient);

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(tracedInMetricExporter)
                        .setInterval(Duration.ofSeconds(properties.getMetricInterval()))
                        .build())
                .build();

        OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal();
    }

    private static void setUpLoggingExporter() {
        Resource resource = Resource.getDefault();

        log.info("Initialized OpenTelemetry with LoggingSpanExporter");
        LoggingSpanExporter loggingExporter = new LoggingSpanExporter();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(loggingExporter).build())
                .setResource(resource)
                .build();

        OpenTelemetrySdk.builder()
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();
    }

    public static OpenTelemetry getOpenTelemetry() {
        return GlobalOpenTelemetry.get();
    }

}
