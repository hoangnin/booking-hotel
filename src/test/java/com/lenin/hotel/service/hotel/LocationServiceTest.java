package com.lenin.hotel.service.hotel;

import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.dto.response.LocationResponse;
import com.lenin.hotel.hotel.repository.LocationRepository;
import com.lenin.hotel.hotel.service.impl.LocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Location location;

    @BeforeEach
    void setUp() {
        location = Location.builder()
                .id(1)
                .name("Test Location")
                .build();
    }

    @Test
    void testCreateLocation_Success() {
        // Arrange
        Map<String, String> locationRequest = Map.of("name", "New Location");

        // Act
        locationService.createLocation(locationRequest);

        // Assert
        verify(locationRepository, times(1)).save(any(Location.class));
    }

    @Test
    void testGetAllLocation_Success() {
        // Arrange
        Location location1 = Location.builder().id(1).name("Location 1").build();
        Location location2 = Location.builder().id(2).name("Location 2").build();
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));

        // Act
        List<LocationResponse> responses = locationService.getAllLocation();

        // Assert
        assertEquals(2, responses.size());
        assertEquals("Location 1", responses.get(0).getName());
        assertEquals("Location 2", responses.get(1).getName());
        verify(locationRepository, times(1)).findAll();
    }
    @Test
    void testCreateLocation_AlreadyExists() {
        // Arrange
        Map<String, String> locationRequest = Map.of("name", "Existing Location");
        when(locationRepository.existsByName("Existing Location")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> locationService.createLocation(locationRequest));
        assertEquals("Location with name 'Existing Location' already exists.", exception.getMessage());
        verify(locationRepository, never()).save(any(Location.class));
    }
    @Test
    void testGetAllLocation_Empty() {
        // Arrange
        when(locationRepository.findAll()).thenReturn(List.of());

        // Act
        List<LocationResponse> responses = locationService.getAllLocation();

        // Assert
        assertEquals(0, responses.size());
        verify(locationRepository, times(1)).findAll();
    }
}