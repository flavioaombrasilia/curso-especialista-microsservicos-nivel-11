package com.algaworks.algadelivery.courier.management.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainersKafka {

    private static final ConfluentKafkaContainer kafkaContainer =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Bean
    @ServiceConnection
    public ConfluentKafkaContainer confluentKafkaContainer() {
        return kafkaContainer;
    }

}
