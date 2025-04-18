package com.lenin.hotel.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Email(message = "Invalid email format")
    private String email; // Optional, but must be valid if provided

    private String address; // Optional, no validation required

    @Pattern(
            regexp = "^(0[0-9]{9})$",
            message = "Phone number must start with 0 and contain exactly 10 digits"
    )
    private String phoneNumber; // Optional, but must be valid if provided

    @Pattern(
            regexp = "^(https?://.*\\.(?:png|jpg|jpeg|gif|webp))$",
            message = "Invalid avatar URL format"
    )
    private String avatarUrl; // Optional, but must be valid if provided
}
