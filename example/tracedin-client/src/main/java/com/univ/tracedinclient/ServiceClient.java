package com.univ.tracedinclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "service-client",
        url = "${traced-in.demo.service.url}",
        configuration = FeignConfig.class)
public interface ServiceClient {

    @GetMapping("/hello")
    String hello();

    @GetMapping("/insert")
    String insert();
}
