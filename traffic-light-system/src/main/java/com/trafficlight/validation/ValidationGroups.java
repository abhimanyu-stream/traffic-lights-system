package com.trafficlight.validation;

/**
 * Validation groups for different scenarios.
 * Used with Bean Validation to apply different validation rules based on context.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class ValidationGroups {
    
    /**
     * Validation group for create operations.
     */
    public interface Create {}
    
    /**
     * Validation group for update operations.
     */
    public interface Update {}
    
    /**
     * Validation group for delete operations.
     */
    public interface Delete {}
    
    /**
     * Validation group for state change operations.
     */
    public interface StateChange {}
    
    /**
     * Validation group for configuration operations.
     */
    public interface Configuration {}
}
