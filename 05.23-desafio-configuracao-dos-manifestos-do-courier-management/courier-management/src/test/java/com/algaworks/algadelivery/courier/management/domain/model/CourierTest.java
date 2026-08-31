package com.algaworks.algadelivery.courier.management.domain.model;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourierTest {

    @Test
    void shouldCreateBrandNewCourierWithZeroedCounters() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");

        assertThat(courier.getId()).isNotNull();
        assertThat(courier.getName()).isEqualTo("João da Silva");
        assertThat(courier.getPhone()).isEqualTo("11999998888");
        assertThat(courier.getPendingDeliveriesQuantity()).isZero();
        assertThat(courier.getFulfilledDeliveriesQuantity()).isZero();
        assertThat(courier.getLastFulfilledDeliveryAt()).isNull();
        assertThat(courier.getPendingDeliveries()).isEmpty();
    }

    @Test
    void shouldIncrementPendingCounterWhenDeliveryIsAssigned() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");
        UUID deliveryId = UUID.randomUUID();

        courier.assign(deliveryId);

        assertThat(courier.getPendingDeliveriesQuantity()).isEqualTo(1);
        assertThat(courier.getPendingDeliveries())
                .extracting(AssignedDelivery::getId)
                .containsExactly(deliveryId);
        assertThat(courier.getPendingDeliveries().get(0).getAssignedAt()).isNotNull();
    }

    @Test
    void shouldMoveDeliveryFromPendingToFulfilled() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");
        UUID deliveryId = UUID.randomUUID();
        courier.assign(deliveryId);

        courier.fulfill(deliveryId);

        assertThat(courier.getPendingDeliveriesQuantity()).isZero();
        assertThat(courier.getFulfilledDeliveriesQuantity()).isEqualTo(1);
        assertThat(courier.getLastFulfilledDeliveryAt()).isNotNull();
        assertThat(courier.getPendingDeliveries()).isEmpty();
    }

    @Test
    void shouldFailWhenFulfillingDeliveryThatWasNeverAssigned() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");

        assertThatThrownBy(() -> courier.fulfill(UUID.randomUUID()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void shouldNotAllowExternalMutationOfPendingDeliveries() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");

        assertThatThrownBy(() -> courier.getPendingDeliveries().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
