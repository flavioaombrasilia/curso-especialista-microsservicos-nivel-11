package com.algaworks.algadelivery.delivery.tracking.config;

import com.algaworks.algadelivery.delivery.tracking.infrastructure.http.client.CourierAPIClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@TestConfiguration
@Profile("test")
public class TestCourierAPIClientConfig {

    @Value("${courier-management.url:http://localhost:8782}")
    private String courierManagementUrl;

    @Bean
    public CourierAPIClient courierAPIClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(courierManagementUrl)
                .requestFactory(generateClientRequestFactory())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(CourierAPIClient.class);
    }

    private ClientHttpRequestFactory generateClientRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(10));
        factory.setReadTimeout(Duration.ofMillis(200));
        return factory;
    }

}
