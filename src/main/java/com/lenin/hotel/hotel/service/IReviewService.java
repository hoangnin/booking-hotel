package com.lenin.hotel.hotel.service;

import com.lenin.hotel.hotel.dto.request.ReviewRequest;
import com.lenin.hotel.hotel.dto.response.ReviewResponse;

import java.util.List;

public interface IReviewService {
    void createReview(ReviewRequest request);

    List<ReviewResponse> getReviewByHotel(Integer hotelId);
}
