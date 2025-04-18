package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.dto.response.BookingResponse;
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
        BookingResponse response = bookingService.createBooking(booking);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/user/booking")
    public ResponseEntity<?> getAllBookingsByUser(
            @RequestParam(defaultValue ="0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        return ResponseEntity.ok().body(bookingService.getBookingByUser(page, size));
    }

    @GetMapping("/admin/booking/{bookingId}")
    public ResponseEntity<?> getBookingById(@PathVariable Integer bookingId) {
        return ResponseEntity.ok().body(bookingService.getBookingById(bookingId));
    }

}
