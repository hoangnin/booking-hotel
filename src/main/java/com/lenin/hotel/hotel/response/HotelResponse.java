package com.lenin.hotel.hotel.response;


import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.request.ImageRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder(toBuilder = true)
public class HotelResponse {
    private Integer id;

    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private Double rating;
    private Integer reviews;
    private String policy;
    private Long ownerId;
    private List<AmenityResponse> amenity;
    private List<ImageRequest> images;
    private BigDecimal price;
}
