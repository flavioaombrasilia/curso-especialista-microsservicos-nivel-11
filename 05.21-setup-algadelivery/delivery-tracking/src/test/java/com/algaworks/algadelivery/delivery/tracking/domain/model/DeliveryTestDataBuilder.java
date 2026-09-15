package com.algaworks.algadelivery.delivery.tracking.domain.model;

import java.math.BigDecimal;
import java.time.Duration;

public class DeliveryTestDataBuilder {

    private DeliveryStatus status = DeliveryStatus.DRAFT;
    private boolean withItems = true;
    private boolean withPreparationDetails = false;

    private DeliveryTestDataBuilder() {
    }

    public static DeliveryTestDataBuilder aDelivery() {
        return new DeliveryTestDataBuilder();
    }

    public Delivery build() {
        Delivery delivery = Delivery.draft();

        if (withItems) {
            delivery.addItem("Pizza Margherita", 2);
            delivery.addItem("Refrigerante", 1);
        }

        if (withPreparationDetails) {
            delivery.editPreparationDetails(buildPreparationDetails());
        }

        switch (this.status) {
            case DRAFT -> {
            }
            case WAITING_FOR_COURIER -> {
                if (!withPreparationDetails) {
                    delivery.editPreparationDetails(buildPreparationDetails());
                }
                delivery.place();
            }
            case IN_TRANSIT -> {
                if (!withPreparationDetails) {
                    delivery.editPreparationDetails(buildPreparationDetails());
                }
                delivery.place();
                delivery.pickUp(java.util.UUID.randomUUID());
            }
            case DELIVERED -> {
                if (!withPreparationDetails) {
                    delivery.editPreparationDetails(buildPreparationDetails());
                }
                delivery.place();
                delivery.pickUp(java.util.UUID.randomUUID());
                delivery.markAsDelivered();
            }
        }

        return delivery;
    }

    public static Delivery.PreparationDetails buildPreparationDetails() {
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

    public static ContactPoint buildSender() {
        return ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua São Paulo")
                .number("100")
                .complement("Sala 401")
                .name("João Silva")
                .phone("(11) 90000-1234")
                .build();
    }

    public static ContactPoint buildRecipient() {
        return ContactPoint.builder()
                .zipCode("12331-342")
                .street("Rua Brasil")
                .number("500")
                .complement("")
                .name("Maria Silva")
                .phone("(11) 91345-1332")
                .build();
    }

    public DeliveryTestDataBuilder status(DeliveryStatus status) {
        this.status = status;
        return this;
    }

    public DeliveryTestDataBuilder withItems(boolean withItems) {
        this.withItems = withItems;
        return this;
    }

    public DeliveryTestDataBuilder withPreparationDetails(boolean withPreparationDetails) {
        this.withPreparationDetails = withPreparationDetails;
        return this;
    }

}
