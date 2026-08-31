package com.algaworks.algadelivery.courier.management.domain.service;

import com.algaworks.algadelivery.courier.management.api.model.CourierInput;
import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.domain.repository.CourierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CourierRegistrationServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierRegistrationService courierRegistrationService;

    @Test
    void shouldPersistBrandNewCourier() {
        when(courierRepository.saveAndFlush(any(Courier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Courier courier = courierRegistrationService.create(input("João da Silva", "11999998888"));

        ArgumentCaptor<Courier> captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).saveAndFlush(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("João da Silva");
        assertThat(captor.getValue().getPendingDeliveriesQuantity()).isZero();
        assertThat(courier.getId()).isNotNull();
    }

    @Test
    void shouldUpdateNameAndPhoneOfExistingCourier() {
        Courier existing = Courier.brandNew("João da Silva", "11999998888");
        when(courierRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(courierRepository.saveAndFlush(existing)).thenReturn(existing);

        Courier updated = courierRegistrationService.update(
                existing.getId(), input("João Pereira", "11911112222"));

        assertThat(updated.getName()).isEqualTo("João Pereira");
        assertThat(updated.getPhone()).isEqualTo("11911112222");
    }

    @Test
    void shouldFailWhenUpdatingCourierThatDoesNotExist() {
        UUID unknownId = UUID.randomUUID();
        when(courierRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierRegistrationService.update(unknownId, input("X", "Y")))
                .isInstanceOf(NoSuchElementException.class);

        verify(courierRepository, never()).saveAndFlush(any());
    }

    private CourierInput input(String name, String phone) {
        CourierInput input = new CourierInput();
        input.setName(name);
        input.setPhone(phone);
        return input;
    }
}
