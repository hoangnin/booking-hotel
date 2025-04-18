package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Integer>, JpaSpecificationExecutor<Hotel> {

    List<Hotel> findByOwner(User owner);

    List<Hotel> findAllByOwnerId(Integer ownerId);

    Integer owner(User owner);
    @Query(value = """
    SELECT *, (
        6371 * acos(
            cos(radians(:latitude)) * cos(radians(h.latitude)) *
            cos(radians(h.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(h.latitude))
        )
    ) AS distance
    FROM hotels h
    ORDER BY distance
    LIMIT :limit OFFSET :offset
""", nativeQuery = true)
    List<Hotel> findNearbyHotels(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

}
