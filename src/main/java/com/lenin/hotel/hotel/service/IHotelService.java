package com.lenin.hotel.hotel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lenin.hotel.hotel.dto.request.ChangePriceRequest;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.dto.request.HotelRequest;
import com.lenin.hotel.hotel.dto.response.HotelResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IHotelService {

    void createHotel(@Valid HotelRequest hotelRequest);

    List<HotelResponse> getAllHotel(Pageable pageable, Integer hotelId, Integer ownerId, Double latitude, Double longitude);
    PriceTracking getLatestPrice(Long hotelId);

    void activeHotelOwner(Integer hotelOwnerId);

    List<HotelResponse> searchHotels(String name, Integer locationId, Double rating, Set<Integer> amenityIds, Double minPrice, Double maxPrice,
                                     Integer minRoomsAvailable, String hotelType, ZonedDateTime checkIn, ZonedDateTime checkOut);

    void changePrice(ChangePriceRequest changePriceRequest);

    void generateAndSendHotelOwnerReport();

    Map<String, String> addFavorite(Integer hotelId);

    List<Integer> getAllUserFavorite();

    HotelResponse getHotelById(Integer id);

    void removeFavorite(Integer hotelId);

    List<Integer> getHotelsByHotelOwnerId();

    void updateHotel(Integer hotelId, JsonNode hotelJson);

    Object dashboardOverview();

    Object dashboardMonthlyBooking();

    Object hotelByLocation();

    Object getCombinedChartData();

    Object getTopHotelsByRevenue(int numTop);
}
