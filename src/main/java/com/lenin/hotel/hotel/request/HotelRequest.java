package com.lenin.hotel.hotel.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
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

    private Set<Integer> amenities;
    private List<ImageRequest> images;

    private PriceTrackingRequest price;
}
