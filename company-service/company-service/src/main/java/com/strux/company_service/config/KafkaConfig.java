package com.strux.company_service.config;

import com.strux.company_service.event.WorkerAssignedToProjectEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, com.strux.company_service.event.WorkerAssignedToProjectEvent> workerAssignedConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "company-service");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);

        // Güvenli paketler
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.strux.*");

        // *** KRİTİK: Type header'ı YOK SAY ve default type ver ***
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.strux.company_service.event.WorkerAssignedToProjectEvent");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "workerAssignedFactory")
    public ConcurrentKafkaListenerContainerFactory<String, WorkerAssignedToProjectEvent>
    workerAssignedFactory(
            ConsumerFactory<String, WorkerAssignedToProjectEvent> cf) {

        ConcurrentKafkaListenerContainerFactory<String, com.strux.company_service.event.WorkerAssignedToProjectEvent> f =
                new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }
}

