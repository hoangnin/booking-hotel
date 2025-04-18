package com.lenin.hotel.hotel.dto.request;

import com.lenin.hotel.common.anotation.ValidDateRange;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@ValidDateRange
@Data
@Builder
public class PriceTrackingRequest {

    @NotNull(message = "fromValid cannot be null")
    @FutureOrPresent(message = "fromValid must be in the present or future")
    private ZonedDateTime fromValid;

    @NotNull(message = "toValid cannot be null")
    @Future(message = "toValid must be in the future")
    private ZonedDateTime toValid;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    private BigDecimal price;
}
