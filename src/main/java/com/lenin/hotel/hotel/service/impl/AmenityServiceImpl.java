package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.hotel.dto.response.AmenityResponse;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.repository.AmenityRepository;
import com.lenin.hotel.hotel.dto.request.AmenityRequest;
import com.lenin.hotel.hotel.service.IAmenityService;
import com.lenin.hotel.hotel.utils.HotelUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenityServiceImpl implements IAmenityService {

    private final AmenityRepository amenityRepository;

    @Override
    public void createAmenity(AmenityRequest amenityRequest) {
        // Check if an amenity with the same name already exists
        Amenity existingAmenity = amenityRepository.findByName(amenityRequest.getName());
        if (existingAmenity != null) {
            throw new BusinessException("Amenity with the same name already exists.");
        }

        // Create and save the new amenity
        Amenity amenity = Amenity.builder()
                .name(amenityRequest.getName())
                .available(true)
                .build();
        amenityRepository.save(amenity);
    }

    @Override
    public List<AmenityResponse> getAll() {
        return amenityRepository.findAll().stream().map(HotelUtils::buildAmenityResponse).toList();
    }
}
