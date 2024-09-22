package com.univ.tracedinsdk.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TracedInExporter implements SpanExporter {

    private final String endpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    public TracedInExporter(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        CompletableResultCode resultCode = new CompletableResultCode();
        CompletableFuture.runAsync(() -> {
            try {
                for (SpanData span : spans) {
                    // SpanData를 JSON으로 직렬화
                    String json = serializeSpanDataToJson(span);

                    // HTTP 클라이언트를 사용하여 서버로 전송
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(endpoint))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    // 응답 처리
                    if (response.statusCode() != 200) {
                        resultCode.fail();
                        return;
                    }
                }
                resultCode.succeed();
            } catch (Exception e) {
                e.printStackTrace();
                resultCode.fail();
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
        return CompletableResultCode.ofSuccess();
    }

    private String serializeSpanDataToJson(SpanData spanData) throws Exception {
        // 필요한 데이터만 추출하여 JSON으로 변환
        return objectMapper.writeValueAsString(spanData);
    }
}
