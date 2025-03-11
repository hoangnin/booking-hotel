package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.booking.model.Location;
import com.lenin.hotel.hotel.repository.LocationRepository;
import com.lenin.hotel.hotel.service.ILocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationServiceImpl implements ILocationService {
    private final LocationRepository locationRepository;
    @Override
    public void createLocation(Map<String, String> locationRequest) {
        Location location = Location.builder()
                .name(locationRequest.get("name"))
                .build();
        locationRepository.save(location);

    }
}
