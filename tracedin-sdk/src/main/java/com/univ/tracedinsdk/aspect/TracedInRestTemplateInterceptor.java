package com.univ.tracedinsdk.aspect;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
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

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 현재 컨텍스트 가져오기
        Context currentContext = Context.current();

        // W3C Trace Context 헤더를 추가하기 위한 Propagator 설정
        TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();

        // 요청 헤더에 Trace 정보를 추가
        propagator.inject(currentContext, request.getHeaders(), HttpHeaders::set);

        log.info("Request to {}", request.getHeaders());
        return execution.execute(request, body);
    }

}
