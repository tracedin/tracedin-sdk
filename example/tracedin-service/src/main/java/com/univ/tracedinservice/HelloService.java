package com.univ.tracedinservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;

    public void hello() {
        helloRepository.createTable();
    }

    public void insert() {
        helloRepository.insertData();
    }

}
