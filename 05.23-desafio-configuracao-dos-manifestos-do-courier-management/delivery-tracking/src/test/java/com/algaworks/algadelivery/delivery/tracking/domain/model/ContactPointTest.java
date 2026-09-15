package com.algaworks.algadelivery.delivery.tracking.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContactPointTest {

    @Test
    void shouldCreateContactPointWithBuilder() {
        ContactPoint contactPoint = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Rua Exemplo")
                .number("123")
                .complement("Apto 45")
                .name("João Silva")
                .phone("(11) 99999-8888")
                .build();

        assertThat(contactPoint.getZipCode()).isEqualTo("12345-678");
        assertThat(contactPoint.getStreet()).isEqualTo("Rua Exemplo");
        assertThat(contactPoint.getNumber()).isEqualTo("123");
        assertThat(contactPoint.getComplement()).isEqualTo("Apto 45");
        assertThat(contactPoint.getName()).isEqualTo("João Silva");
        assertThat(contactPoint.getPhone()).isEqualTo("(11) 99999-8888");
    }

    @Test
    void shouldBeEqualWhenSameData() {
        ContactPoint contact1 = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Rua Exemplo")
                .number("123")
                .complement("Apto 45")
                .name("João Silva")
                .phone("(11) 99999-8888")
                .build();

        ContactPoint contact2 = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Rua Exemplo")
                .number("123")
                .complement("Apto 45")
                .name("João Silva")
                .phone("(11) 99999-8888")
                .build();

        assertThat(contact1).isEqualTo(contact2);
        assertThat(contact1.hashCode()).isEqualTo(contact2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentData() {
        ContactPoint contact1 = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Rua Exemplo")
                .number("123")
                .complement("Apto 45")
                .name("João Silva")
                .phone("(11) 99999-8888")
                .build();

        ContactPoint contact2 = ContactPoint.builder()
                .zipCode("98765-432")
                .street("Rua Diferente")
                .number("456")
                .complement("Casa")
                .name("Maria Santos")
                .phone("(11) 88888-7777")
                .build();

        assertThat(contact1).isNotEqualTo(contact2);
    }

    @Test
    void shouldCreateContactPointWithEmptyComplement() {
        ContactPoint contactPoint = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Rua Exemplo")
                .number("123")
                .complement("")
                .name("João Silva")
                .phone("(11) 99999-8888")
                .build();

        assertThat(contactPoint.getComplement()).isEmpty();
    }
}
