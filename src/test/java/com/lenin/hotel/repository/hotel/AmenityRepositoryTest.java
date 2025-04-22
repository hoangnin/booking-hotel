package com.lenin.hotel.repository.hotel;

import com.lenin.hotel.configuration.DatabaseTestContainer;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.repository.AmenityRepository;
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
public class AmenityRepositoryTest {

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DatabaseTestContainer.postgresqlContainer().getJdbcUrl());
        registry.add("spring.datasource.username", () -> DatabaseTestContainer.postgresqlContainer().getUsername());
        registry.add("spring.datasource.password", () -> DatabaseTestContainer.postgresqlContainer().getPassword());
    }

    @Test
    public void testFindByName() {
        // Arrange
        Amenity amenity = new Amenity();
        amenity.setName("WiFi");
        amenity.setAvailable(true);
        amenityRepository.save(amenity);

        // Act
        Amenity foundAmenity = amenityRepository.findByName("WiFi");

        // Assert
        assertThat(foundAmenity).isNotNull();
        assertThat(foundAmenity.getName()).isEqualTo("WiFi");
    }

    @Test
    public void testFindAllByIdIn() {
        // Arrange
        Amenity amenity1 = new Amenity();
        amenity1.setName("WiFi");
        amenity1.setAvailable(true);
        amenityRepository.save(amenity1);

        Amenity amenity2 = new Amenity();
        amenity2.setName("Pool");
        amenity2.setAvailable(true);
        amenityRepository.save(amenity2);

        // Act
        List<Amenity> amenities = amenityRepository.findAllByIdIn(List.of(amenity1.getId().toString(), amenity2.getId().toString()));

        // Assert
        assertThat(amenities).hasSize(2);
        assertThat(amenities).extracting(Amenity::getName).containsExactlyInAnyOrder("WiFi", "Pool");
    }

    @Test
    public void testSaveAmenity() {
        // Arrange
        Amenity amenity = new Amenity();
        amenity.setName("Gym");
        amenity.setAvailable(true);

        // Act
        Amenity savedAmenity = amenityRepository.save(amenity);

        // Assert
        assertThat(savedAmenity).isNotNull();
        assertThat(savedAmenity.getId()).isNotNull();
        assertThat(savedAmenity.getName()).isEqualTo("Gym");
    }

    @Test
    public void testDeleteAmenity() {
        // Arrange
        Amenity amenity = new Amenity();
        amenity.setName("Spa");
        amenity.setAvailable(true);
        Amenity savedAmenity = amenityRepository.save(amenity);

        // Act
        amenityRepository.deleteById(savedAmenity.getId());
        Amenity deletedAmenity = amenityRepository.findByName("Spa");

        // Assert
        assertThat(deletedAmenity).isNull();
    }

    @Test
    public void testFindByName_NotFound() {
        // Act
        Amenity foundAmenity = amenityRepository.findByName("NonExistentAmenity");

        // Assert
        assertThat(foundAmenity).isNull();
    }
}