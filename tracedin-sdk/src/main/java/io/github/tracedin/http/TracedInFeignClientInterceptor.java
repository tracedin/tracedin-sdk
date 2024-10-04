package io.github.tracedin.http;

import feign.RequestInterceptor;
import feign.RequestTemplate;
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

@Slf4j
public class TracedInFeignClientInterceptor implements RequestInterceptor {

    private final Tracer tracer;

    public TracedInFeignClientInterceptor(TracedInProperties properties) {
        this.tracer = GlobalOpenTelemetry.getTracer(properties.getBasePackage());
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 클라이언트 스팬 생성
        Span span = tracer.spanBuilder(requestTemplate.method() + " " + requestTemplate.url())
                .setSpanKind(SpanKind.CLIENT)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // 컨텍스트를 헤더에 주입
            TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
            propagator.inject(Context.current(), requestTemplate, RequestTemplate::header);

            log.info("Request: {}", requestTemplate.request().headers());

            // 추가적인 속성 설정 가능
            span.setAttribute("span.type", "http");
            span.setAttribute("http.method", requestTemplate.method());
            span.setAttribute("http.url", requestTemplate.url());
            span.setAttribute("http.server_ip", requestTemplate.feignTarget().url());

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error in Feign Client Interceptor");
            throw e;
        } finally {
            span.end();
        }
    }
}

