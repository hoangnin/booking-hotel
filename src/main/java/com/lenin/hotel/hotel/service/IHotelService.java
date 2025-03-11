package com.lenin.hotel.hotel.service;

import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.request.HotelRequest;
import com.lenin.hotel.hotel.response.HotelResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IHotelService {

    void createHotel(@Valid HotelRequest hotelRequest);

    List<HotelResponse> getAllRoom(Pageable pageable, Integer hotelId, Integer ownerId);
    PriceTracking getPriceTracking(Integer hotelId);
}
