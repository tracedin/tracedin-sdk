package io.github.tracedin.aspect;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tracedin.OpenTelemetryInitializer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Map;

public class TracingInterceptor implements MethodInterceptor {

    private final Tracer tracer;

    public TracingInterceptor(String serviceName) {
        this.tracer = OpenTelemetryInitializer.getOpenTelemetry()
                .getTracer(serviceName);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MethodExtractAdapter extractAdapter = new MethodExtractAdapter(invocation);

        Span span = tracer.spanBuilder(extractAdapter.getFullMethodName())
                .setParent(Context.current())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Object result = invocation.proceed();

            Map<String, Object> parameters = extractAdapter.extractParameters();
            addAllParameter(span, parameters);

            span.setAttribute("method.return", result != null ? result.toString() : "null");
            span.setAttribute("method.thread-name", Thread.currentThread().getName());

            return result;
        } catch (Throwable throwable) {
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR, "Exception in traced method");
            throw throwable;
        } finally {
            span.end();
        }
    }

    private void addAllParameter(Span span, Map<String, Object> parameters) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            span.setAttribute("method.args." + entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
        }
    }
}
