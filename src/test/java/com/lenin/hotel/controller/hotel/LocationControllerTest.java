package com.lenin.hotel.controller.hotel;

import com.lenin.hotel.hotel.controller.LocationController;
import com.lenin.hotel.hotel.dto.response.LocationResponse;
import com.lenin.hotel.hotel.service.ILocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationControllerTest {

    @Mock
    private ILocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @Test
    void testCreateLocation() {
        // Arrange
        Map<String, String> locationRequest = Map.of("name", "Test Location");
        doNothing().when(locationService).createLocation(locationRequest);

        // Act
        ResponseEntity<?> responseEntity = locationController.createLocation(locationRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Map.of("message", "Location created successfully"), responseEntity.getBody());
        verify(locationService, times(1)).createLocation(locationRequest);
    }

    @Test
    void testGetAllLocations() {
        // Arrange
        LocationResponse location1 = LocationResponse.builder()
                .id(1)
                .name("Location 1")
                .build();
        LocationResponse location2 = LocationResponse.builder()
                .id(2)
                .name("Location 2")
                .build();
        List<LocationResponse> locationList = List.of(location1, location2);

        when(locationService.getAllLocation()).thenReturn(locationList);

        // Act
        ResponseEntity<?> responseEntity = locationController.getAllLocations();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(locationList, responseEntity.getBody());
        verify(locationService, times(1)).getAllLocation();
    }
}