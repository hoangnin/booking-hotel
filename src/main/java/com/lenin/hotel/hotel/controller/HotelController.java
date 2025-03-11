package com.lenin.hotel.hotel.controller;

import com.lenin.hotel.hotel.request.HotelRequest;
import com.lenin.hotel.hotel.response.HotelResponse;
import com.lenin.hotel.hotel.service.IHotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {
    private final IHotelService hotelService;

    @PostMapping("/hotelOwner/createRoom")
    public ResponseEntity<?> createRoom(@Valid @RequestBody HotelRequest hotelRequest) {
        hotelService.createHotel(hotelRequest);
        return ResponseEntity.ok().body(Map.of("message", "Room created successfully!"));
    }




    @GetMapping("/public/getRoom")
    public ResponseEntity<?> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") Integer hotelId,
            @RequestParam(defaultValue = "") Integer ownerId
            ) {
        Pageable pageable = PageRequest.of(page, size);
        List<HotelResponse> rooms = hotelService.getAllRoom(pageable, hotelId, ownerId);
        return ResponseEntity.ok().body(rooms);
    }

}
