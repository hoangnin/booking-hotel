package com.lenin.hotel.hotel.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChangePriceRequest {
    @NotNull(message = "Hotel ID is required")
    private Integer hotelId;

    @NotNull(message = "New price is required")
    private BigDecimal newPrice;
}