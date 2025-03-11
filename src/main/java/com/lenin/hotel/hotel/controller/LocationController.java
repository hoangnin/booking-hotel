package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.service.ILocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/location")
@RequiredArgsConstructor
public class LocationController {
    private final ILocationService locationService;
    @PostMapping("/create")
    public ResponseEntity<?> createLocation (@Valid @RequestBody Map<String,String> location) {
        locationService.createLocation(location);
        return ResponseEntity.ok().body(Map.of("message", "Location created successfully"));
    }
}
