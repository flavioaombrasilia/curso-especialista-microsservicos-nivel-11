package com.algaworks.algadelivery.delivery.tracking.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void shouldCreateExceptionWithoutMessage() {
        DomainException exception = new DomainException();

        assertThat(exception.getMessage()).isNull();
    }

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Invalid operation";
        DomainException exception = new DomainException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Invalid operation";
        RuntimeException cause = new RuntimeException("Root cause");
        
        DomainException exception = new DomainException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldBeRuntimeException() {
        DomainException exception = new DomainException();

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
