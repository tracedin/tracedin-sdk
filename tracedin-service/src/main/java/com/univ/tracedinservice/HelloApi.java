package com.univ.tracedinservice;

import lombok.RequiredArgsConstructor;
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


}
