package io.github.tracedin.exporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.tracedin.exporter.dto.AppendSpanRequest;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TracedInSpanExporter implements SpanExporter {

    private final String endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    public TracedInSpanExporter(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = createHttpClient();
        this.objectMapper = createObjectMapper();
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
            String json = serializeSpanDataToJson(spans);
            HttpRequest request = buildHttpRequest(json);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Failed to send span. Status code: {}, Response: {}", response.statusCode(), response.body());
                return false;
            }

            log.info("Successfully sent spans: {}", spans.stream().map(SpanData::getSpanId).toList());
            return true;
        } catch (Exception e) {
            log.error("Error while sending spans: {}", spans.stream().map(SpanData::getSpanId).toList() , e);
            return false;
        }
    }

    private HttpRequest buildHttpRequest(String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private String serializeSpanDataToJson(Collection<SpanData> spanData) {
        try {
            List<AppendSpanRequest> requests = spanData.stream().map(AppendSpanRequest::from).toList();
            return objectMapper.writeValueAsString(requests);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

    public static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
