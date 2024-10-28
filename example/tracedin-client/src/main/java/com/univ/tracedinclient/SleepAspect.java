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
        int sleepTime = 100 + random.nextInt(400);
        Thread.sleep(sleepTime);
    }
}
