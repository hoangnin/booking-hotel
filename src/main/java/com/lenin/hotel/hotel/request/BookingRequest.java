package com.lenin.hotel.hotel.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class BookingRequest {
    @NotNull(message = "Hotel ID cannot be null")
    private Integer hotelId;

    private String note;

    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private ZonedDateTime checkIn;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private ZonedDateTime checkOut;

    @AssertTrue(message = "Check-in must be before check-out")
    public boolean isCheckInBeforeCheckOut() {
        return checkIn != null && checkOut != null && checkIn.isBefore(checkOut);
    }

}
