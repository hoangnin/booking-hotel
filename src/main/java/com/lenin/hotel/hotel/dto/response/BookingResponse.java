package com.lenin.hotel.hotel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lenin.hotel.booking.enumuration.BookingStatus;
import com.lenin.hotel.hotel.model.PriceTracking;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {
    private Integer id;
    private String hotelName;
    private BookingStatus status;
    private Integer hotelId;
    private ZonedDateTime checkInDate;
    private ZonedDateTime checkOutDate;
    private String note;
    private Long totalBill;
    private String paymentUrl;

}
