package com.lenin.hotel.hotel.dto.request;

import com.lenin.hotel.common.enumuration.ImageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageRequest {
    private String url;
    private ImageType type; // HOTEL, ROOM, COMMENT, AMENITY
    private String id;
}

