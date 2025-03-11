package com.lenin.hotel.hotel.response;

import com.lenin.hotel.booking.enumuration.BookingStatus;
import lombok.Data;

@Data
public class BookingResponse {
    private String bookingId;
    private BookingStatus status;
    private String hotelId;
    private String note;
    private Integer userId;
    private String price;

}
