package io.github.tracedin.config;

import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingConsumerInterceptor;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {

    public KafkaConfig() {
        log.info("Initializing OpenTelemetry Kafka");
    }

    @Bean
    public <K, V> KafkaTemplate<K, V> kafkaTemplate(ProducerFactory<K, V> pf) {
        KafkaTemplate<K, V> kafkaTemplate = new KafkaTemplate<>(pf);
        kafkaTemplate.setProducerInterceptor(new TracingProducerInterceptor<>());
        return kafkaTemplate;
    }

    @Bean
    public <K, V> ConcurrentKafkaListenerContainerFactory<K, V> kafkaListenerContainerFactory(ConsumerFactory<K, V> cf) {
        Map<String, Object> additionalConfiguration = Map.of(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());
        cf.updateConfigs(additionalConfiguration);

        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
