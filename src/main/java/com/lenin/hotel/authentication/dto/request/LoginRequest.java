package com.lenin.hotel.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
//    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 30, message = "Password must be between 8 and 30 characters")
//    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=.*[0-9]).{8,}$",
//            message = "Password must contain at least one uppercase letter, one special character (@#$%^&+=!), and one number")
    private String password;

}
