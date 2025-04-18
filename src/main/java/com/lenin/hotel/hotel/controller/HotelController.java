package com.lenin.hotel.hotel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lenin.hotel.authentication.service.IUserService;
import com.lenin.hotel.hotel.dto.request.ChangePriceRequest;
import com.lenin.hotel.hotel.dto.request.HotelRequest;
import com.lenin.hotel.hotel.dto.response.HotelResponse;
import com.lenin.hotel.hotel.dto.response.UserResponse;
import com.lenin.hotel.hotel.service.IHotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HotelController {
    private final IHotelService hotelService;
    private final IUserService userService;


    @PostMapping("/hotelOwner/createHotel")
    public ResponseEntity<?> createRoom(@Valid @RequestBody HotelRequest hotelRequest) {
        hotelService.createHotel(hotelRequest);
        return ResponseEntity.ok().body(Map.of("message", "Room created successfully!"));
    }

    @GetMapping("/hotelOwner/hotels")
    public ResponseEntity<?> getHotel() {

       return ResponseEntity.ok().body(hotelService.getHotelsByHotelOwnerId());
    }

    @GetMapping("/admin/active/{hotelOwnerId}")
    public ResponseEntity<?> setHotelOwner(@PathVariable Integer hotelOwnerId) {
        hotelService.activeHotelOwner(hotelOwnerId);
        return ResponseEntity.ok().body(Map.of("message", "Activated successfully!"));
    }

    @GetMapping("/admin/dashboard/overview")
    public ResponseEntity<?> getHotelOverview() {
        return ResponseEntity.ok().body(hotelService.dashboardOverview());
    }

    @GetMapping("/admin/dashboard/monthlyBooking")
    public ResponseEntity<?> getHotelMonthlyBooking() {
        return ResponseEntity.ok().body(hotelService.dashboardMonthlyBooking());
    }
    @GetMapping("/admin/dashboard/hotelByLocation")
    public ResponseEntity<?> getHotelByLocation() {
        return ResponseEntity.ok().body(hotelService.hotelByLocation());
    }
    @GetMapping("/admin/dashboard/combinedChart")
    public ResponseEntity<?> getCombinedChartData() {
        return ResponseEntity.ok().body(hotelService.getCombinedChartData());
    }
    @GetMapping("/admin/dashboard/top-hotels-revenue")
    public ResponseEntity<?> getTopHotelsByRevenue(@RequestParam Integer numTop) {
        return ResponseEntity.ok().body(hotelService.getTopHotelsByRevenue(numTop));
    }


    @GetMapping("/public/getHotel")
    public ResponseEntity<?> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") Integer hotelId,
            @RequestParam(defaultValue = "") Integer ownerId,
            @RequestParam(defaultValue = "") Double latitude,
            @RequestParam(defaultValue = "") Double longitude
            ) {
        Pageable pageable = PageRequest.of(page, size);
        List<HotelResponse> rooms = hotelService.getAllHotel(pageable, hotelId, ownerId, latitude, longitude);
        return ResponseEntity.ok().body(rooms);
    }

    @GetMapping("/public/hotel/{id}")
    public ResponseEntity<?> getHotel(@PathVariable Integer id) {
        HotelResponse hotelResponse = hotelService.getHotelById(id);
        return ResponseEntity.ok().body(hotelResponse);
    }
    @GetMapping("/public/owner/{id}")
    public ResponseEntity<?> getHotelOwner(@PathVariable Integer id) {
        UserResponse userResponse = userService.getHotelOwner(id);
        return ResponseEntity.ok().body(userResponse);
    }

    @GetMapping("/public/hotel/search")
    public ResponseEntity<?> searchHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer locationId,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) Set<Integer> amenityIds,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minRoomsAvailable,
            @RequestParam(required = false) String hotelType,
            @RequestParam(required = false) ZonedDateTime checkIn,
            @RequestParam(required = false) ZonedDateTime checkOut) {

        return ResponseEntity.ok().body(hotelService.searchHotels(name, locationId, rating,
                amenityIds, minPrice, maxPrice, minRoomsAvailable, hotelType, checkIn, checkOut));
    }

    @PostMapping("/hotelOwner/changePrice")
    public ResponseEntity<?> changePrice(@Valid @RequestBody ChangePriceRequest changePriceRequest) {
        hotelService.changePrice(changePriceRequest);
        return ResponseEntity.ok().body(Map.of("message", "Price changed successfully!"));
    }

    @GetMapping("/hotelOwner/sendReport")
    public ResponseEntity<?> sendHotelOwnerReport() {
        try {
            hotelService.generateAndSendHotelOwnerReport();
            return ResponseEntity.ok().body(Map.of("message", "Report generated successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/hotelOwner/update/hotel/{id}")
    public ResponseEntity<?> updateHotel(
            @PathVariable("id") Integer hotelId,
            @RequestBody JsonNode hotelJson) {

        hotelService.updateHotel(hotelId, hotelJson);
        return ResponseEntity.ok().body(Map.of("message", "Hotel updated successfully!"));
    }


}
