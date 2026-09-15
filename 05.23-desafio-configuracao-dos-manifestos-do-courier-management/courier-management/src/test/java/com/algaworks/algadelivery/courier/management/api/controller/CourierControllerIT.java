package com.algaworks.algadelivery.courier.management.api.controller;

import com.algaworks.algadelivery.courier.management.api.AbstractPresentationIT;
import com.algaworks.algadelivery.courier.management.domain.model.Courier;
import com.algaworks.algadelivery.courier.management.domain.repository.CourierRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

class CourierControllerIT extends AbstractPresentationIT {

    @Autowired
    private CourierRepository courierRepository;

    @BeforeEach
    void setUp() {
        beforeEach();
        RestAssured.basePath = "/api/v1/couriers";
    }

    @Test
    void shouldCreateCourier() {
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                    {
                        "name": "João da Silva",
                        "phone": "11999998888"
                    }
                    """)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("name", equalTo("João da Silva"))
                .body("phone", equalTo("11999998888"))
                .body("pendingDeliveriesQuantity", equalTo(0))
                .body("fulfilledDeliveriesQuantity", equalTo(0));
    }

    @Test
    void shouldRejectCourierWithoutName() {
        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                    {
                        "name": "",
                        "phone": "11999998888"
                    }
                    """)
            .when()
                .post()
            .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldFindCourierById() {
        UUID courierId = givenCourier("Maria Souza", "11912341234");

        RestAssured
            .given()
                .accept(ContentType.JSON)
                .pathParam("courierId", courierId)
            .when()
                .get("/{courierId}")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(courierId.toString()))
                .body("name", equalTo("Maria Souza"));
    }

    @Test
    void shouldReturnNotFoundForUnknownCourier() {
        RestAssured
            .given()
                .accept(ContentType.JSON)
                .pathParam("courierId", UUID.randomUUID())
            .when()
                .get("/{courierId}")
            .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldUpdateCourier() {
        UUID courierId = givenCourier("Carlos Lima", "11955554444");

        RestAssured
            .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("courierId", courierId)
                .body("""
                    {
                        "name": "Carlos Lima Filho",
                        "phone": "11933332222"
                    }
                    """)
            .when()
                .put("/{courierId}")
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo("Carlos Lima Filho"))
                .body("phone", equalTo("11933332222"));
    }

    @Test
    void shouldListCouriersPaginated() {
        givenCourier("Entregador Listado", "11900001111");

        RestAssured
            .given()
                .accept(ContentType.JSON)
            .when()
                .get()
            .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", notNullValue())
                .body("content.name", hasItem("Entregador Listado"))
                .body("page.size", equalTo(10))
                .body("page.number", equalTo(0));
    }

    @Test
    void shouldCalculatePayoutFee() {
        // O endpoint de cálculo falha de propósito em ~50% das chamadas — é o cenário que o
        // Delivery-Tracking usa para exercitar retry e circuit breaker. Aqui insistimos até
        // obter uma resposta bem-sucedida e então validamos o valor calculado.
        Response response = null;

        for (int attempt = 0; attempt < 30; attempt++) {
            response = RestAssured
                    .given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                            {
                                "distanceInKm": 3.5
                            }
                            """)
                    .when()
                        .post("/payout-calculation");

            if (response.statusCode() == HttpStatus.OK.value()) {
                break;
            }
        }

        assertThat(response).isNotNull();
        response.then()
                .statusCode(HttpStatus.OK.value())
                .body("payoutFee", comparesEqualTo(new BigDecimal("35.00")));
    }

    private UUID givenCourier(String name, String phone) {
        return courierRepository.saveAndFlush(Courier.brandNew(name, phone)).getId();
    }
}
