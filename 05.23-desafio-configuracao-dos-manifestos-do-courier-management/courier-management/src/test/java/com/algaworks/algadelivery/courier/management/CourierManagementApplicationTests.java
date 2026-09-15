package com.algaworks.algadelivery.courier.management;

import com.algaworks.algadelivery.courier.management.utils.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class,
        LoadBalancerEurekaAutoConfiguration.class,
        BlockingLoadBalancerClientAutoConfiguration.class
})
@Import(TestcontainersConfig.class)
class CourierManagementApplicationTests {

    @Test
    void contextLoads() {
    }

}
