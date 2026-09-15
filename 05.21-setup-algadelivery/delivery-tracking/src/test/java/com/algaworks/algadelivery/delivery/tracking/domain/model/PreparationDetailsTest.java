package com.algaworks.algadelivery.delivery.tracking.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PreparationDetailsTest {

    @Test
    void shouldCreatePreparationDetailsWithBuilder() {
        ContactPoint sender = ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua São Paulo")
                .number("100")
                .name("João Silva")
                .phone("(11) 90000-1234")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("12331-342")
                .street("Rua Brasil")
                .number("500")
                .name("Maria Silva")
                .phone("(11) 91345-1332")
                .build();

        Delivery.PreparationDetails details = Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal("5.00"))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();

        assertThat(details.getSender()).isEqualTo(sender);
        assertThat(details.getRecipient()).isEqualTo(recipient);
        assertThat(details.getDistanceFee()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(details.getCourierPayout()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(details.getExpectedDeliveryTime()).isEqualTo(Duration.ofHours(5));
    }

    @Test
    void shouldCreatePreparationDetailsWithDifferentDurations() {
        ContactPoint sender = ContactPoint.builder()
                .zipCode("00000-000")
                .street("Rua São Paulo")
                .number("100")
                .name("João Silva")
                .phone("(11) 90000-1234")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("12331-342")
                .street("Rua Brasil")
                .number("500")
                .name("Maria Silva")
                .phone("(11) 91345-1332")
                .build();

        Delivery.PreparationDetails details = Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("20.00"))
                .courierPayout(new BigDecimal("8.00"))
                .expectedDeliveryTime(Duration.ofMinutes(45))
                .build();

        assertThat(details.getExpectedDeliveryTime()).isEqualTo(Duration.ofMinutes(45));
    }
}
