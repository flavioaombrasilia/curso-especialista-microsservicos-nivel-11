package com.algaworks.algadelivery.courier.management.infrastructure.kafka;

import com.algaworks.algadelivery.courier.management.domain.service.CourierDeliveryService;
import com.algaworks.algadelivery.courier.management.infrastructure.event.DeliveryFulfilledIntegrationEvent;
import com.algaworks.algadelivery.courier.management.infrastructure.event.DeliveryPlacedIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KafkaDeliveriesMessageHandlerTest {

    @Mock
    private CourierDeliveryService courierDeliveryService;

    @InjectMocks
    private KafkaDeliveriesMessageHandler handler;

    @Test
    void shouldAssignCourierWhenDeliveryIsPlaced() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryPlacedIntegrationEvent event = new DeliveryPlacedIntegrationEvent();
        event.setDeliveryId(deliveryId);
        event.setOccurredAt(OffsetDateTime.now());

        handler.handle(event);

        verify(courierDeliveryService).assign(deliveryId);
    }

    @Test
    void shouldFulfillDeliveryWhenDeliveryIsFulfilled() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryFulfilledIntegrationEvent event = new DeliveryFulfilledIntegrationEvent();
        event.setDeliveryId(deliveryId);
        event.setOccurredAt(OffsetDateTime.now());

        handler.handle(event);

        verify(courierDeliveryService).fulfill(deliveryId);
    }

    @Test
    void shouldIgnoreUnknownEventTypes() {
        handler.defaultHandler("delivery-picked-up-event");

        verifyNoInteractions(courierDeliveryService);
    }
}
