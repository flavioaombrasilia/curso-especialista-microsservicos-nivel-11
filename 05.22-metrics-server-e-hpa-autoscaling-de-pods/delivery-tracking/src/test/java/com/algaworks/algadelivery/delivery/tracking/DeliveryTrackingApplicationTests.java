package com.algaworks.algadelivery.delivery.tracking;


import com.algaworks.algadelivery.delivery.tracking.config.TestCourierAPIClientConfig;
import com.algaworks.algadelivery.delivery.tracking.utils.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({TestcontainersConfig.class, TestCourierAPIClientConfig.class})
class DeliveryTrackingApplicationTests {

	@Test
	void contextLoads() {
	}

}
