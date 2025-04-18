package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.booking.model.Location;
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

import static com.lenin.hotel.hotel.utils.HotelUtils.buildLocationResponse;

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

    @Override
    public List<LocationResponse> getAllLocation() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream().map(HotelUtils :: buildLocationResponse ).collect(Collectors.toList()) ;
    }
}
