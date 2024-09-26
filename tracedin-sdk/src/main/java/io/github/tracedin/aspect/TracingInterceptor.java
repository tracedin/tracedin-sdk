package io.github.tracedin.aspect;


import io.github.tracedin.OpenTelemetryInitializer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TracingInterceptor implements MethodInterceptor {

    private final Tracer tracer;

    public TracingInterceptor() {
        this.tracer = OpenTelemetryInitializer.getOpenTelemetry()
                .getTracer("com.univ.tracedinsdk");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String className = invocation.getMethod().getDeclaringClass()
                .getSimpleName();
        String methodName = invocation.getMethod().getName();
        String spanName = className + "." + methodName;

        // 스팬 생성 및 부모 스팬 설정
        Span span = tracer.spanBuilder(spanName)
                .setParent(Context.current())
                .startSpan();

        // 메서드 입력 파라미터를 속성으로 추가
        span.setAttribute("method.args", getArgumentsAsString(invocation));

        long startTime = System.currentTimeMillis();

        try (Scope scope = span.makeCurrent()) {
            Object result = invocation.proceed();

            // 메서드 반환값을 속성으로 추가
            span.setAttribute("method.return", result != null ?
                    result.toString() : "null");

            return result;
        } catch (Throwable throwable) {
            // 예외 기록 및 상태 코드 설정
            span.recordException(throwable);
            span.setStatus(StatusCode.ERROR, "Exception in traced method");
            throw throwable;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 메서드 실행 시간을 속성으로 추가
            span.setAttribute("method.duration", duration);

            // 기타 필요한 속성 추가 가능
            span.setAttribute("thread.name", Thread.currentThread().getName());

            span.end();
        }
    }

    private String getArgumentsAsString(MethodInvocation invocation) {
        Object[] args = invocation.getArguments();
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object arg : args) {
            sb.append(arg != null ? arg.toString() : "null").append(", ");
        }
        if (sb.length() > 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("]");
        return sb.toString();
    }
}
