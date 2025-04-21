package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.common.enumuration.BookingStatus;
import com.lenin.hotel.hotel.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b 
        WHERE b.hotel.id = :hotelId 
        AND (b.checkIn < :checkOut AND b.checkOut > :checkIn)
    """)
    boolean existsOverlappingBooking(@Param("hotelId") Integer hotelId,
                                     @Param("checkIn") ZonedDateTime checkIn,
                                     @Param("checkOut") ZonedDateTime checkOut);

    Page<Booking> findAllByUser(User user, Pageable pageable);
    boolean existsByUserAndHotelId(User user, Integer hotelId);

    List<Booking> findByStatusAndCreateDtBetween(BookingStatus bookingStatus, ZonedDateTime start, ZonedDateTime end);

    long countByStatusAndCreateDtBetween(BookingStatus bookingStatus, ZonedDateTime start, ZonedDateTime end);
    @Query("SELECT YEAR(b.createDt), MONTH(b.createDt), COUNT(b) " +
            "FROM Booking b " +
            "WHERE b.createDt BETWEEN :from AND :to " +
            "GROUP BY YEAR(b.createDt), MONTH(b.createDt)")
    List<Object[]> countBookingsBetweenDates(@Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

    List<Booking> findByStatus(BookingStatus bookingStatus);
}
