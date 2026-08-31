package com.algaworks.algadelivery.courier.management.api;

import com.algaworks.algadelivery.courier.management.utils.TestcontainersConfig;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
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

import static io.restassured.config.JsonConfig.jsonConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    EurekaClientAutoConfiguration.class,
    EurekaDiscoveryClientConfiguration.class,
    LoadBalancerEurekaAutoConfiguration.class,
    BlockingLoadBalancerClientAutoConfiguration.class
})
@Import(TestcontainersConfig.class)
public abstract class AbstractPresentationIT {

    @LocalServerPort
    protected int port;

    protected void beforeEach() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;
        RestAssured.config = RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));
        RestAssured.registerParser("application/problem+json", Parser.JSON);
    }

}
