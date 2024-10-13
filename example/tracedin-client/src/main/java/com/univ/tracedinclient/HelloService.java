package com.univ.tracedinclient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;
    private final ServiceClient serviceClient;

    public void hello() {
        String hello = serviceClient.hello();
        String insert = serviceClient.insert();
        System.out.println("Response from API: " + hello);
        System.out.println("Response from API: " + insert);
        helloRepository.hello();
    }

    public void insert() {
        String insert = serviceClient.insert();
        System.out.println("Response from API: " + insert);
    }
}