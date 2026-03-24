package com.trafficlight.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for Direction enum values.
 * Validates that the direction is one of the allowed values.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DirectionValidator.class)
@Documented
public @interface ValidDirection {
    
    String message() default "Invalid direction. Must be NORTH, SOUTH, EAST, or WEST";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
