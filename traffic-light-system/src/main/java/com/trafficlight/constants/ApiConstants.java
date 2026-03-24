package com.trafficlight.constants;

/**
 * API constants for Traffic Light Controller system.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public final class ApiConstants {
    
    private ApiConstants() {
        // Utility class - prevent instantiation
    }
    
    // Base API paths
    public static final String TRAFFIC_SERVICE_BASE = "/api/traffic-service";
    
    // HTTP Headers
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    // Default pagination values
    public static final int DEFAULT_SIZE = 20;
    
    // Response messages
    public static final String DELETED_MESSAGE = "Resource deleted successfully";
    
    // Traffic Light specific messages
    public static final String TRAFFIC_LIGHT_CREATED = "Traffic light created successfully";
    public static final String TRAFFIC_LIGHT_UPDATED = "Traffic light updated successfully";
    public static final String TRAFFIC_LIGHT_STATE_CHANGED = "Traffic light state changed successfully";
    
    // Intersection specific messages
    public static final String INTERSECTION_CREATED = "Intersection created successfully";
    public static final String INTERSECTION_UPDATED = "Intersection updated successfully";
    
    // System messages
    public static final String SYSTEM_HEALTHY = "System is healthy";
}