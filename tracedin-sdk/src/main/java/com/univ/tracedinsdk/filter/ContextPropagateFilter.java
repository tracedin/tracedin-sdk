package com.univ.tracedinsdk.filter;

import com.univ.tracedinsdk.OpenTelemetryInitializer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
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
import org.springframework.web.filter.OncePerRequestFilter;

public class ContextPropagateFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public ContextPropagateFilter() {
        this.tracer = OpenTelemetryInitializer.getOpenTelemetry()
                .getTracer("com.univ.tracedinsdk");;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 요청으로부터 컨텍스트 추출
        Context extractedContext = getExtract(request);

        // 추출한 컨텍스트를 부모로 하는 새로운 스팬 생성
        Span span = tracer.spanBuilder(request.getRequestURI())
                .setParent(extractedContext)
                .startSpan();

        // 스팬을 현재 컨텍스트로 설정
        try (Scope scope = span.makeCurrent()) {
            // 스팬에 속성 추가
            span.setAttribute("http.method", request.getMethod());
            span.setAttribute("http.url", request.getRequestURL().toString());

            // 응답 시간을 측정하기 위한 시작 시간 기록
            long startTime = System.currentTimeMillis();

            // 필터 체인 실행
            filterChain.doFilter(request, response);

            // 응답 시간 계산
            long duration = System.currentTimeMillis() - startTime;

            // 응답 관련 속성 추가
            span.setAttribute("http.status_code", response.getStatus());
            span.setAttribute("http.client_ip", request.getRemoteAddr());
            span.setAttribute("http.response_time_ms", duration);
        } catch (Exception ex) {
            // 예외 발생 시 스팬에 예외 정보 기록
            span.recordException(ex);
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, ex.getMessage());
            throw ex;
        } finally {
            // 스팬 종료
            span.end();
        }
    }

    private static Context getExtract(HttpServletRequest request) {

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
