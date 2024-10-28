package com.univ.tracedinclient;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Random;

@Aspect
@Component
public class SleepAspect {

    private final Random random = new Random();

    @Before("execution(* com.univ.tracedinclient..*(..))")
    public void addSleep() throws InterruptedException {
        if (random.nextInt(1_000_00) < 1) { // 0.001% 확률로 이상치
            int sleepTime = 1000 + random.nextInt(1000); // 1000ms~2000ms (1~2초)
            Thread.sleep(sleepTime);
        }
        // 나머지 95% 확률의 경우는 대기 없음
    }
}
