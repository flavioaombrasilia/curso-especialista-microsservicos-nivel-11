package com.algaworks.algadelivery.delivery.tracking.infrastructure.persistence;

import com.algaworks.algadelivery.delivery.tracking.utils.TestcontainersConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
public abstract class AbstractPersistenceIT {

}
