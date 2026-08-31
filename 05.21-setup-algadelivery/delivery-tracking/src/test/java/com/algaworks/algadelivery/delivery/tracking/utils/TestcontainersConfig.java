package com.algaworks.algadelivery.delivery.tracking.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({TestcontainersKafka.class, TestcontainersPostgreSQL.class})
public class TestcontainersConfig {

}
