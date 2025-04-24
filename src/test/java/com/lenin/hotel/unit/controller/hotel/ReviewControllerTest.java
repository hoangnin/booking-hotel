package com.lenin.hotel.unit.controller.hotel;

import com.lenin.hotel.hotel.controller.ReviewController;
import com.lenin.hotel.hotel.dto.request.ReviewRequest;
import com.lenin.hotel.hotel.dto.response.ReviewResponse;
import com.lenin.hotel.hotel.service.IReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    @Mock
    private IReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    void testCreateReview() {
        // Arrange
        ReviewRequest reviewRequest = ReviewRequest.builder().build();
        doNothing().when(reviewService).createReview(reviewRequest);

        // Act
        ResponseEntity<?> responseEntity = reviewController.createReview(reviewRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Map.of("message", "Review created"), responseEntity.getBody());
        verify(reviewService, times(1)).createReview(reviewRequest);
    }

    @Test
    void testGetAllReviews() {
        // Arrange
        Integer hotelId = 1;
        ReviewResponse reviewResponse1 = ReviewResponse.builder().build();
        ReviewResponse reviewResponse2 = ReviewResponse.builder().build();
        List<ReviewResponse> reviewList = List.of(reviewResponse1, reviewResponse2);

        when(reviewService.getReviewByHotel(hotelId)).thenReturn(reviewList);

        // Act
        ResponseEntity<?> responseEntity = reviewController.getAllReviews(hotelId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(reviewList, responseEntity.getBody());
        verify(reviewService, times(1)).getReviewByHotel(hotelId);
    }
}