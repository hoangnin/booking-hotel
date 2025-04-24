package com.lenin.hotel.unit.service.hotel;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.hotel.model.Booking;
import com.lenin.hotel.common.service.IEmailService;
import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.BookingRepository;
import com.lenin.hotel.hotel.repository.HotelRepository;
import com.lenin.hotel.hotel.service.IHotelService;
import com.lenin.hotel.hotel.service.impl.BookingServiceImpl;
import com.lenin.hotel.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private PaymentServiceImpl paymentService;
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequest bookingRequest;
    private User user;
    @Mock
    private IHotelService hotelService;

    @BeforeEach
    void setUp() {
        // Mock SecurityContext and Authentication
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn("testUser");

        // Mock user
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        // BookingRequest setup
        bookingRequest = BookingRequest.builder()
                .hotelId(1)
                .checkIn(ZonedDateTime.now().plusDays(1))
                .checkOut(ZonedDateTime.now().plusDays(5))
                .build();
    }

   @Test
    void testCreateBooking_Success() {
        // Arrange
        User user = new User();
        Hotel hotel = new Hotel();
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(150.0)); // Set a valid price

        when(userRepository.getByUsername(anyString())).thenReturn(java.util.Optional.of(user));
        when(hotelRepository.findById(1)).thenReturn(java.util.Optional.of(hotel));
        when(hotelService.getLatestPrice(anyLong())).thenReturn(priceTracking);
        when(bookingRepository.existsOverlappingBooking(anyInt(), any(), any())).thenReturn(false);
        when(paymentService.createPayment(any(Booking.class))).thenReturn("payment-url");

        // Act
        var response = bookingService.createBooking(bookingRequest);

        // Assert
        assertNotNull(response);
        assertEquals("payment-url", response.getPaymentUrl());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailService, times(1)).sendMailBookingSuccess(any(Booking.class));
    }

    @Test
    void testCreateBooking_UserNotFound() {
        // Arrange
        when(userRepository.getByUsername(anyString())).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(bookingRequest));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_HotelNotFound() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(bookingRequest));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_OverlappingDates() {
        // Arrange
        User user = new User();
        Hotel hotel = new Hotel();
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(150.0)); // Set a valid price

        when(userRepository.getByUsername(anyString())).thenReturn(java.util.Optional.of(user));
        when(hotelRepository.findById(1)).thenReturn(java.util.Optional.of(hotel));
        when(hotelService.getLatestPrice(anyLong())).thenReturn(priceTracking); // Mock valid price tracking
        when(bookingRepository.existsOverlappingBooking(anyInt(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> bookingService.createBooking(bookingRequest));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}