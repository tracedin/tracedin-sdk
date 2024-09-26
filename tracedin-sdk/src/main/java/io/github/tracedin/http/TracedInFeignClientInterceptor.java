package io.github.tracedin.http;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TracedInFeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        TextMapPropagator propagator = GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
        propagator.inject(Context.current(), requestTemplate, RequestTemplate::header);
        log.info("Request: {}", requestTemplate.request().headers());
    }

}
