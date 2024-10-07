package com.univ.tracedinclient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity error(Exception e) {
        return ResponseEntity.status(500).body("Internal Server Error");
    }

}
