package com.lenin.hotel.hotel.repository;

import com.lenin.hotel.hotel.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface AmenityRepository extends JpaRepository<Amenity, Integer> {
}
