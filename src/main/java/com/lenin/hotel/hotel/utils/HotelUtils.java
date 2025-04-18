package com.lenin.hotel.hotel.utils;

import com.lenin.hotel.booking.model.Location;
import com.lenin.hotel.hotel.dto.response.*;
import com.lenin.hotel.hotel.model.*;
import com.lenin.hotel.hotel.dto.request.HotelRequest;
import com.lenin.hotel.hotel.dto.request.ImageRequest;
import com.lenin.hotel.hotel.dto.request.PriceTrackingRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public class HotelUtils {
    public static ReviewResponse buildReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }

    public static PriceTracking buildPriceTracking(PriceTrackingRequest price) {
        return PriceTracking.builder()
                .price(price.getPrice())
//                .validFrom(price.getFromValid())
//                .validTo(price.getToValid())
                .build();
    }

    public static HotelResponse buildHotelResponse(Hotel hotel, List<Image> images, BigDecimal currentPrice, List<BookedDateRange> bookedDateRanges) {
        return buildHotelResponse(hotel).toBuilder()
                .images(images.stream().map(HotelUtils::buildImageResponse).toList())
                .price(currentPrice)
                .bookedDateRange(bookedDateRanges)
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
                .reviews(hotel.getTotalReview())
                .rating(hotel.getRating())
                .longitude(hotel.getLongitude())
                .latitude(hotel.getLatitude())
                .googleMapEmbed(hotel.getGoogleMapEmbed())
                .location(hotel.getLocation().getName())
                .build();
    }

    private static final Function<Image, ImageRequest> IMAGE_MAPPER =
            image -> ImageRequest.builder()
                    .url(image.getUrl())
                    .type(image.getType())
                    .id(String.valueOf(image.getId()))
                    .build();

    private static final Function<Amenity, AmenityResponse> AMENITY_MAPPER =
            amenity -> AmenityResponse.builder()
                    .id(amenity.getId())
                    .name(amenity.getName())
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
                .rating(0.0)
                .avatar(hotelRequest.getAvatar())
                .longitude(hotelRequest.getLongitude())
                .latitude(hotelRequest.getLatitude())
                .googleMapEmbed(hotelRequest.getGoogleMapEmbed())
                .build();
    }
    public static LocationResponse buildLocationResponse(Location location) {
      return   LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .build();
    }
}
