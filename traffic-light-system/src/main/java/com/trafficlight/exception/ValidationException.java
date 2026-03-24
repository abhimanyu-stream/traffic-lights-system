package com.trafficlight.exception;

import java.util.List;

/**
 * Exception thrown when validation errors occur.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class ValidationException extends TrafficLightException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message, List<String> validationErrors) {
        super("VALIDATION_ERROR", message);
        this.validationErrors = validationErrors;
    }
    
    public ValidationException(List<String> validationErrors) {
        super("VALIDATION_ERROR", "Validation failed with " + validationErrors.size() + " error(s)");
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}