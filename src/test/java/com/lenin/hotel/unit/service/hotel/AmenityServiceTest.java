package com.lenin.hotel.unit.service.hotel;

import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.hotel.dto.request.AmenityRequest;
import com.lenin.hotel.hotel.dto.response.AmenityResponse;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.repository.AmenityRepository;
import com.lenin.hotel.hotel.service.impl.AmenityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AmenityServiceTest {

    @Mock
    private AmenityRepository amenityRepository;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    @BeforeEach
    void setUp() {
        amenityService = new AmenityServiceImpl(amenityRepository);
    }

    @Test
    void testCreateAmenity() {
        // Arrange
        AmenityRequest amenityRequest = new AmenityRequest();
        amenityRequest.setName("Free Wi-Fi");

        Amenity amenity = Amenity.builder()
                .name("Free Wi-Fi")
                .available(true)
                .build();

        when(amenityRepository.save(any(Amenity.class))).thenReturn(amenity);

        // Act
        amenityService.createAmenity(amenityRequest);

        // Assert
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }

    @Test
    void testGetAll() {
        // Arrange
        Amenity amenity1 = Amenity.builder().id(1).name("Free Wi-Fi").available(true).build();
        Amenity amenity2 = Amenity.builder().id(2).name("Swimming Pool").available(true).build();

        when(amenityRepository.findAll()).thenReturn(List.of(amenity1, amenity2));

        // Act
        List<AmenityResponse> result = amenityService.getAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Free Wi-Fi", result.get(0).getName());
        assertEquals("Swimming Pool", result.get(1).getName());
        verify(amenityRepository, times(1)).findAll();
    }
    @Test
    void testCreateAmenity_DuplicateName() {
        // Arrange
        AmenityRequest amenityRequest = new AmenityRequest();
        amenityRequest.setName("Free Wi-Fi");

        Amenity existingAmenity = Amenity.builder()
                .name("Free Wi-Fi")
                .available(true)
                .build();

        when(amenityRepository.findByName("Free Wi-Fi")).thenReturn(existingAmenity);

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> amenityService.createAmenity(amenityRequest));
        assertEquals("Amenity with the same name already exists.", exception.getMessage());
        verify(amenityRepository, never()).save(any(Amenity.class));
    }

    @Test
    void testGetAll_EmptyList() {
        // Arrange
        when(amenityRepository.findAll()).thenReturn(List.of());

        // Act
        List<AmenityResponse> result = amenityService.getAll();

        // Assert
        assertEquals(0, result.size());
        verify(amenityRepository, times(1)).findAll();
    }
}