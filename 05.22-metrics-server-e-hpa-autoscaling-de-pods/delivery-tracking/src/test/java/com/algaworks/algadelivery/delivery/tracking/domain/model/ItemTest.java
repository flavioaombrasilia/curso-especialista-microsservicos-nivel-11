package com.algaworks.algadelivery.delivery.tracking.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    void shouldCreateItemWithBrandNew() {
        Delivery delivery = Delivery.draft();
        
        Item item = Item.brandNew("Pizza Margherita", 2, delivery);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getName()).isEqualTo("Pizza Margherita");
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        Delivery delivery = Delivery.draft();
        Item item1 = Item.brandNew("Pizza Margherita", 2, delivery);
        Item item2 = Item.brandNew("Refrigerante", 3, delivery);

        assertThat(item1).isNotEqualTo(item2);
    }

    @Test
    void shouldUpdateQuantity() {
        Delivery delivery = Delivery.draft();
        Item item = Item.brandNew("Pizza Margherita", 2, delivery);

        item.setQuantity(5);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldHaveDifferentHashCodeWhenDifferentId() {
        Delivery delivery = Delivery.draft();
        Item item1 = Item.brandNew("Pizza Margherita", 2, delivery);
        Item item2 = Item.brandNew("Pizza Margherita", 2, delivery);

        assertThat(item1.hashCode()).isNotEqualTo(item2.hashCode());
    }
}
