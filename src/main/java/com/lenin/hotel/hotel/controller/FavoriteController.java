package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.service.IHotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FavoriteController {
    private final IHotelService hotelService;
    @GetMapping("/user/favorite/{hotelId}")
    public ResponseEntity<?> addFavorite(@PathVariable Integer hotelId) {
        return ResponseEntity.ok().body(hotelService.addFavorite(hotelId));
    }
    @GetMapping("/user/favorite")
    public ResponseEntity<?> getFavorites() {
        return ResponseEntity.ok().body(hotelService.getAllUserFavorite());
    }
    @DeleteMapping("/user/favorite/{hotelId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Integer hotelId) {
        hotelService.removeFavorite(hotelId);
        return ResponseEntity.ok().body(Map.of("message", "Successfully removed favorite"));
    }
}
