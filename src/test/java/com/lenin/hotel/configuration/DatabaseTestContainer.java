package com.lenin.hotel.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class DatabaseTestContainer {
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        postgresContainer.start();  // Khởi động container ngay khi lớp này được nạp
    }

    @Bean
    public static PostgreSQLContainer<?> postgresqlContainer() {
        return postgresContainer;
    }
}
