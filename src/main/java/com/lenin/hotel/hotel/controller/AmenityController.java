package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.dto.request.AmenityRequest;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class AmenityController {
    private final IAmenityService amenityService;
    @PostMapping("/admin/createAmenity")
    public ResponseEntity<?> createAmenity(@Valid @RequestBody AmenityRequest amenityRequest) {
        amenityService.createAmenity(amenityRequest);
        return ResponseEntity.ok().body(Map.of("message", "create amenity successfully!"));
    }

    @RequestMapping("/public/amenity")
    public ResponseEntity<?> getAllAmenities() {
        return ResponseEntity.ok().body(amenityService.getAll());
    }

}
