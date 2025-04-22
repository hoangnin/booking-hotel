package com.lenin.hotel.repository.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.PriceTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
@Import(DatabaseTestContainer.class)
public class PriceTrackingRepositoryTest {

    @Autowired
    private PriceTrackingRepository priceTrackingRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private Hotel hotel;

    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DatabaseTestContainer.postgresqlContainer().getJdbcUrl());
        registry.add("spring.datasource.username", () -> DatabaseTestContainer.postgresqlContainer().getUsername());
        registry.add("spring.datasource.password", () -> DatabaseTestContainer.postgresqlContainer().getPassword());
    }

    @BeforeEach
    public void setUp() {
        // Create and persist a Location
        Location location = Location.builder()
                .name("Test Location")
                .build();
        location = entityManager.persistAndFlush(location);

        // Create and persist an Owner
        User owner = new User();
        owner.setUsername("Test Owner");
        owner.setEmail("owner@example.com");
        owner.setPassword("securePassword123"); // Set a valid password
        owner = entityManager.persistAndFlush(owner);

        // Create and persist a Hotel
        hotel = Hotel.builder()
                .name("Test Hotel")
                .latitude(10.762622)
                .longitude(106.660172)
                .location(location) // Assign the location
                .owner(owner)       // Assign the owner
                .build();
        hotel = entityManager.persistAndFlush(hotel);

        // Create and persist PriceTracking entries
        PriceTracking priceTracking1 = PriceTracking.builder()
                .price(BigDecimal.valueOf(100.0))
                .hotel(hotel)
                .createDt(ZonedDateTime.now().minusDays(2))
                .build();
        priceTrackingRepository.save(priceTracking1);

        PriceTracking priceTracking2 = PriceTracking.builder()
                .price(BigDecimal.valueOf(120.0))
                .hotel(hotel)
                .createDt(ZonedDateTime.now().minusDays(1))
                .build();
        priceTrackingRepository.save(priceTracking2);

        PriceTracking priceTracking3 = PriceTracking.builder()
                .price(BigDecimal.valueOf(150.0))
                .hotel(hotel)
                .createDt(ZonedDateTime.now())
                .build();
        priceTrackingRepository.save(priceTracking3);
    }

    @Test
    public void testFindTopByHotelIdOrderByCreateDtDesc() {
        // Act
        Optional<PriceTracking> latestPriceTracking = priceTrackingRepository.findTopByHotelIdOrderByCreateDtDesc(hotel.getId().longValue());

        // Assert
        assertThat(latestPriceTracking).isPresent();
        assertThat(latestPriceTracking.get().getPrice()).isEqualTo(BigDecimal.valueOf(150.0));
        assertThat(latestPriceTracking.get().getHotel().getId()).isEqualTo(hotel.getId());
    }

    @Test
    public void testFindTopByHotelIdOrderByCreateDtDesc_NoResults() {
        // Act
        Optional<PriceTracking> latestPriceTracking = priceTrackingRepository.findTopByHotelIdOrderByCreateDtDesc(999L);

        // Assert
        assertThat(latestPriceTracking).isNotPresent();
    }
}