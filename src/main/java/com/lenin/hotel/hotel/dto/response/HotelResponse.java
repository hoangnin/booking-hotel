package com.lenin.hotel.hotel.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.lenin.hotel.hotel.dto.request.ImageRequest;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
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
    private List<BookedDateRange> bookedDateRange;
    private Double longitude;
    private Double latitude;
    private String googleMapEmbed;
    private String location;
}
