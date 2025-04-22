package com.lenin.hotel.controller.hotel;

import com.lenin.hotel.hotel.controller.FavoriteController;
import com.lenin.hotel.hotel.service.IHotelService;
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
public class FavoriteControllerTest {

    @Mock
    private IHotelService hotelService;

    @InjectMocks
    private FavoriteController favoriteController;

    @Test
    void testAddFavorite() {
        // Arrange
        Integer hotelId = 1;
        Map<String, String> expectedResponse = Map.of("message", "Successfully added favorite hotel.");
        when(hotelService.addFavorite(hotelId)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<?> responseEntity = favoriteController.addFavorite(hotelId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(hotelService, times(1)).addFavorite(hotelId);
    }

    @Test
    void testGetFavorites() {
        // Arrange
        List<Integer> favoriteHotelIds = List.of(1, 2, 3);
        when(hotelService.getAllUserFavorite()).thenReturn(favoriteHotelIds);

        // Act
        ResponseEntity<?> responseEntity = favoriteController.getFavorites();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(favoriteHotelIds, responseEntity.getBody());
        verify(hotelService, times(1)).getAllUserFavorite();
    }

    @Test
    void testRemoveFavorite() {
        // Arrange
        Integer hotelId = 1;
        Map<String, String> expectedResponse = Map.of("message", "Successfully removed favorite");

        // Act
        ResponseEntity<?> responseEntity = favoriteController.removeFavorite(hotelId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, ((ResponseEntity<Map<String, String>>) responseEntity).getBody());
        verify(hotelService, times(1)).removeFavorite(hotelId);
    }
}