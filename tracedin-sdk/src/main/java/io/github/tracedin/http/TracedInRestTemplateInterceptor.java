package io.github.tracedin.http;

import io.github.tracedin.config.TracedInProperties;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class TracedInRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    public TracedInRestTemplateInterceptor(TracedInProperties properties) {
        this.tracer = GlobalOpenTelemetry.getTracer(properties.getBasePackage());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 클라이언트 스팬 생성
        Span span = tracer.spanBuilder(request.getMethod() + " " + request.getURI())
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // 컨텍스트를 헤더에 주입
            TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
            propagator.inject(Context.current(), request.getHeaders(), HttpHeaders::set);

            log.info("Request: {}", request.getHeaders());
            span.setAttribute("span.type", "http");
            span.setAttribute("http.server_ip", request.getURI().getHost());

            // 요청 실행
            ClientHttpResponse response = execution.execute(request, body);

            // 응답 정보를 스팬에 기록
            span.setAttribute("http.status_code", response.getStatusCode().value());
            span.setAttribute("http.url", request.getURI().toString());
            span.setAttribute("http.method", request.getMethod().toString());

            return response;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "HTTP request failed");
            throw e;
        } finally {
            span.end();
        }
    }
}
