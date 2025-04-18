package com.lenin.hotel.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationResponse {
    private Integer id;
    private String name;
}
