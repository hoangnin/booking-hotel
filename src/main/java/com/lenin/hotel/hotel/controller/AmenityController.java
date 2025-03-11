package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.request.AmenityRequest;
import com.lenin.hotel.hotel.service.IAmenityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hotelOwner")
@RequiredArgsConstructor
public class AmenityController {
    private final IAmenityService amenityService;
    @PostMapping("/createAmenity")
    public ResponseEntity<?> createAmenity(@Valid @RequestBody AmenityRequest amenityRequest) {
        amenityService.createAmenity(amenityRequest);
        return ResponseEntity.ok().body(Map.of("message", "create amenity successfully!"));
    }
}
