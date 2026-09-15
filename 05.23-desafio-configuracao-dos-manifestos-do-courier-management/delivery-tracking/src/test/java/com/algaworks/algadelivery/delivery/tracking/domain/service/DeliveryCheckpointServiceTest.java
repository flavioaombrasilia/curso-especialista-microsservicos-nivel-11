package com.algaworks.algadelivery.delivery.tracking.domain.service;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import com.algaworks.algadelivery.delivery.tracking.domain.model.DeliveryStatus;
import com.algaworks.algadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryCheckpointServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryCheckpointService deliveryCheckpointService;

    private Delivery delivery;
    private UUID deliveryId;

    @BeforeEach
    void setUp() {
        deliveryId = UUID.randomUUID();
        delivery = Delivery.draft();
        
        ContactPoint sender = ContactPoint.builder()
                .zipCode("12345-000")
                .street("Rua A")
                .number("100")
                .name("João Silva")
                .phone("11999999999")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("54321-000")
                .street("Rua B")
                .number("200")
                .name("Maria Santos")
                .phone("11988888888")
                .build();

        Delivery.PreparationDetails preparationDetails = Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("31.50"))
                .courierPayout(new BigDecimal("25.00"))
                .expectedDeliveryTime(Duration.ofMinutes(30))
                .build();

        delivery.editPreparationDetails(preparationDetails);
        delivery.addItem("Pizza", 2);
    }

    @Test
    void shouldPlaceDeliverySuccessfully() {
        // Given
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        deliveryCheckpointService.place(deliveryId);

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_FOR_COURIER);
        assertThat(delivery.getPlacedAt()).isNotNull();

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository).saveAndFlush(delivery);
    }

    @Test
    void shouldThrowExceptionWhenPlacingNonExistentDelivery() {
        // Given
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deliveryCheckpointService.place(deliveryId))
                .isInstanceOf(DomainException.class);

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldPickUpDeliverySuccessfully() {
        // Given
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        delivery.place();
        UUID courierId = UUID.randomUUID();

        // When
        deliveryCheckpointService.pickUp(deliveryId, courierId);

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        assertThat(delivery.getCourierId()).isEqualTo(courierId);
        assertThat(delivery.getAssignedAt()).isNotNull();

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository).saveAndFlush(delivery);
    }

    @Test
    void shouldThrowExceptionWhenPickingUpNonExistentDelivery() {
        // Given
        UUID courierId = UUID.randomUUID();
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deliveryCheckpointService.pickUp(deliveryId, courierId))
                .isInstanceOf(DomainException.class);

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCompleteDeliverySuccessfully() {
        // Given
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        delivery.place();
        delivery.pickUp(UUID.randomUUID());

        // When
        deliveryCheckpointService.complete(deliveryId);

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getFulfilledAt()).isNotNull();

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository).saveAndFlush(delivery);
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonExistentDelivery() {
        // Given
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deliveryCheckpointService.complete(deliveryId))
                .isInstanceOf(DomainException.class);

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository, never()).saveAndFlush(any());
    }
}
