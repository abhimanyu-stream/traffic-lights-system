package com.trafficlight.exception;

/**
 * Base exception class for all traffic light system exceptions.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class TrafficLightException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] args;
    
    public TrafficLightException(String message) {
        super(message);
        this.errorCode = "TRAFFIC_LIGHT_ERROR";
        this.args = new Object[0];
    }
    
    public TrafficLightException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TRAFFIC_LIGHT_ERROR";
        this.args = new Object[0];
    }
    
    public TrafficLightException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }
    
    public TrafficLightException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public TrafficLightException(String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getArgs() {
        return args;
    }
}