package com.lenin.hotel.hotel.service;

import com.lenin.hotel.hotel.request.BookingRequest;
import com.lenin.hotel.hotel.response.BookingResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IBookingService {
    void createBooking(@Valid BookingRequest booking);
    List<BookingResponse> getBookingByUserId(String userId);
}
