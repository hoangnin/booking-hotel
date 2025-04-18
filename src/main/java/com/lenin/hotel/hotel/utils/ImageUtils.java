package com.lenin.hotel.hotel.utils;


import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.dto.request.ImageRequest;

public class ImageUtils {
    public static Image buildImage(ImageRequest imageRequest, Integer referenceId, String referenceTable) {
        return Image.builder()
                .url(imageRequest.getUrl())
                .type(imageRequest.getType())
                .referenceId(referenceId)
                .referenceTable(referenceTable)
                .build();
    }
}
