package com.algaworks.algadelivery.delivery.tracking.api.controller;

import com.algaworks.algadelivery.delivery.tracking.api.AbstractPresentationIT;
import com.algaworks.algadelivery.delivery.tracking.api.model.ContactPointInput;
import com.algaworks.algadelivery.delivery.tracking.api.model.CourierIdInput;
import com.algaworks.algadelivery.delivery.tracking.api.model.DeliveryInput;
import com.algaworks.algadelivery.delivery.tracking.api.model.ItemInput;
import com.algaworks.algadelivery.delivery.tracking.domain.model.Delivery;
import com.algaworks.algadelivery.delivery.tracking.domain.model.DeliveryStatus;
import com.algaworks.algadelivery.delivery.tracking.domain.model.DeliveryTestDataBuilder;
import com.algaworks.algadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

class DeliveryControllerIT extends AbstractPresentationIT {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @BeforeAll
    static void beforeAll() {
        initWireMock();
    }

    @AfterAll
    static void afterAll() {
        stopMock();
    }

    @BeforeEach
    public void setup() {
        super.beforeEach();
    }

    @Test
    void shouldCreateDraftDelivery() {
        DeliveryInput input = createValidDeliveryInput();

        UUID createdDeliveryId = RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(input)
            .when()
                .post("/api/v1/deliveries")
            .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .statusCode(HttpStatus.CREATED.value())
                .body("id", Matchers.not(Matchers.emptyString()))
                .body("status", Matchers.is("DRAFT"))
                .body("totalItems", Matchers.equalTo(2))
                .extract()
            .jsonPath().getUUID("id");

        Assertions.assertThat(deliveryRepository.existsById(createdDeliveryId)).isTrue();
        Delivery delivery = deliveryRepository.findById(createdDeliveryId).orElseThrow();
        Assertions.assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DRAFT);
        Assertions.assertThat(delivery.getSender().getName()).isEqualTo("João Silva");
        Assertions.assertThat(delivery.getRecipient().getName()).isEqualTo("Maria Santos");
    }

    @Test
    void shouldEditDelivery() {
        Delivery delivery = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.DRAFT)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        deliveryRepository.saveAndFlush(delivery);

        DeliveryInput input = createValidDeliveryInput();
        input.getItems().get(0).setQuantity(5);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(input)
            .when()
                .put("/api/v1/deliveries/{deliveryId}", delivery.getId())
            .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.is(delivery.getId().toString()))
                .body("totalItems", Matchers.equalTo(5));

        Delivery updatedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();
        Assertions.assertThat(updatedDelivery.getTotalItems()).isEqualTo(5);
    }

    @Test
    void shouldFindDeliveryById() {
        Delivery delivery = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.DRAFT)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        deliveryRepository.saveAndFlush(delivery);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get("/api/v1/deliveries/{deliveryId}", delivery.getId())
            .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.is(delivery.getId().toString()))
                .body("status", Matchers.is("DRAFT"));
    }

    @Test
    void shouldReturnNotFoundWhenDeliveryDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get("/api/v1/deliveries/{deliveryId}", nonExistentId)
            .then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldPlaceDelivery() {
        Delivery delivery = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.DRAFT)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        deliveryRepository.saveAndFlush(delivery);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .post("/api/v1/deliveries/{deliveryId}/placement", delivery.getId())
            .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Delivery updatedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();
        Assertions.assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_FOR_COURIER);
        Assertions.assertThat(updatedDelivery.getPlacedAt()).isNotNull();
    }

    @Test
    void shouldPickupDelivery() {
        Delivery delivery = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.WAITING_FOR_COURIER)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        deliveryRepository.saveAndFlush(delivery);

        UUID courierId = UUID.randomUUID();
        CourierIdInput courierInput = new CourierIdInput();
        courierInput.setCourierId(courierId);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(courierInput)
            .when()
                .post("/api/v1/deliveries/{deliveryId}/pickups", delivery.getId())
            .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Delivery updatedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();
        Assertions.assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        Assertions.assertThat(updatedDelivery.getCourierId()).isEqualTo(courierId);
        Assertions.assertThat(updatedDelivery.getAssignedAt()).isNotNull();
    }

    @Test
    void shouldCompleteDelivery() {
        Delivery delivery = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.IN_TRANSIT)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        deliveryRepository.saveAndFlush(delivery);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .post("/api/v1/deliveries/{deliveryId}/completion", delivery.getId())
            .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value());

        Delivery updatedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();
        Assertions.assertThat(updatedDelivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        Assertions.assertThat(updatedDelivery.getFulfilledAt()).isNotNull();
    }

    @Test
    void shouldNotPlaceDeliveryWithoutPreparationDetails() {
        Delivery delivery = Delivery.draft();
        deliveryRepository.saveAndFlush(delivery);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .post("/api/v1/deliveries/{deliveryId}/placement", delivery.getId())
            .then()
                .assertThat()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldValidateDeliveryInputWhenCreating() {
        DeliveryInput invalidInput = new DeliveryInput();

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidInput)
            .when()
                .post("/api/v1/deliveries")
            .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldFindAllDeliveries() {
        Delivery delivery1 = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.DRAFT)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        Delivery delivery2 = DeliveryTestDataBuilder.aDelivery()
                .status(DeliveryStatus.WAITING_FOR_COURIER)
                .withItems(true)
                .withPreparationDetails(true)
                .build();
        deliveryRepository.saveAndFlush(delivery1);
        deliveryRepository.saveAndFlush(delivery2);

        RestAssured
            .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
                .get("/api/v1/deliveries")
            .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", Matchers.greaterThanOrEqualTo(2));
    }

    private DeliveryInput createValidDeliveryInput() {
        ContactPointInput sender = new ContactPointInput();
        sender.setZipCode("12345-000");
        sender.setStreet("Rua A");
        sender.setNumber("100");
        sender.setComplement("Apto 1");
        sender.setName("João Silva");
        sender.setPhone("11999999999");

        ContactPointInput recipient = new ContactPointInput();
        recipient.setZipCode("54321-000");
        recipient.setStreet("Rua B");
        recipient.setNumber("200");
        recipient.setComplement("Casa");
        recipient.setName("Maria Santos");
        recipient.setPhone("11988888888");

        ItemInput item = new ItemInput();
        item.setName("Pizza");
        item.setQuantity(2);

        DeliveryInput input = new DeliveryInput();
        input.setSender(sender);
        input.setRecipient(recipient);
        input.setItems(List.of(item));

        return input;
    }
}
