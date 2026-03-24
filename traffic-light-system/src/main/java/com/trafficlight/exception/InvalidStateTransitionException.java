package com.trafficlight.exception;

import com.trafficlight.enums.LightState;

/**
 * Exception thrown when an invalid state transition is attempted.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class InvalidStateTransitionException extends TrafficLightException {
    
    public InvalidStateTransitionException(String lightId, LightState fromState, LightState toState) {
        super("INVALID_STATE_TRANSITION", 
              "Invalid state transition for traffic light " + lightId + ": " + fromState + " -> " + toState,
              lightId, fromState, toState);
    }
    
    public InvalidStateTransitionException(String lightId, LightState fromState, LightState toState, String reason) {
        super("INVALID_STATE_TRANSITION", 
              "Invalid state transition for traffic light " + lightId + ": " + fromState + " -> " + toState + ". Reason: " + reason,
              lightId, fromState, toState, reason);
    }
}