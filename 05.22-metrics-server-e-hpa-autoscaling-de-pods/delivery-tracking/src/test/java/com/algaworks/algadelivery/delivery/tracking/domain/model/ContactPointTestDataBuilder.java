package com.algaworks.algadelivery.delivery.tracking.domain.model;

public class ContactPointTestDataBuilder {

    private String zipCode = "12345-678";
    private String street = "Rua das Flores";
    private String number = "123";
    private String complement = "Apto 45";
    private String name = "João Silva";
    private String phone = "(11) 98765-4321";

    private ContactPointTestDataBuilder() {
    }

    public static ContactPointTestDataBuilder aContactPoint() {
        return new ContactPointTestDataBuilder();
    }

    public ContactPoint build() {
        return ContactPoint.builder()
                .zipCode(zipCode)
                .street(street)
                .number(number)
                .complement(complement)
                .name(name)
                .phone(phone)
                .build();
    }

    public ContactPointTestDataBuilder zipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }

    public ContactPointTestDataBuilder street(String street) {
        this.street = street;
        return this;
    }

    public ContactPointTestDataBuilder number(String number) {
        this.number = number;
        return this;
    }

    public ContactPointTestDataBuilder complement(String complement) {
        this.complement = complement;
        return this;
    }

    public ContactPointTestDataBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ContactPointTestDataBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

}
