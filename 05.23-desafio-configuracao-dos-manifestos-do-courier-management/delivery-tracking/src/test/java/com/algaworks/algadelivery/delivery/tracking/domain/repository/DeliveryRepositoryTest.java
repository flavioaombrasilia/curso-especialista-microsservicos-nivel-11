package com.algaworks.algadelivery.delivery.tracking.domain.repository;

import com.algaworks.algadelivery.delivery.tracking.domain.model.ContactPoint;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import com.algaworks.algadelivery.delivery.tracking.domain.model.DeliveryStatus;
import com.algaworks.algadelivery.delivery.tracking.infrastructure.persistence.AbstractPersistenceIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryRepositoryTest extends AbstractPersistenceIT {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    void shouldSaveDeliveryWithItems() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.addItem("Computador", 2);
        delivery.addItem("Notebook", 3);

        Delivery savedDelivery = deliveryRepository.saveAndFlush(delivery);

        assertThat(savedDelivery.getId()).isNotNull();
        assertThat(savedDelivery.getItems()).hasSize(2);
        assertThat(savedDelivery.getTotalItems()).isEqualTo(5);
    }

    @Test
    void shouldFindDeliveryById() {
        Delivery delivery = createAndSaveDelivery();

        Optional<Delivery> foundDelivery = deliveryRepository.findById(delivery.getId());

        assertThat(foundDelivery).isPresent();
        assertThat(foundDelivery.get().getId()).isEqualTo(delivery.getId());
        assertThat(foundDelivery.get().getSender().getName()).isEqualTo("João Silva");
        assertThat(foundDelivery.get().getRecipient().getName()).isEqualTo("Maria Silva");
    }

    @Test
    void shouldReturnEmptyWhenDeliveryNotFound() {
        UUID randomId = UUID.randomUUID();

        Optional<Delivery> foundDelivery = deliveryRepository.findById(randomId);

        assertThat(foundDelivery).isEmpty();
    }

    @Test
    void shouldUpdateDeliveryStatus() {
        Delivery delivery = createAndSaveDelivery();
        delivery.place();

        deliveryRepository.saveAndFlush(delivery);
        Delivery updatedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_FOR_COURIER);
        assertThat(updatedDelivery.getPlacedAt()).isNotNull();
    }

    @Test
    void shouldDeleteDelivery() {
        Delivery delivery = createAndSaveDelivery();
        UUID deliveryId = delivery.getId();

        deliveryRepository.delete(delivery);
        deliveryRepository.flush();

        Optional<Delivery> deletedDelivery = deliveryRepository.findById(deliveryId);
        assertThat(deletedDelivery).isEmpty();
    }

    @Test
    void shouldFindAllDeliveries() {
        Delivery delivery1 = createAndSaveDelivery();
        Delivery delivery2 = createAndSaveDelivery();

        List<Delivery> deliveries = deliveryRepository.findAll();

        assertThat(deliveries.size()).isGreaterThanOrEqualTo(2);
        assertThat(deliveries).anyMatch(d -> d.getId().equals(delivery1.getId()));
        assertThat(deliveries).anyMatch(d -> d.getId().equals(delivery2.getId()));
    }

    @Test
    void shouldCountDeliveries() {
        deliveryRepository.deleteAll();
        createAndSaveDelivery();
        createAndSaveDelivery();
        createAndSaveDelivery();

        long count = deliveryRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldCheckIfDeliveryExists() {
        Delivery delivery = createAndSaveDelivery();

        boolean exists = deliveryRepository.existsById(delivery.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDeliveryDoesNotExist() {
        UUID randomId = UUID.randomUUID();

        boolean exists = deliveryRepository.existsById(randomId);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldPersistDeliveryWithCourierAssigned() {
        Delivery delivery = createAndSaveDelivery();
        delivery.place();
        UUID courierId = UUID.randomUUID();
        delivery.pickUp(courierId);

        deliveryRepository.saveAndFlush(delivery);
        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(persistedDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        assertThat(persistedDelivery.getCourierId()).isEqualTo(courierId);
        assertThat(persistedDelivery.getAssignedAt()).isNotNull();
    }

    @Test
    void shouldPersistCompleteDeliveryLifecycle() {
        Delivery delivery = createAndSaveDelivery();
        
        // Place
        delivery.place();
        deliveryRepository.saveAndFlush(delivery);
        
        // Pick up
        UUID courierId = UUID.randomUUID();
        delivery.pickUp(courierId);
        deliveryRepository.saveAndFlush(delivery);
        
        // Mark as delivered
        delivery.markAsDelivered();
        deliveryRepository.saveAndFlush(delivery);

        Delivery finalDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(finalDelivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(finalDelivery.getPlacedAt()).isNotNull();
        assertThat(finalDelivery.getAssignedAt()).isNotNull();
        assertThat(finalDelivery.getFulfilledAt()).isNotNull();
        assertThat(finalDelivery.getCourierId()).isEqualTo(courierId);
    }

    @Test
    void shouldPersistDeliveryWithMultipleItems() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        
        delivery.addItem("Pizza Margherita", 2);
        delivery.addItem("Pizza Calabresa", 1);
        delivery.addItem("Refrigerante 2L", 3);
        delivery.addItem("Água", 2);

        deliveryRepository.saveAndFlush(delivery);
        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(persistedDelivery.getItems()).hasSize(4);
        assertThat(persistedDelivery.getTotalItems()).isEqualTo(8);
    }

    @Test
    void shouldPersistDeliveryWithRemovedItems() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        
        UUID itemId1 = delivery.addItem("Pizza Margherita", 2);
        UUID itemId2 = delivery.addItem("Pizza Calabresa", 1);
        delivery.addItem("Refrigerante 2L", 3);

        delivery.removeItem(itemId1);
        delivery.removeItem(itemId2);

        deliveryRepository.saveAndFlush(delivery);
        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(persistedDelivery.getItems()).hasSize(1);
        assertThat(persistedDelivery.getTotalItems()).isEqualTo(3);
    }

    @Test
    void shouldPersistDeliveryWithUpdatedItemQuantity() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        
        UUID itemId = delivery.addItem("Pizza Margherita", 2);
        delivery.changeItemQuantity(itemId, 5);

        deliveryRepository.saveAndFlush(delivery);
        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(persistedDelivery.getItems()).hasSize(1);
        assertThat(persistedDelivery.getTotalItems()).isEqualTo(5);
        assertThat(persistedDelivery.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldPersistDeliveryWithTotalCostCalculation() {
        Delivery delivery = Delivery.draft();
        
        ContactPoint sender = createSender();
        ContactPoint recipient = createRecipient();
        
        BigDecimal distanceFee = new BigDecimal("25.50");
        BigDecimal courierPayout = new BigDecimal("10.75");
        
        Delivery.PreparationDetails details = Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(distanceFee)
                .courierPayout(courierPayout)
                .expectedDeliveryTime(Duration.ofHours(3))
                .build();
        
        delivery.editPreparationDetails(details);

        deliveryRepository.saveAndFlush(delivery);
        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertThat(persistedDelivery.getTotalCost()).isEqualByComparingTo(new BigDecimal("36.25"));
        assertThat(persistedDelivery.getDistanceFee()).isEqualByComparingTo(distanceFee);
        assertThat(persistedDelivery.getCourierPayout()).isEqualByComparingTo(courierPayout);
    }

    private Delivery createAndSaveDelivery() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.addItem("Computador", 2);
        delivery.addItem("Mouse", 1);
        return deliveryRepository.saveAndFlush(delivery);
    }

    private Delivery.PreparationDetails createdValidPreparationDetails() {
        return Delivery.PreparationDetails.builder()
                .sender(createSender())
                .recipient(createRecipient())
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal("5.00"))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }

    private ContactPoint createSender() {
        return ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua São Paulo")
                .number("100")
                .complement("Sala 401")
                .name("João Silva")
                .phone("(11) 90000-1234")
                .build();
    }

    private ContactPoint createRecipient() {
        return ContactPoint.builder()
                .zipCode("12331-342")
                .street("Rua Brasil")
                .number("500")
                .complement("")
                .name("Maria Silva")
                .phone("(11) 91345-1332")
                .build();
    }
}