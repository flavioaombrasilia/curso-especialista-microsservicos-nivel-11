package com.algaworks.algadelivery.delivery.tracking.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryStatusTest {

    @Test
    void draft_canChangeToWaitingForCourier() {
        assertThat(DeliveryStatus.DRAFT.canChangeTo(DeliveryStatus.WAITING_FOR_COURIER)).isTrue();
    }

    @Test
    void draft_canNotChangeToInTransit() {
        assertThat(DeliveryStatus.DRAFT.canNotChangeTo(DeliveryStatus.IN_TRANSIT)).isTrue();
    }

    @Test
    void draft_canNotChangeToDelivered() {
        assertThat(DeliveryStatus.DRAFT.canNotChangeTo(DeliveryStatus.DELIVERED)).isTrue();
    }

    @Test
    void waitingForCourier_canChangeToInTransit() {
        assertThat(DeliveryStatus.WAITING_FOR_COURIER.canChangeTo(DeliveryStatus.IN_TRANSIT)).isTrue();
    }

    @Test
    void waitingForCourier_canNotChangeToDraft() {
        assertThat(DeliveryStatus.WAITING_FOR_COURIER.canNotChangeTo(DeliveryStatus.DRAFT)).isTrue();
    }

    @Test
    void waitingForCourier_canNotChangeToDelivered() {
        assertThat(DeliveryStatus.WAITING_FOR_COURIER.canNotChangeTo(DeliveryStatus.DELIVERED)).isTrue();
    }

    @Test
    void inTransit_canChangeToDelivered() {
        assertThat(DeliveryStatus.IN_TRANSIT.canChangeTo(DeliveryStatus.DELIVERED)).isTrue();
    }

    @Test
    void inTransit_canNotChangeToDraft() {
        assertThat(DeliveryStatus.IN_TRANSIT.canNotChangeTo(DeliveryStatus.DRAFT)).isTrue();
    }

    @Test
    void inTransit_canNotChangeToWaitingForCourier() {
        assertThat(DeliveryStatus.IN_TRANSIT.canNotChangeTo(DeliveryStatus.WAITING_FOR_COURIER)).isTrue();
    }

    @Test
    void delivered_canNotChangeToAnyStatus() {
        assertThat(DeliveryStatus.DELIVERED.canNotChangeTo(DeliveryStatus.DRAFT)).isTrue();
        assertThat(DeliveryStatus.DELIVERED.canNotChangeTo(DeliveryStatus.WAITING_FOR_COURIER)).isTrue();
        assertThat(DeliveryStatus.DELIVERED.canNotChangeTo(DeliveryStatus.IN_TRANSIT)).isTrue();
    }
}