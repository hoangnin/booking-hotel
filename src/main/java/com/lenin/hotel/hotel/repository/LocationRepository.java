package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.booking.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Integer> {

}
