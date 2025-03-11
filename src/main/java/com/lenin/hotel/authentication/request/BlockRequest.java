package com.lenin.hotel.authentication.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlockRequest {
    @NotBlank(message = "Ban reason is required")
    private String banReason;
    @NotBlank(message = "User ID id required")
    private long userId;
}
