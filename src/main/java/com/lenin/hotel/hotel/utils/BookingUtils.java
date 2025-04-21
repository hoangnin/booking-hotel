package com.lenin.hotel.hotel.utils;

import com.lenin.hotel.common.enumuration.BookingStatus;
import com.lenin.hotel.hotel.model.Booking;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.hotel.dto.request.BookingRequest;
import com.lenin.hotel.hotel.dto.response.BookingResponse;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class BookingUtils {
    public static Booking buildBookingEntity(BookingRequest request){
        return Booking.builder()
                .note(request.getNote())
                .status(BookingStatus.PENDING)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .build();
    }
    public static BookingResponse buildBookingResponse(Booking booking){

        return BookingResponse.builder()
                .note(booking.getNote())
                .hotelId(booking.getHotel().getId())
                .hotelName(booking.getHotel().getName())
                .checkInDate(booking.getCheckIn())
                .checkOutDate(booking.getCheckOut())
                .status(booking.getStatus())
                .id(booking.getId())
                .totalBill(calculateTotalBill(booking))
                .build();
    }
    public static Long calculateTotalBill(Booking booking){
        ZonedDateTime checkIn = booking.getCheckIn();
        ZonedDateTime checkOut = booking.getCheckOut();

        long days = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (days <= 0) {
            throw new BusinessException("Check-out date must be after check-in date");
        }
        BigDecimal pricePerDay = booking.getPriceTracking().getPrice();
        Long amount = pricePerDay.multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(100)) // Stripe dùng đơn vị nhỏ nhất (cent)
                .longValue();
        return amount;
    }
}
