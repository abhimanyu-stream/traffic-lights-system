package com.trafficlight.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for LightState enum values.
 * Validates that the light state is one of the allowed values.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LightStateValidator.class)
@Documented
public @interface ValidLightState {
    
    String message() default "Invalid light state. Must be RED, YELLOW, or GREEN";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
