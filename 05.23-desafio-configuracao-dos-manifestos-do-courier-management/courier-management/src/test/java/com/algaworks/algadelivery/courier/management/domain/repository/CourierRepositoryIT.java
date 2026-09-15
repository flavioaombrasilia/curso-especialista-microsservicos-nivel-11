package com.algaworks.algadelivery.courier.management.domain.repository;

import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.infrastructure.persistence.AbstractPersistenceIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourierRepositoryIT extends AbstractPersistenceIT {

    @Autowired
    private CourierRepository courierRepository;

    @Test
    void shouldPersistCourierWithItsAssignedDeliveries() {
        Courier courier = Courier.brandNew("João da Silva", "11999998888");
        UUID deliveryId = UUID.randomUUID();
        courier.assign(deliveryId);

        courierRepository.saveAndFlush(courier);

        Courier found = courierRepository.findById(courier.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("João da Silva");
        assertThat(found.getPendingDeliveriesQuantity()).isEqualTo(1);
        assertThat(found.getPendingDeliveries())
                .extracting(d -> d.getId())
                .containsExactly(deliveryId);
    }

    @Test
    void shouldFindCourierByTheDeliveryAssignedToHim() {
        Courier courier = Courier.brandNew("Maria Souza", "11912341234");
        UUID deliveryId = UUID.randomUUID();
        courier.assign(deliveryId);
        courierRepository.saveAndFlush(courier);

        Optional<Courier> found = courierRepository.findByPendingDeliveries_id(deliveryId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(courier.getId());
    }

    @Test
    void shouldNotFindCourierWhenDeliveryWasAlreadyFulfilled() {
        Courier courier = Courier.brandNew("Maria Souza", "11912341234");
        UUID deliveryId = UUID.randomUUID();
        courier.assign(deliveryId);
        courierRepository.saveAndFlush(courier);

        courier.fulfill(deliveryId);
        courierRepository.saveAndFlush(courier);

        assertThat(courierRepository.findByPendingDeliveries_id(deliveryId)).isEmpty();
    }

    @Test
    void shouldReturnTheCourierWhoFulfilledADeliveryLongestAgo() {
        Courier older = fulfilledCourier("Antigo", "11900000001");
        Courier newer = fulfilledCourier("Recente", "11900000002");

        assertThat(older.getLastFulfilledDeliveryAt())
                .isBefore(newer.getLastFulfilledDeliveryAt());

        Courier idlest = courierRepository
                .findTop1ByOrderByLastFulfilledDeliveryAtAsc()
                .orElseThrow();

        assertThat(idlest.getId()).isEqualTo(older.getId());
    }

    private Courier fulfilledCourier(String name, String phone) {
        Courier courier = Courier.brandNew(name, phone);
        UUID deliveryId = UUID.randomUUID();
        courier.assign(deliveryId);
        courier.fulfill(deliveryId);
        courierRepository.saveAndFlush(courier);
        sleepBriefly();
        return courier;
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
