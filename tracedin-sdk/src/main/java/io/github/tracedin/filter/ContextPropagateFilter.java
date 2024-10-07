package io.github.tracedin.filter;

import io.github.tracedin.OpenTelemetryInitializer;
import io.github.tracedin.config.TracedInProperties;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class ContextPropagateFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public ContextPropagateFilter(TracedInProperties properties) {
        this.tracer = OpenTelemetryInitializer.getOpenTelemetry()
                .getTracer(properties.getBasePackage());;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Context extractedContext = getExtract(request);

        Span span = tracer.spanBuilder(request.getRequestURI())
                .setParent(extractedContext)
                .setSpanKind(SpanKind.SERVER) // 스팬 종류 설정
                .startSpan();

        long startTime = System.currentTimeMillis();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("span.type", "http");
            span.setAttribute("http.method", request.getMethod());
            span.setAttribute("http.url", request.getRequestURL().toString());
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            setSpanHttpAttribute(request, response, span, duration);
            span.end();
        }
    }

    private void setSpanHttpAttribute(HttpServletRequest request, HttpServletResponse response,
            Span span, long duration) {
        span.setAttribute("http.status_code", response.getStatus());
        span.setAttribute("http.client_ip", request.getRemoteAddr());
        span.setAttribute("http.response_time_ms", duration);
    }

    private Context getExtract(HttpServletRequest request) {

        TextMapGetter<HttpServletRequest> getter = new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(HttpServletRequest request) {
                return Collections.list(request.getHeaderNames());
            }

            @Override
            public String get(HttpServletRequest carrier, String key) {
                assert carrier != null;
                return carrier.getHeader(key);
            }
        };

        return GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), request, getter);
    }
}
