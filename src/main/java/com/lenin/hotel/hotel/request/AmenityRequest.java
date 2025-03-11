package com.lenin.hotel.hotel.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmenityRequest {
    @NotBlank(message = "Amenity name cannot be blank")
    @Size(max = 100, message = "Amenity name must be at most 100 characters")
    private String name;
    @NotBlank(message = "Amenity name cannot be blank")
    private String icon;

}
