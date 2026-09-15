package com.algaworks.algadelivery.delivery.tracking.domain.service;

import com.algaworks.algadelivery.delivery.tracking.api.model.ContactPointInput;
import com.algaworks.algadelivery.delivery.tracking.api.model.DeliveryInput;
import com.algaworks.algadelivery.delivery.tracking.api.model.ItemInput;
import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryPreparationServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryTimeEstimationService deliveryTimeEstimationService;

    @Mock
    private CourierPayoutCalculationService courierPayoutCalculationService;

    @InjectMocks
    private DeliveryPreparationService deliveryPreparationService;

    private DeliveryInput deliveryInput;
    private DeliveryEstimate deliveryEstimate;

    @BeforeEach
    void setUp() {
        ContactPointInput sender = new ContactPointInput();
        sender.setZipCode("12345-000");
        sender.setStreet("Rua A");
        sender.setNumber("100");
        sender.setComplement("Apto 1");
        sender.setName("João Silva");
        sender.setPhone("11999999999");

        ContactPointInput recipient = new ContactPointInput();
        recipient.setZipCode("54321-000");
        recipient.setStreet("Rua B");
        recipient.setNumber("200");
        recipient.setComplement("Casa");
        recipient.setName("Maria Santos");
        recipient.setPhone("11988888888");

        ItemInput item = new ItemInput();
        item.setName("Pizza");
        item.setQuantity(2);

        deliveryInput = new DeliveryInput();
        deliveryInput.setSender(sender);
        deliveryInput.setRecipient(recipient);
        deliveryInput.setItems(List.of(item));

        deliveryEstimate = new DeliveryEstimate(Duration.ofMinutes(30), 10.5);
    }

    @Test
    void shouldDraftDeliverySuccessfully() {
        // Given
        when(deliveryTimeEstimationService.estimate(any(), any())).thenReturn(deliveryEstimate);
        when(courierPayoutCalculationService.calculatePayout(anyDouble())).thenReturn(new BigDecimal("25.00"));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Delivery result = deliveryPreparationService.draft(deliveryInput);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.DRAFT);
        assertThat(result.getSender().getName()).isEqualTo("João Silva");
        assertThat(result.getRecipient().getName()).isEqualTo("Maria Santos");
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getCourierPayout()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.getDistanceFee()).isNotNull();
        assertThat(result.getTotalCost()).isNotNull();
        assertThat(result.getExpectedDeliveryAt()).isNotNull();

        verify(deliveryTimeEstimationService).estimate(any(), any());
        verify(courierPayoutCalculationService).calculatePayout(10.5);
        verify(deliveryRepository).saveAndFlush(any(Delivery.class));
    }

    @Test
    void shouldEditDeliverySuccessfully() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        Delivery existingDelivery = Delivery.draft();
        existingDelivery.addItem("Old Item", 1);

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(existingDelivery));
        when(deliveryTimeEstimationService.estimate(any(), any())).thenReturn(deliveryEstimate);
        when(courierPayoutCalculationService.calculatePayout(anyDouble())).thenReturn(new BigDecimal("25.00"));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Delivery result = deliveryPreparationService.edit(deliveryId, deliveryInput);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Pizza");

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository).saveAndFlush(any(Delivery.class));
    }

    @Test
    void shouldThrowExceptionWhenEditingNonExistentDelivery() {
        // Given
        UUID deliveryId = UUID.randomUUID();
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> deliveryPreparationService.edit(deliveryId, deliveryInput))
                .isInstanceOf(DomainException.class);

        verify(deliveryRepository).findById(deliveryId);
        verify(deliveryRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCalculateDistanceFeeCorrectly() {
        // Given
        when(deliveryTimeEstimationService.estimate(any(), any())).thenReturn(deliveryEstimate);
        when(courierPayoutCalculationService.calculatePayout(anyDouble())).thenReturn(new BigDecimal("25.00"));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Delivery result = deliveryPreparationService.draft(deliveryInput);

        // Then
        // Distance fee should be 3 * 10.5 = 31.50
        assertThat(result.getDistanceFee()).isEqualByComparingTo(new BigDecimal("31.50"));
    }

    @Test
    void shouldCalculateTotalCostCorrectly() {
        // Given
        when(deliveryTimeEstimationService.estimate(any(), any())).thenReturn(deliveryEstimate);
        when(courierPayoutCalculationService.calculatePayout(anyDouble())).thenReturn(new BigDecimal("25.00"));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Delivery result = deliveryPreparationService.draft(deliveryInput);

        // Then
        // Total cost should be distanceFee + courierPayout = 31.50 + 25.00 = 56.50
        assertThat(result.getTotalCost()).isEqualByComparingTo(new BigDecimal("56.50"));
    }

    @Test
    void shouldAddAllItemsFromInput() {
        // Given
        ItemInput item1 = new ItemInput();
        item1.setName("Pizza");
        item1.setQuantity(2);

        ItemInput item2 = new ItemInput();
        item2.setName("Refrigerante");
        item2.setQuantity(3);

        deliveryInput.setItems(List.of(item1, item2));

        when(deliveryTimeEstimationService.estimate(any(), any())).thenReturn(deliveryEstimate);
        when(courierPayoutCalculationService.calculatePayout(anyDouble())).thenReturn(new BigDecimal("25.00"));
        when(deliveryRepository.saveAndFlush(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Delivery result = deliveryPreparationService.draft(deliveryInput);

        // Then
        assertThat(result.getTotalItems()).isEqualTo(5);
        assertThat(result.getItems()).hasSize(2);
    }
}
