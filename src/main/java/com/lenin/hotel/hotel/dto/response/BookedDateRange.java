package com.lenin.hotel.hotel.dto.response;

import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookedDateRange {
    private ZonedDateTime checkIn;
    private ZonedDateTime checkOut;
}

