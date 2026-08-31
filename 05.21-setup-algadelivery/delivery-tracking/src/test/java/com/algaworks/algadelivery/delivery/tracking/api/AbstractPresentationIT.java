package com.algaworks.algadelivery.delivery.tracking.api;

import com.algaworks.algadelivery.delivery.tracking.config.TestCourierAPIClientConfig;
import com.algaworks.algadelivery.delivery.tracking.utils.TestcontainersConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.config.JsonConfig.jsonConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    EurekaClientAutoConfiguration.class,
    EurekaDiscoveryClientConfiguration.class,
    LoadBalancerEurekaAutoConfiguration.class,
    BlockingLoadBalancerClientAutoConfiguration.class
})
@Import({TestcontainersConfig.class, TestCourierAPIClientConfig.class})
public abstract class AbstractPresentationIT {

    @LocalServerPort
    protected int port;

    protected static WireMockServer wireMockCourierManagement;

    protected void beforeEach() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;
        RestAssured.config().jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
    }

    protected static void initWireMock() {
        wireMockCourierManagement = new WireMockServer(options()
                .port(8782)
                .usingFilesUnderDirectory("src/test/resources/wiremock/courier-management"));

        wireMockCourierManagement.start();
    }

    protected static void stopMock() {
        wireMockCourierManagement.stop();
    }
}
