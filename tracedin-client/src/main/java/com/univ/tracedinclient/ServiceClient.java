package com.univ.tracedinclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "service-client",
        url = "http://localhost:8081",
        configuration = FeignConfig.class)
public interface ServiceClient {

    @GetMapping("/hello")
    String hello();
}
