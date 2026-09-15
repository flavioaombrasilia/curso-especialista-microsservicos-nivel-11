package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DeliveryTest {

    @Test
    void shouldCreateDraftDelivery() {
        Delivery delivery = Delivery.draft();

        assertThat(delivery.getId()).isNotNull();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DRAFT);
        assertThat(delivery.getTotalItems()).isZero();
        assertThat(delivery.getTotalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(delivery.getCourierPayout()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(delivery.getDistanceFee()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldAddItem() {
        Delivery delivery = Delivery.draft();

        UUID itemId = delivery.addItem("Pizza Margherita", 2);

        assertThat(itemId).isNotNull();
        assertThat(delivery.getItems()).hasSize(1);
        assertThat(delivery.getTotalItems()).isEqualTo(2);
    }

    @Test
    void shouldAddMultipleItems() {
        Delivery delivery = Delivery.draft();

        delivery.addItem("Pizza Margherita", 2);
        delivery.addItem("Refrigerante", 3);

        assertThat(delivery.getItems()).hasSize(2);
        assertThat(delivery.getTotalItems()).isEqualTo(5);
    }

    @Test
    void shouldRemoveItem() {
        Delivery delivery = Delivery.draft();
        UUID itemId = delivery.addItem("Pizza Margherita", 2);

        delivery.removeItem(itemId);

        assertThat(delivery.getItems()).isEmpty();
        assertThat(delivery.getTotalItems()).isZero();
    }

    @Test
    void shouldChangeItemQuantity() {
        Delivery delivery = Delivery.draft();
        UUID itemId = delivery.addItem("Pizza Margherita", 2);

        delivery.changeItemQuantity(itemId, 5);

        assertThat(delivery.getTotalItems()).isEqualTo(5);
        assertThat(delivery.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRemoveAllItems() {
        Delivery delivery = Delivery.draft();
        delivery.addItem("Pizza Margherita", 2);
        delivery.addItem("Refrigerante", 3);

        delivery.removeItems();

        assertThat(delivery.getItems()).isEmpty();
        assertThat(delivery.getTotalItems()).isZero();
    }

    @Test
    void shouldEditPreparationDetails() {
        Delivery delivery = Delivery.draft();
        Delivery.PreparationDetails details = createdValidPreparationDetails();

        delivery.editPreparationDetails(details);

        assertThat(delivery.getSender().getName()).isEqualTo("João Silva");
        assertThat(delivery.getRecipient().getName()).isEqualTo("Maria Silva");
        assertThat(delivery.getDistanceFee()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(delivery.getCourierPayout()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(delivery.getTotalCost()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(delivery.getExpectedDeliveryAt()).isNotNull();
    }

    @Test
    void shouldNotEditPreparationDetailsWhenNotDraft() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.place();

        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> delivery.editPreparationDetails(createdValidPreparationDetails()));
    }

    @Test
    void shouldChangeToPlaced() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());

        delivery.place();

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_FOR_COURIER);
        assertThat(delivery.getPlacedAt()).isNotNull();
    }

    @Test
    void shouldNotPlaceWithoutPreparationDetails() {
        Delivery delivery = Delivery.draft();

        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> delivery.place());

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DRAFT);
        assertThat(delivery.getPlacedAt()).isNull();
    }

    @Test
    void shouldNotPlaceWhenAlreadyPlaced() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.place();

        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> delivery.place());
    }

    @Test
    void shouldPickUp() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.place();
        UUID courierId = UUID.randomUUID();

        delivery.pickUp(courierId);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        assertThat(delivery.getCourierId()).isEqualTo(courierId);
        assertThat(delivery.getAssignedAt()).isNotNull();
    }

    @Test
    void shouldNotPickUpWithInvalidStatusTransition() {
        Delivery delivery = Delivery.draft();
        UUID courierId = UUID.randomUUID();

        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> delivery.pickUp(courierId));

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DRAFT);
    }

    @Test
    void shouldMarkAsDelivered() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.place();
        delivery.pickUp(UUID.randomUUID());

        delivery.markAsDelivered();

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getFulfilledAt()).isNotNull();
    }

    @Test
    void shouldNotMarkAsDeliveredWithInvalidStatusTransition() {
        Delivery delivery = Delivery.draft();
        delivery.editPreparationDetails(createdValidPreparationDetails());
        delivery.place();

        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> delivery.markAsDelivered());

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_FOR_COURIER);
    }

    @Test
    void shouldReturnUnmodifiableItemsList() {
        Delivery delivery = Delivery.draft();
        delivery.addItem("Pizza Margherita", 2);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> delivery.getItems().clear());
    }

    private Delivery.PreparationDetails createdValidPreparationDetails() {
        ContactPoint sender = ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua São Paulo")
                .number("100")
                .complement("Sala 401")
                .name("João Silva")
                .phone("(11) 90000-1234")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("12331-342")
                .street("Rua Brasil")
                .number("500")
                .complement("")
                .name("Maria Silva")
                .phone("(11) 91345-1332")
                .build();

        return Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal("5.00"))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }
}