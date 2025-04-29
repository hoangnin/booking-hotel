package com.lenin.hotel.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private int id;
    private int rating;
    private String content;
    private String url;
    private UserReviewResponse userReview;
}
