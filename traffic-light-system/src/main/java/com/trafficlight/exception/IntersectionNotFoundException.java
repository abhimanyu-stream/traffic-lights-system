package com.trafficlight.exception;

/**
 * Exception thrown when an intersection is not found.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class IntersectionNotFoundException extends TrafficLightException {
    
    public IntersectionNotFoundException(String intersectionId) {
        super("INTERSECTION_NOT_FOUND", "Intersection not found: " + intersectionId, intersectionId);
    }
    
    public IntersectionNotFoundException(String intersectionId, Throwable cause) {
        super("INTERSECTION_NOT_FOUND", "Intersection not found: " + intersectionId, cause, intersectionId);
    }
}