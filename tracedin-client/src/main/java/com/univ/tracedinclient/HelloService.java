package com.univ.tracedinclient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;
    private final RestTemplate restTemplate;

    public void hello() {
        // 요청할 URL
        String url = "http://localhost:8081/hello";

        // GET 요청 보내고 응답 받기
        String response = restTemplate.getForObject(url, String.class);

        // 응답 출력
        System.out.println("Response from API: " + response);
        helloRepository.hello();
    }

}
