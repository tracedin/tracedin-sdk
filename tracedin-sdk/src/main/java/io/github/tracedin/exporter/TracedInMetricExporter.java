package io.github.tracedin.exporter;

import static io.github.tracedin.exporter.TracedInSpanExporter.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tracedin.exporter.dto.AppendServiceMetricsRequest;
import io.github.tracedin.exporter.dto.MetricRequest;
import io.github.tracedin.exporter.grpc.GrpcClient;
import io.github.tracedin.exporter.grpc.ServiceMetricsProto;
import io.github.tracedin.exporter.grpc.ServiceMetricsProto.AppendServiceMetricsResponse;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TracedInMetricExporter implements MetricExporter {

    private final GrpcClient grpcClient;
    private final ExecutorService executorService;

    public TracedInMetricExporter(GrpcClient grpcClient) {
        this.grpcClient = grpcClient;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {
        CompletableResultCode resultCode = new CompletableResultCode();
        CompletableFuture.runAsync(() -> {
            boolean success = sendSpan(metrics);

            if (success) {
                resultCode.succeed();
                log.info("Successfully exported all metrics.");
            } else {
                resultCode.fail();
                log.error("Failed to export some metrics.");
            }
        }, executorService);
        return resultCode;
    }

    @Override
    public CompletableResultCode flush() {
        // 필요한 경우 flush 로직 구현
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        executorService.shutdown();
        log.info("Metric Exporter ExecutorService has been shut down.");
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return switch (instrumentType) {
            case COUNTER -> AggregationTemporality.DELTA;
            case UP_DOWN_COUNTER, OBSERVABLE_COUNTER, OBSERVABLE_UP_DOWN_COUNTER,
                 OBSERVABLE_GAUGE -> AggregationTemporality.CUMULATIVE;
            case HISTOGRAM ->
                // 필요에 따라 CUMULATIVE 또는 DELTA 선택
                    AggregationTemporality.CUMULATIVE;
            default ->
                // 기본값 설정 (CUMULATIVE 권장)
                    AggregationTemporality.CUMULATIVE;
        };
    }

    private boolean sendSpan(Collection<MetricData> metrics) {
        try {
            ServiceMetricsProto.AppendServiceMetricsRequest request = toGrpcServiceMetrics(metrics);
            AppendServiceMetricsResponse response = grpcClient.appendServiceMetrics(request);

            if (response.getStatusCode() != 200) {
                log.error("Failed to send span. Status code: {}", response.getStatusCode());
                return false;
            }
            log.info("Successfully sent metrics");
            return true;
        } catch (Exception e) {
            log.error("Error while sending metrics", e);
            return false;
        }
    }

    private ServiceMetricsProto.AppendServiceMetricsRequest toGrpcServiceMetrics(Collection<MetricData> metrics) {
        List<MetricRequest> metricData = metrics.stream().map(MetricRequest::from).toList();
        AppendServiceMetricsRequest requestDto = AppendServiceMetricsRequest.from(metricData);
        return ServiceMetricsProto.AppendServiceMetricsRequest.newBuilder()
                .setProjectKey(requestDto.projectKey())
                .setServiceName(requestDto.serviceName())
                .addAllMetrics(requestDto.metrics().stream().map(MetricRequest::toGrpcMetric).toList())
                .build();
    }
}