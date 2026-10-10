package com.ordermanagement.platform.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class PostgresIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("order_management_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);

        registry.add("spring.datasource.username",
                postgres::getUsername);

        registry.add("spring.datasource.password",
                postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto",
                () -> "create-drop");
    }

    @Test
    void shouldStartApplicationWithPostgresContainer() {

        assertThat(postgres.isRunning()).isTrue();
    }
}