package com.lenin.hotel.hotel.utils;


import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.request.ImageRequest;

public class ImageUtils {
    public static Image buildImage(ImageRequest imageRequest, Hotel hotel) {
        return Image.builder()
                .url(imageRequest.getUrl())
                .type(imageRequest.getType())
                .referenceId(hotel.getId())
                .referenceTable("hotel")
                .build();
    }
}
