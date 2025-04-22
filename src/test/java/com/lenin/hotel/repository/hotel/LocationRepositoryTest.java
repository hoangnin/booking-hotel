package com.lenin.hotel.repository.hotel;

import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback
public class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    private Location location;

    @BeforeEach
    public void setUp() {
        // Create and persist a Location
        location = Location.builder()
                .name("Test Location")
                .build();
        location = locationRepository.save(location);
    }

    @Test
    public void testExistsByName() {
        // Act
        boolean exists = locationRepository.existsByName("Test Location");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByName_NotFound() {
        // Act
        boolean exists = locationRepository.existsByName("Nonexistent Location");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    public void testFindById() {
        // Act
        Location foundLocation = locationRepository.findById(location.getId()).orElse(null);

        // Assert
        assertThat(foundLocation).isNotNull();
        assertThat(foundLocation.getName()).isEqualTo("Test Location");
    }

    @Test
    public void testSaveAndDelete() {
        // Arrange
        Location newLocation = Location.builder()
                .name("Another Location")
                .build();
        newLocation = locationRepository.save(newLocation);

        // Act
        locationRepository.delete(newLocation);
        boolean exists = locationRepository.existsByName("Another Location");

        // Assert
        assertThat(exists).isFalse();
    }
}