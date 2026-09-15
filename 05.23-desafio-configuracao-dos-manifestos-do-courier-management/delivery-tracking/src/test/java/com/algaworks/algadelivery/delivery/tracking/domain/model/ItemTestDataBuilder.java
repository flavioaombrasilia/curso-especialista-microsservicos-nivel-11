package com.algaworks.algadelivery.delivery.tracking.domain.model;

public class ItemTestDataBuilder {

    private String description = "Pizza Margherita";
    private Integer quantity = 2;
    private Delivery delivery;

    private ItemTestDataBuilder() {
    }

    public static ItemTestDataBuilder anItem() {
        return new ItemTestDataBuilder();
    }

    public Item build() {
        if (delivery == null) {
            delivery = Delivery.draft();
        }
        return Item.brandNew(description, quantity, delivery);
    }

    public ItemTestDataBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ItemTestDataBuilder quantity(Integer quantity) {
        this.quantity = quantity;
        return this;
    }

    public ItemTestDataBuilder delivery(Delivery delivery) {
        this.delivery = delivery;
        return this;
    }

}
