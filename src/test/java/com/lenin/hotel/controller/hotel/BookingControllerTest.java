package com.lenin.hotel.controller.hotel;

import com.lenin.hotel.hotel.controller.BookingController;
import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.dto.response.BookingResponse;
import com.lenin.hotel.hotel.service.IBookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    @Mock
    private IBookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void testCreateBooking() {
        // Arrange
        BookingRequest bookingRequest = BookingRequest.builder().build();
        BookingResponse bookingResponse = BookingResponse.builder().build();

        when(bookingService.createBooking(bookingRequest)).thenReturn(bookingResponse);

        // Act
        ResponseEntity<?> responseEntity = bookingController.createBooking(bookingRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(bookingResponse, responseEntity.getBody());
        verify(bookingService, times(1)).createBooking(bookingRequest);
    }

    @Test
    void testGetAllBookingsByUser() {
        // Arrange
        int page = 0;
        int size = 10;
        //Act
        ResponseEntity<?> responseEntity = bookingController.getAllBookingsByUser(page, size);
        //Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(bookingService, times(1)).getBookingByUser(page, size);
    }

    @Test
    void testGetBookingById() {
        // Arrange
        Integer bookingId = 1;
        BookingResponse bookingResponse = BookingResponse.builder().build();

        when(bookingService.getBookingById(bookingId)).thenReturn(bookingResponse);

        // Act
        ResponseEntity<?> responseEntity = bookingController.getBookingById(bookingId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(bookingResponse, responseEntity.getBody());
        verify(bookingService, times(1)).getBookingById(bookingId);
    }
}