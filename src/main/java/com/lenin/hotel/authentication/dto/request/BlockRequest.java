package com.lenin.hotel.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlockRequest {
    @NotBlank(message = "Ban reason is required")
    private String banReason;
    @NotBlank(message = "User ID id required")
    private long userId;
}
