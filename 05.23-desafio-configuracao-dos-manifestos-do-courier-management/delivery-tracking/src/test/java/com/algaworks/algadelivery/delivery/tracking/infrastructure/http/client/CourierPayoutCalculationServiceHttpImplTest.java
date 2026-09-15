package com.algaworks.algadelivery.delivery.tracking.infrastructure.http.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierPayoutCalculationServiceHttpImplTest {

    @Mock
    private CourierAPIClient courierAPIClient;

    @InjectMocks
    private CourierPayoutCalculationServiceHttpImpl service;

    @Test
    void shouldCalculatePayoutSuccessfully() {
        // Given
        Double distanceInKm = 10.5;
        CourierPayoutResultModel expectedResult = new CourierPayoutResultModel(new BigDecimal("25.00"));

        when(courierAPIClient.payoutCalculation(any(CourierPayoutCalculationInput.class)))
                .thenReturn(expectedResult);

        // When
        BigDecimal result = service.calculatePayout(distanceInKm);

        // Then
        assertThat(result).isEqualByComparingTo(new BigDecimal("25.00"));
        verify(courierAPIClient).payoutCalculation(any(CourierPayoutCalculationInput.class));
    }

    @Test
    void shouldThrowGatewayTimeoutExceptionWhenResourceAccessExceptionOccurs() {
        // Given
        Double distanceInKm = 10.5;
        when(courierAPIClient.payoutCalculation(any(CourierPayoutCalculationInput.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // When/Then
        assertThatThrownBy(() -> service.calculatePayout(distanceInKm))
                .isInstanceOf(GatewayTimeoutException.class)
                .hasCauseInstanceOf(ResourceAccessException.class);

        verify(courierAPIClient).payoutCalculation(any(CourierPayoutCalculationInput.class));
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenHttpServerErrorExceptionOccurs() {
        // Given
        Double distanceInKm = 10.5;
        when(courierAPIClient.payoutCalculation(any(CourierPayoutCalculationInput.class)))
                .thenThrow(HttpServerErrorException.class);

        // When/Then
        assertThatThrownBy(() -> service.calculatePayout(distanceInKm))
                .isInstanceOf(BadGatewayException.class)
                .hasCauseInstanceOf(HttpServerErrorException.class);

        verify(courierAPIClient).payoutCalculation(any(CourierPayoutCalculationInput.class));
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenCallNotPermittedExceptionOccurs() {
        // Given
        Double distanceInKm = 10.5;
        CallNotPermittedException exception = CallNotPermittedException.createCallNotPermittedException(
                io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("test"));
        when(courierAPIClient.payoutCalculation(any(CourierPayoutCalculationInput.class)))
                .thenThrow(exception);

        // When/Then
        assertThatThrownBy(() -> service.calculatePayout(distanceInKm))
                .isInstanceOf(BadGatewayException.class)
                .hasCauseInstanceOf(CallNotPermittedException.class);

        verify(courierAPIClient).payoutCalculation(any(CourierPayoutCalculationInput.class));
    }

    @Test
    void shouldThrowBadGatewayExceptionWhenIllegalArgumentExceptionOccurs() {
        // Given
        Double distanceInKm = 10.5;
        when(courierAPIClient.payoutCalculation(any(CourierPayoutCalculationInput.class)))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // When/Then
        assertThatThrownBy(() -> service.calculatePayout(distanceInKm))
                .isInstanceOf(BadGatewayException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(courierAPIClient).payoutCalculation(any(CourierPayoutCalculationInput.class));
    }
}
