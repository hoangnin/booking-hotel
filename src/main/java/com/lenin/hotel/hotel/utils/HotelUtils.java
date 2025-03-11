package com.lenin.hotel.hotel.utils;

import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.request.HotelRequest;
import com.lenin.hotel.hotel.request.ImageRequest;
import com.lenin.hotel.hotel.request.PriceTrackingRequest;
import com.lenin.hotel.hotel.response.AmenityResponse;
import com.lenin.hotel.hotel.response.HotelResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public class HotelUtils {
    public static PriceTracking buildPriceTracking(PriceTrackingRequest price) {
        return PriceTracking.builder()
                .price(price.getPrice())
                .currency(price.getCurrency())
                .validFrom(price.getFromValid())
                .validTo(price.getToValid())
                .build();
    }

    public static HotelResponse buildHotelResponse(Hotel hotel, List<Image> images, BigDecimal currentPrice) {
        return buildHotelResponse(hotel).toBuilder()
                .images(images.stream().map(HotelUtils::buildImageResponse).toList())
                .price(currentPrice)
                .build();
    }

    public static HotelResponse buildHotelResponse(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .policy(hotel.getPolicy())
                .ownerId(hotel.getOwner().getId())
                .amenity(hotel.getAmenities().stream()
                        .map(HotelUtils::buildAmenityResponse)
                        .toList())
                .build();
    }

    private static final Function<Image, ImageRequest> IMAGE_MAPPER =
            image -> ImageRequest.builder()
                    .url(image.getUrl())
                    .type(image.getType())
                    .build();

    private static final Function<Amenity, AmenityResponse> AMENITY_MAPPER =
            amenity -> AmenityResponse.builder()
                    .id(amenity.getId())
                    .name(amenity.getName())
                    .icon(amenity.getIcon())
                    .build();

    public static ImageRequest buildImageResponse(Image image) {
        return IMAGE_MAPPER.apply(image);
    }

    public static AmenityResponse buildAmenityResponse(Amenity amenity) {
        return AMENITY_MAPPER.apply(amenity);
    }

    public static Hotel buildHotel(HotelRequest hotelRequest) {
        return Hotel.builder()
                .name(hotelRequest.getName())
                .description(hotelRequest.getDescription())
                .address(hotelRequest.getAddress())
                .phone(hotelRequest.getPhone())
                .email(hotelRequest.getEmail())
                .policy(hotelRequest.getPolicy())
                .build();
    }
}
