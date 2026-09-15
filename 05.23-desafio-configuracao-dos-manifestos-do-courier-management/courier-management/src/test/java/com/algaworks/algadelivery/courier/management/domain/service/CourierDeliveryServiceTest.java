package com.algaworks.algadelivery.courier.management.domain.service;

import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.domain.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierDeliveryServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierDeliveryService courierDeliveryService;

    @Test
    void shouldAssignDeliveryToTheIdlestCourier() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");
        UUID deliveryId = UUID.randomUUID();
        when(courierRepository.findTop1ByOrderByLastFulfilledDeliveryAtAsc())
                .thenReturn(Optional.of(courier));

        courierDeliveryService.assign(deliveryId);

        assertThat(courier.getPendingDeliveriesQuantity()).isEqualTo(1);
        assertThat(courier.getPendingDeliveries())
                .extracting(d -> d.getId())
                .containsExactly(deliveryId);
        verify(courierRepository).saveAndFlush(courier);
    }

    @Test
    void shouldFailWhenThereIsNoCourierAvailable() {
        when(courierRepository.findTop1ByOrderByLastFulfilledDeliveryAtAsc())
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierDeliveryService.assign(UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class);

        verify(courierRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldFulfillDeliveryOfTheCourierThatHoldsIt() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");
        UUID deliveryId = UUID.randomUUID();
        courier.assign(deliveryId);
        when(courierRepository.findByPendingDeliveries_id(deliveryId))
                .thenReturn(Optional.of(courier));

        courierDeliveryService.fulfill(deliveryId);

        assertThat(courier.getPendingDeliveriesQuantity()).isZero();
        assertThat(courier.getFulfilledDeliveriesQuantity()).isEqualTo(1);
        assertThat(courier.getLastFulfilledDeliveryAt()).isNotNull();
        verify(courierRepository).saveAndFlush(courier);
    }

    @Test
    void shouldFailWhenFulfillingDeliveryThatNoCourierHolds() {
        UUID deliveryId = UUID.randomUUID();
        when(courierRepository.findByPendingDeliveries_id(deliveryId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierDeliveryService.fulfill(deliveryId))
                .isInstanceOf(NoSuchElementException.class);

        verify(courierRepository, never()).saveAndFlush(any());
    }
}
