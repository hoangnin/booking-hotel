package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.service.ILocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocationController {
    private final ILocationService locationService;
    @PostMapping("/admin/location/create")
    public ResponseEntity<?> createLocation (@Valid @RequestBody Map<String,String> location) {
        locationService.createLocation(location);
        return ResponseEntity.ok().body(Map.of("message", "Location created successfully"));
    }
    @GetMapping("/public/location")
    public ResponseEntity<?> getAllLocations() {
        return ResponseEntity.ok().body(locationService.getAllLocation());
    }
}
