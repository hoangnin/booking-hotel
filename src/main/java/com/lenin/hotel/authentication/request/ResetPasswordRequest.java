package com.lenin.hotel.authentication.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 30, message = "Password must be between 8 and 30 characters")
    private String newPassword;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 30, message = "Password must be between 8 and 30 characters")
    private String confirmPassword;
}
