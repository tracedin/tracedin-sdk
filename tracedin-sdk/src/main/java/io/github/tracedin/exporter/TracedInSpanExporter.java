package io.github.tracedin.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.tracedin.exporter.dto.AppendSpanRequest;
import io.github.tracedin.exporter.grpc.GrpcClient;
import io.github.tracedin.exporter.grpc.SpanProto.AppendSpanResponse;
import io.github.tracedin.exporter.grpc.SpanProto.AppendSpansRequest;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TracedInSpanExporter implements SpanExporter {

    private final GrpcClient grpcClient;
    private final ExecutorService executorService;

    public TracedInSpanExporter(GrpcClient grpcClient) {
        this.grpcClient = grpcClient;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        CompletableResultCode resultCode = new CompletableResultCode();

        CompletableFuture.runAsync(() -> {
            boolean success = sendSpan(spans);

            if (success) {
                resultCode.succeed();
                log.info("Successfully exported all spans.");
            } else {
                resultCode.fail();
                log.error("Failed to export some spans.");
            }
        }, executorService);

        return resultCode;
    }

    private boolean sendSpan(Collection<SpanData> spans) {
        try {
            AppendSpansRequest appendSpansRequest = spanDataToGrpcRequest(spans);

            AppendSpanResponse response = grpcClient.appendSpans(appendSpansRequest);
            if (response.getStatusCode() != 200) {
                log.error("Failed to send span ");
                return false;
            }

            log.info("Successfully sent spans: {}", spans.stream().map(SpanData::getSpanId).toList());
            return true;
        } catch (Exception e) {
            log.error("Error while sending spans: {}", spans.stream().map(SpanData::getSpanId).toList() , e);
            return false;
        }
    }

    private AppendSpansRequest spanDataToGrpcRequest(Collection<SpanData> spanData) {

        List<AppendSpanRequest> requests = spanData.stream().map(AppendSpanRequest::from).toList();

        return AppendSpansRequest.newBuilder()
                .addAllSpans(requests.stream().map(AppendSpanRequest::toGrpcSpan).toList())
                .build();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        executorService.shutdown();
        log.info("ExecutorService has been shut down.");
        return CompletableResultCode.ofSuccess();
    }
}
