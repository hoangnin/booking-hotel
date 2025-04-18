package com.lenin.hotel.hotel.service;

import com.lenin.hotel.common.PagedResponse;
import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.dto.response.BookingResponse;
import jakarta.validation.Valid;

public interface IBookingService {
    BookingResponse createBooking(@Valid BookingRequest booking);
    PagedResponse<BookingResponse> getBookingByUser(int page, int size);

    BookingResponse getBookingById(Integer id);
}
