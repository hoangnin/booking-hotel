package com.lenin.hotel.hotel.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class HotelRequest {

    @NotBlank(message = "Hotel name cannot be blank")
    @Size(max = 100, message = "Hotel name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @NotNull(message = "Latitude cannot be null")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    private Double longitude;

    @NotBlank(message = "Google Map embed link cannot be blank")
    private String googleMapEmbed;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Policy cannot be blank")
    @Size(max = 1000, message = "Policy must be at most 1000 characters")
    private String policy;

    @NotNull(message = "Location cannot be blank")
    private Integer locationId;

    private String avatar;

    private Set<Integer> amenities;
    private List<ImageRequest> images;

    private PriceTrackingRequest price;
}
