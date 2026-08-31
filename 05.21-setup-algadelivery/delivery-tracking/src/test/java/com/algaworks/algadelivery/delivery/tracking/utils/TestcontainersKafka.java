package com.algaworks.algadelivery.delivery.tracking.utils;

import com.algaworks.algadelivery.delivery.tracking.infrastructure.event.IntegrationEventPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainersKafka {

    private static final ConfluentKafkaContainer kafkaContainer =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Bean
    @Primary
    public IntegrationEventPublisher integrationEventPublisher() {
        return (topic, key, event) -> {
            // Mock implementation - does nothing in tests
        };
    }

    @Bean
    @ServiceConnection
    public ConfluentKafkaContainer confluentKafkaContainer() {
        return kafkaContainer;
    }
     
}
