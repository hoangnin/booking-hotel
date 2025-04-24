package com.lenin.hotel.configuration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDynamicProperties {
    public static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        PostgreSQLContainer<?> container = DatabaseTestContainer.getPostgreSqlContainer();

        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}