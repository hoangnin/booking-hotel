package com.lenin.hotel.hotel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AmenityRequest {
    @NotBlank(message = "Amenity name cannot be blank")
    @Size(max = 100, message = "Amenity name must be at most 100 characters")
    private String name;


}
