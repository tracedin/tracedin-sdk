package com.univ.tracedinservice;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloApi {

    private final HelloService helloService;

    @GetMapping("/hello")
    public String hello() {
        helloService.hello();
        return "Hello, World!";
    }

    @GetMapping("/insert")
    public String insert() {
        helloService.insert();
        return "Data inserted!";
    }

    @KafkaListener(topics = "hello", groupId = "hello-group")
    public void listenHelloMessage(String message) {
        System.out.printf("Hello, My Name is %s%n", message);
    }
}
