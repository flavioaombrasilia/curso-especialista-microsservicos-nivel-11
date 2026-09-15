package com.algaworks.algadelivery.courier.management.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourierPayoutServiceTest {

    private final CourierPayoutService courierPayoutService = new CourierPayoutService();

    @ParameterizedTest
    @CsvSource({
            "1.0,   10.00",
            "3.5,   35.00",
            "0.0,    0.00",
            "10.0, 100.00"
    })
    void shouldCalculateTenPerKilometer(Double distanceInKm, String expectedPayout) {
        BigDecimal payoutFee = courierPayoutService.calculate(distanceInKm);

        assertThat(payoutFee).isEqualByComparingTo(new BigDecimal(expectedPayout));
    }

    @Test
    void shouldAlwaysReturnTwoDecimalPlaces() {
        BigDecimal payoutFee = courierPayoutService.calculate(2.345);

        assertThat(payoutFee.scale()).isEqualTo(2);
        assertThat(payoutFee).isEqualByComparingTo(new BigDecimal("23.45"));
    }

    @Test
    void shouldFailWhenDistanceIsNull() {
        assertThatThrownBy(() -> courierPayoutService.calculate(null))
                .isInstanceOf(NullPointerException.class);
    }
}
