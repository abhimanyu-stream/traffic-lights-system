package com.trafficlight.exception;

/**
 * Exception thrown when a light sequence is not found.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class LightSequenceNotFoundException extends TrafficLightException {
    
    public LightSequenceNotFoundException(String sequenceId) {
        super("LIGHT_SEQUENCE_NOT_FOUND", "Light sequence not found: " + sequenceId, sequenceId);
    }
    
    public LightSequenceNotFoundException(String sequenceId, Throwable cause) {
        super("LIGHT_SEQUENCE_NOT_FOUND", "Light sequence not found: " + sequenceId, cause, sequenceId);
    }
}