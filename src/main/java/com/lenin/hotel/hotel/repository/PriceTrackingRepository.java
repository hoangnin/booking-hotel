package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.hotel.model.PriceTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface PriceTrackingRepository extends JpaRepository<PriceTracking, Integer> {
    Optional<PriceTracking> findTopByHotelIdOrderByCreateDtDesc(Long hotelId);
}
