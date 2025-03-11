package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Integer> {

    List<Hotel> findByOwner(User owner);

    List<Hotel> findAllByOwnerId(Integer ownerId);

    Integer owner(User owner);
}
