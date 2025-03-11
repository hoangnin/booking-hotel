package com.lenin.hotel.hotel.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmenityResponse {
    private Integer id;
    private String name;
    private String icon;
}
