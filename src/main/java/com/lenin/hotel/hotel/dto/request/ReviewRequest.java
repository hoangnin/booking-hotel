package com.lenin.hotel.hotel.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequest {
    private int rating;
    private String content;
    private Integer hotelId;
    private String imageUrl;
}
