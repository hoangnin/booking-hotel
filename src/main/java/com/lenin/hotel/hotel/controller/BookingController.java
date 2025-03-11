package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.hotel.request.BookingRequest;
import com.lenin.hotel.hotel.service.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {
    private final IBookingService bookingService;
    @PostMapping("/user/booking")
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest booking) {
        bookingService.createBooking(booking);
        return ResponseEntity.ok().body(Map.of("message", "Booking created"));
    }

    @GetMapping("/user/booking/{userId}")
    public ResponseEntity<?> getAllBookingsByUser(@PathVariable String userId) {
        return ResponseEntity.ok().body(bookingService.getBookingByUserId(userId));
    }

}
