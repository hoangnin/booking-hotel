package com.lenin.hotel.configuration;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@Component
@TestConfiguration
public class DatabaseTestContainer {

    private static final PostgreSQLContainer<?> postgreSqlContainer;

    static {
        postgreSqlContainer = new PostgreSQLContainer<>("postgres:15.2-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

        // Start container immediately
        postgreSqlContainer.start();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url(postgreSqlContainer.getJdbcUrl())
            .username(postgreSqlContainer.getUsername())
            .password(postgreSqlContainer.getPassword())
            .build();
    }

    public static PostgreSQLContainer<?> getPostgreSqlContainer() {
        return postgreSqlContainer;
    }
}
