package com.lenin.hotel.common.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "toValid must be after fromValid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
