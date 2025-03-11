package com.lenin.hotel.hotel.service;

import com.lenin.hotel.hotel.request.AmenityRequest;
import jakarta.validation.Valid;

public interface IAmenityService {
    void createAmenity(@Valid AmenityRequest amenityRequest);
}
