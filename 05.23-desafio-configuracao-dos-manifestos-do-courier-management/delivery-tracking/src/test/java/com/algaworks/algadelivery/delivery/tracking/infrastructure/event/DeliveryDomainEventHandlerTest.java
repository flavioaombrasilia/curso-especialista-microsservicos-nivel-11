package com.algaworks.algadelivery.delivery.tracking.infrastructure.event;

import com.algaworks.algadelivery.delivery.tracking.domain.event.DeliveryFulfilledEvent;
import com.algaworks.algadelivery.delivery.tracking.domain.event.DeliveryPickUpEvent;
import com.algaworks.algadelivery.delivery.tracking.domain.event.DeliveryPlacedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.algaworks.algadelivery.delivery.tracking.infrastructure.kafka.KafkaTopicConfig.deliveryEventsTopicName;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryDomainEventHandlerTest {

    @Mock
    private IntegrationEventPublisher integrationEventPublisher;

    @InjectMocks
    private DeliveryDomainEventHandler eventHandler;

    @Test
    void shouldHandleDeliveryPlacedEvent() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();
        DeliveryPlacedEvent event = new DeliveryPlacedEvent(timestamp, deliveryId);

        // When
        eventHandler.handle(event);

        // Then
        verify(integrationEventPublisher).publish(event, deliveryId.toString(), deliveryEventsTopicName);
    }

    @Test
    void shouldHandleDeliveryPickUpEvent() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();
        DeliveryPickUpEvent event = new DeliveryPickUpEvent(timestamp, deliveryId);

        // When
        eventHandler.handle(event);

        // Then
        verify(integrationEventPublisher).publish(event, deliveryId.toString(), deliveryEventsTopicName);
    }

    @Test
    void shouldHandleDeliveryFulfilledEvent() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();
        DeliveryFulfilledEvent event = new DeliveryFulfilledEvent(timestamp, deliveryId);

        // When
        eventHandler.handle(event);

        // Then
        verify(integrationEventPublisher).publish(event, deliveryId.toString(), deliveryEventsTopicName);
    }
}
