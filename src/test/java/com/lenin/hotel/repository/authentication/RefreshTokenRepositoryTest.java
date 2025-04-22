package com.lenin.hotel.repository.authentication;

    import com.lenin.hotel.authentication.model.RefreshToken;
    import com.lenin.hotel.authentication.model.User;
    import com.lenin.hotel.authentication.repository.RefreshTokenRepository;
    import com.lenin.hotel.configuration.DatabaseTestContainer;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
    import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.test.annotation.Rollback;
    import org.springframework.test.context.DynamicPropertyRegistry;
    import org.springframework.test.context.DynamicPropertySource;
    import org.testcontainers.containers.PostgreSQLContainer;

    import java.time.Duration;
    import java.time.Instant;
    import java.util.Optional;

    import static org.assertj.core.api.Assertions.assertThat;

    @DataJpaTest
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @Rollback
    @Import(DatabaseTestContainer.class)
    public class RefreshTokenRepositoryTest {

        @Autowired
        private RefreshTokenRepository refreshTokenRepository;

        @Autowired
        private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

        @Autowired
        private PostgreSQLContainer<?> postgreSQLContainer;

        @DynamicPropertySource
        static void properties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", () -> DatabaseTestContainer.postgresqlContainer().getJdbcUrl());
            registry.add("spring.datasource.username", () -> DatabaseTestContainer.postgresqlContainer().getUsername());
            registry.add("spring.datasource.password", () -> DatabaseTestContainer.postgresqlContainer().getPassword());
        }

        private User user;
        private RefreshToken refreshToken;

        @BeforeEach
        public void setUp() {
            user = new User();
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPassword("password123");
            user = entityManager.persistAndFlush(user);

            refreshToken = new RefreshToken();
            refreshToken.setToken("initial-token");
            refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
            refreshToken.setUser(user);
            refreshToken = entityManager.persistAndFlush(refreshToken);
        }

        @Test
        public void testFindByUserId() {
            Optional<RefreshToken> found = refreshTokenRepository.findByUserId(user.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getToken()).isEqualTo("initial-token");
        }

        @Test
        public void testFindByToken() {
            Optional<RefreshToken> found = refreshTokenRepository.findByToken("initial-token");
            assertThat(found).isPresent();
            assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        }


        @Test
        public void testUpdateToken() {
            String newToken = "updated-token";
            Instant newExpiry = Instant.now().plusSeconds(7200);
            refreshTokenRepository.updateToken(newToken, newExpiry, user.getId());
            entityManager.clear();
            Optional<RefreshToken> updated = refreshTokenRepository.findByUserId(user.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getToken()).isEqualTo(newToken);

            // Increase tolerance to 5 milliseconds (5000000 nanoseconds)
            Duration diff = Duration.between(updated.get().getExpiryDate(), newExpiry).abs();
            assertThat(diff.toNanos()).isLessThan(5_000_000);
        }

        @Test
        public void testDeleteByUserId() {
            refreshTokenRepository.deleteByUserId(user.getId());
            Optional<RefreshToken> found = refreshTokenRepository.findByUserId(user.getId());
            assertThat(found).isNotPresent();
        }

        @Test
        public void testDeleteByUser() {
            refreshTokenRepository.deleteByUser(user);
            Optional<RefreshToken> found = refreshTokenRepository.findByUserId(user.getId());
            assertThat(found).isNotPresent();
        }
    }