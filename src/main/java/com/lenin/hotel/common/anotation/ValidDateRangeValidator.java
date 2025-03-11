package com.lenin.hotel.common.anotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.lenin.hotel.hotel.request.PriceTrackingRequest;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, PriceTrackingRequest> {
    @Override
    public boolean isValid(PriceTrackingRequest request, ConstraintValidatorContext context) {
        if (request.getFromValid() == null || request.getToValid() == null) {
            return true; // Để @NotNull kiểm tra riêng
        }
        return request.getFromValid().isBefore(request.getToValid()); // fromValid phải trước toValid
    }
}