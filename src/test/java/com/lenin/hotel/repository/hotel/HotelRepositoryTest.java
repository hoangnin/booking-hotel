package com.lenin.hotel.repository.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.repository.HotelRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
@Import(DatabaseTestContainer.class)
public class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private User owner;
    private Location location;

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
        // Create a User as owner
        owner = new User();
        owner.setUsername("owneruser");
        owner.setEmail("owner@example.com");
        owner.setPassword("password123");
        owner = entityManager.persistAndFlush(owner);

        // Create a Location
        location = new Location();
        location.setName("Test Location");
        location = entityManager.persistAndFlush(location);

        // Create a Hotel
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setLatitude(10.762622);
        hotel.setLongitude(106.660172);
        hotel.setLocation(location);
        hotel.setOwner(owner);
        hotelRepository.save(hotel);
    }

    @Test
    public void testFindByOwner() {
        // Act
        List<Hotel> hotels = hotelRepository.findByOwner(owner);

        // Assert
        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    public void testFindAllByOwnerId() {
        // Act
        List<Hotel> hotels = hotelRepository.findAllByOwnerId(owner.getId().intValue());

        // Assert
        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    public void testFindNearbyHotels() {
        // Act
        List<Hotel> hotels = hotelRepository.findNearbyHotels(10.762622, 106.660172, 10, 0);

        // Assert
        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getName()).isEqualTo("Test Hotel");
    }
}