package io.github.tracedin.http;

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
        TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
        propagator.inject(Context.current(), request.getHeaders(), HttpHeaders::set);

        log.info("Request: {}", request.getHeaders());
        return execution.execute(request, body);
    }

}
