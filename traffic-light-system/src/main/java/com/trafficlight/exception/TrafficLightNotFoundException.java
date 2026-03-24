package com.trafficlight.exception;

/**
 * Exception thrown when a traffic light is not found.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class TrafficLightNotFoundException extends TrafficLightException {
    
    public TrafficLightNotFoundException(String lightId) {
        super("TRAFFIC_LIGHT_NOT_FOUND", "Traffic light not found: " + lightId, lightId);
    }
    
    public TrafficLightNotFoundException(String lightId, Throwable cause) {
        super("TRAFFIC_LIGHT_NOT_FOUND", "Traffic light not found: " + lightId, cause, lightId);
    }
}