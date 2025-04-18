package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.hotel.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findReviewsByHotelId(Integer id);
}
