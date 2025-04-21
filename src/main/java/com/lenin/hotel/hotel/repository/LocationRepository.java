package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.hotel.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    boolean existsByName(String name);
}
