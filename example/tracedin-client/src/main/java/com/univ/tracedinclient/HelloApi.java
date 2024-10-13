package com.univ.tracedinclient;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HelloApi {

    private final HelloService helloService;
    private final HelloKafkaService helloKafkaService;

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

    @PostMapping("send-message")
    public String sendMessage(@RequestParam String name) {
        helloKafkaService.hello(name);
        return "Message Sent!";
    }


}
