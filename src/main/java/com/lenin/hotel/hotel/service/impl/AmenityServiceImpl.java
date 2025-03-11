package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.repository.AmenityRepository;
import com.lenin.hotel.hotel.request.AmenityRequest;
import com.lenin.hotel.hotel.service.IAmenityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenityServiceImpl implements IAmenityService {

    private final AmenityRepository amenityRepository;

    @Override
    public void createAmenity(AmenityRequest amenityRequest) {
        Amenity amenity = Amenity.builder()
                .name(amenityRequest.getName())
                .icon(amenityRequest.getIcon())
                .available(true)
                .build();
        amenityRepository.save(amenity);
    }
}
