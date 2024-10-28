package com.univ.tracedinclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class TracedinClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracedinClientApplication.class, args);
    }

}
