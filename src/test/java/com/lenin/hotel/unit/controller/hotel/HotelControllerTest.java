package com.lenin.hotel.unit.controller.hotel;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.lenin.hotel.authentication.service.IUserService;
    import com.lenin.hotel.hotel.controller.HotelController;
    import com.lenin.hotel.hotel.dto.request.ChangePriceRequest;
    import com.lenin.hotel.hotel.dto.request.HotelRequest;
    import com.lenin.hotel.hotel.dto.response.HotelResponse;
    import com.lenin.hotel.hotel.dto.response.UserResponse;
    import com.lenin.hotel.hotel.service.IHotelService;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;

    import java.time.ZonedDateTime;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;

    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.mockito.Mockito.*;

    @ExtendWith(MockitoExtension.class)
    public class HotelControllerTest {

        @Mock
        private IHotelService hotelService;

        @Mock
        private IUserService userService;

        @InjectMocks
        private HotelController hotelController;

        @Test
        void testCreateRoom() {
            // Arrange
            HotelRequest hotelRequest = HotelRequest.builder().build();
            doNothing().when(hotelService).createHotel(hotelRequest);

            // Act
            ResponseEntity<?> responseEntity = hotelController.createRoom(hotelRequest);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(Map.of("message", "Room created successfully!"), responseEntity.getBody());
            verify(hotelService, times(1)).createHotel(hotelRequest);
        }

        @Test
        void testGetHotel() {
            // Arrange
            List<Integer> hotelIds = List.of(1, 2, 3);
            when(hotelService.getHotelsByHotelOwnerId()).thenReturn(hotelIds);

            // Act
            ResponseEntity<?> responseEntity = hotelController.getHotel();

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(hotelIds, responseEntity.getBody());
            verify(hotelService, times(1)).getHotelsByHotelOwnerId();
        }

        @Test
        void testSetHotelOwner() {
            // Arrange
            Integer hotelOwnerId = 1;
            doNothing().when(hotelService).activeHotelOwner(hotelOwnerId);

            // Act
            ResponseEntity<?> responseEntity = hotelController.setHotelOwner(hotelOwnerId);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(Map.of("message", "Activated successfully!"), responseEntity.getBody());
            verify(hotelService, times(1)).activeHotelOwner(hotelOwnerId);
        }

        @Test
        void testGetAllRooms() {
            // Arrange
            int page = 0;
            int size = 10;
            Integer hotelId = 1;
            Integer ownerId = 2;
            Double latitude = 1.0;
            Double longitude = 2.0;
            List<HotelResponse> hotelResponses = List.of(new HotelResponse());
            when(hotelService.getAllHotel(any(Pageable.class), eq(hotelId), eq(ownerId), eq(latitude), eq(longitude))).thenReturn(hotelResponses);

            // Act
            ResponseEntity<?> responseEntity = hotelController.getAllRooms(page, size, hotelId, ownerId, latitude, longitude);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(hotelResponses, responseEntity.getBody());
            verify(hotelService, times(1)).getAllHotel(any(Pageable.class), eq(hotelId), eq(ownerId), eq(latitude), eq(longitude));
        }

        @Test
        void testGetHotelById() {
            // Arrange
            Integer id = 1;
            HotelResponse hotelResponse = new HotelResponse();
            when(hotelService.getHotelById(id)).thenReturn(hotelResponse);

            // Act
            ResponseEntity<?> responseEntity = hotelController.getHotel(id);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(hotelResponse, responseEntity.getBody());
            verify(hotelService, times(1)).getHotelById(id);
        }

        @Test
        void testGetHotelOwner() {
            // Arrange
            Integer id = 1;
            UserResponse userResponse = UserResponse.builder().build();
            when(userService.getHotelOwner(id)).thenReturn(userResponse);

            // Act
            ResponseEntity<?> responseEntity = hotelController.getHotelOwner(id);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(userResponse, responseEntity.getBody());
            verify(userService, times(1)).getHotelOwner(id);
        }

        @Test
        void testSearchHotels() {
            // Arrange
            String name = "hotelName";
            Integer locationId = 1;
            Double rating = 4.5;
            Set<Integer> amenityIds = Set.of(1, 2);
            Double minPrice = 100.0;
            Double maxPrice = 200.0;
            Integer minRoomsAvailable = 5;
            String hotelType = "Luxury";
            ZonedDateTime checkIn = ZonedDateTime.now();
            ZonedDateTime checkOut = ZonedDateTime.now().plusDays(1);
            List<HotelResponse> hotelResponses = List.of(new HotelResponse());
            when(hotelService.searchHotels(name, locationId, rating, amenityIds, minPrice, maxPrice, minRoomsAvailable, hotelType, checkIn, checkOut))
                    .thenReturn(hotelResponses);

            // Act
            ResponseEntity<?> responseEntity = hotelController.searchHotels(name, locationId, rating, amenityIds, minPrice, maxPrice, minRoomsAvailable, hotelType, checkIn, checkOut);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(hotelResponses, responseEntity.getBody());
            verify(hotelService, times(1)).searchHotels(name, locationId, rating, amenityIds, minPrice, maxPrice, minRoomsAvailable, hotelType, checkIn, checkOut);
        }

        @Test
        void testChangePrice() {
            // Arrange
            ChangePriceRequest changePriceRequest = new ChangePriceRequest();
            doNothing().when(hotelService).changePrice(changePriceRequest);

            // Act
            ResponseEntity<?> responseEntity = hotelController.changePrice(changePriceRequest);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(Map.of("message", "Price changed successfully!"), responseEntity.getBody());
            verify(hotelService, times(1)).changePrice(changePriceRequest);
        }

        @Test
        void testSendHotelOwnerReport() {
            // Arrange
            doNothing().when(hotelService).generateAndSendHotelOwnerReport();

            // Act
            ResponseEntity<?> responseEntity = hotelController.sendHotelOwnerReport();

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(Map.of("message", "Report generated successfully!"), responseEntity.getBody());
            verify(hotelService, times(1)).generateAndSendHotelOwnerReport();
        }

        @Test
        void testUpdateHotel() {
            // Arrange
            Integer hotelId = 1;
            JsonNode hotelJson = mock(JsonNode.class);
            doNothing().when(hotelService).updateHotel(hotelId, hotelJson);

            // Act
            ResponseEntity<?> responseEntity = hotelController.updateHotel(hotelId, hotelJson);

            // Assert
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(Map.of("message", "Hotel updated successfully!"), responseEntity.getBody());
            verify(hotelService, times(1)).updateHotel(hotelId, hotelJson);
        }
    }