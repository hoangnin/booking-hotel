package com.lenin.hotel.unit.controller.hotel;

import com.lenin.hotel.hotel.controller.AmenityController;
import com.lenin.hotel.hotel.dto.request.AmenityRequest;
import com.lenin.hotel.hotel.dto.response.AmenityResponse;
import com.lenin.hotel.hotel.service.IAmenityService;
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
public class AmenityControllerTest {

    @Mock
    private IAmenityService amenityService;

    @InjectMocks
    private AmenityController amenityController;

    @Test
    void testCreateAmenity() {
        // Arrange
        AmenityRequest amenityRequest = new AmenityRequest();
        amenityRequest.setName("Free Wi-Fi");

        // Act
        ResponseEntity<?> responseEntity = amenityController.createAmenity(amenityRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Map.of("message", "create amenity successfully!"), responseEntity.getBody());
        verify(amenityService, times(1)).createAmenity(amenityRequest);
    }

    @Test
    void testGetAllAmenities() {
        // Arrange
        AmenityResponse amenity1 = AmenityResponse.builder()
                .id(1)
                .name("Swimming Pool")
                .build();
        AmenityResponse amenity2 = AmenityResponse.builder()
                .id(2)
                .name("Free Wi-Fi")
                .build();
        List<AmenityResponse> amenityList = List.of(amenity1, amenity2);

        when(amenityService.getAll()).thenReturn(amenityList);

        // Act
        ResponseEntity<?> responseEntity = amenityController.getAllAmenities();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(amenityList, responseEntity.getBody());
        verify(amenityService, times(1)).getAll();
    }
}