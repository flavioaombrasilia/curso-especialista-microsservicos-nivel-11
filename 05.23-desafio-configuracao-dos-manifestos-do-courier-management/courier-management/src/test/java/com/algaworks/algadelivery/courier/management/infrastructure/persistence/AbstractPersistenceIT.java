package com.algaworks.algadelivery.courier.management.infrastructure.persistence;

import com.algaworks.algadelivery.courier.management.utils.TestcontainersPostgreSQL;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersPostgreSQL.class)
public abstract class AbstractPersistenceIT {

}
