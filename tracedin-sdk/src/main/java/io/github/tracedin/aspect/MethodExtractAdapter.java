package io.github.tracedin.aspect;

import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class MethodExtractAdapter {
    private final MethodInvocation methodInvocation;

    public String getFullMethodName() {
        return extractClassName() + '.' + extractMethodName();
    }

    public String extractMethodName() {
        Method method = methodInvocation.getMethod();
        return method.getName();
    }

    public String extractClassName() {
        Method method = methodInvocation.getMethod();
        return method.getDeclaringClass().getSimpleName();
    }

    public Map<String, Object> extractParameters() {
        Object[] arguments = methodInvocation.getArguments();
        Parameter[] parameters = methodInvocation.getMethod().getParameters();

        return IntStream.range(0, parameters.length)
                .boxed()
                .filter(i -> Objects.nonNull(arguments[i]))
                .collect(Collectors.toMap(i -> parameters[i].getName(), i -> arguments[i]));
    }
}
