package com.univ.tracedinclient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;
    private final RestTemplate restTemplate;
    private final ServiceClient serviceClient;

    public void hello() {
//        String url = "http://localhost:8081/hello";
//        String response = restTemplate.getForObject(url, String.class);
        String hello = serviceClient.hello();

        // 응답 출력
        System.out.println("Response from API: " + hello);
        helloRepository.hello();
    }

}
