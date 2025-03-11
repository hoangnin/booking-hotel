package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b 
        WHERE b.hotel.id = :hotelId 
        AND (b.checkIn < :checkOut AND b.checkOut > :checkIn)
    """)
    boolean existsOverlappingBooking(@Param("hotelId") Integer hotelId,
                                     @Param("checkIn") ZonedDateTime checkIn,
                                     @Param("checkOut") ZonedDateTime checkOut);
}
