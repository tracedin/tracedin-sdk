package com.univ.tracedinclient;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelloKafkaService {
    private static final String TOPIC = "hello";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void hello(String name) {
        ProducerRecord<String, String> payload = new ProducerRecord<>(TOPIC, name);
        kafkaTemplate.send(payload);
    }

}
