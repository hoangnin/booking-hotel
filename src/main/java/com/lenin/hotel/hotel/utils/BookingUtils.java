package com.lenin.hotel.hotel.utils;

import com.lenin.hotel.booking.enumuration.BookingStatus;
import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.hotel.request.BookingRequest;

public class BookingUtils {
    public static Booking buildBookingEntity(BookingRequest request){
        return Booking.builder()
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .build();
    }
}
