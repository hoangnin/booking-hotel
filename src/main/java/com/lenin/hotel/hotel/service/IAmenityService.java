package com.lenin.hotel.hotel.service;

import com.lenin.hotel.hotel.dto.request.AmenityRequest;
import com.lenin.hotel.hotel.dto.response.AmenityResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IAmenityService {
    void createAmenity(@Valid AmenityRequest amenityRequest);

    List<AmenityResponse> getAll();
}
