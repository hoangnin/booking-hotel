package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.hotel.model.Location;
import com.lenin.hotel.hotel.dto.response.LocationResponse;
import com.lenin.hotel.hotel.repository.LocationRepository;
import com.lenin.hotel.hotel.service.ILocationService;
import com.lenin.hotel.hotel.utils.HotelUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationServiceImpl implements ILocationService {
    private final LocationRepository locationRepository;
    @Override
    public void createLocation(Map<String, String> locationRequest) {
        String locationName = locationRequest.get("name");
        if (locationRepository.existsByName(locationName)) {
            throw new IllegalArgumentException("Location with name '" + locationName + "' already exists.");
        }
        Location location = Location.builder()
                .name(locationName)
                .build();
        locationRepository.save(location);
    }

    @Override
    public List<LocationResponse> getAllLocation() {
        List<Location> locations = locationRepository.findAll();
        if (locations.isEmpty()) {
            return List.of(); // Return an empty list
        }
        return locations.stream().map(HotelUtils::buildLocationResponse).collect(Collectors.toList());
    }
}
