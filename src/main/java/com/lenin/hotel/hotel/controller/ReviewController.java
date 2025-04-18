package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.dto.request.ReviewRequest;
import com.lenin.hotel.hotel.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final IReviewService reviewService;

    @PostMapping("/user/review")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request){
        reviewService.createReview(request);
        return ResponseEntity.ok().body(Map.of("message", "Review created"));
    }

    @GetMapping("/public/review/{hotelId}")
    public ResponseEntity<?> getAllReviews(@PathVariable Integer hotelId){
        return ResponseEntity.ok().body(reviewService.getReviewByHotel(hotelId));
    }

}
