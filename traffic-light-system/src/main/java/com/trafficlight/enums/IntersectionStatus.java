package com.trafficlight.enums;

/**
 * Enumeration representing the operational status of an intersection.
 * 
 * This enum is used to control the overall state of an intersection
 * and determine whether traffic light operations should be active.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public enum IntersectionStatus {
    
    /**
     * Intersection is active and traffic lights are operational
     */
    ACTIVE("ACTIVE", "Intersection is operational"),
    
    /**
     * Intersection is inactive - traffic lights are not operational
     */
    INACTIVE("INACTIVE", "Intersection is not operational"),
    
    /**
     * Intersection is under maintenance - traffic lights may be in safe mode
     */
    MAINTENANCE("MAINTENANCE", "Intersection is under maintenance"),
    
    /**
     * Intersection has an error condition - requires attention
     */
    ERROR("ERROR", "Intersection has an error condition"),
    
    /**
     * Intersection is in emergency mode - special handling required
     */
    EMERGENCY("EMERGENCY", "Intersection is in emergency mode");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructor for IntersectionStatus enum.
     * 
     * @param displayName the human-readable name of the status
     * @param description a description of what this status means
     */
    IntersectionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the display name of the status.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the status.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the intersection is operational in this status.
     * 
     * @return true if the intersection can operate normally, false otherwise
     */
    public boolean isOperational() {
        return this == ACTIVE;
    }
    
    /**
     * Checks if the intersection allows traffic light state changes.
     * 
     * @return true if state changes are allowed, false otherwise
     */
    public boolean allowsStateChanges() {
        return this == ACTIVE || this == EMERGENCY;
    }
    
    /**
     * Checks if the intersection requires immediate attention.
     * 
     * @return true if immediate attention is required, false otherwise
     */
    public boolean requiresAttention() {
        return this == ERROR || this == EMERGENCY;
    }
    
    /**
     * Checks if the intersection is in a safe mode.
     * 
     * @return true if in safe mode, false otherwise
     */
    public boolean isSafeMode() {
        return this == MAINTENANCE || this == ERROR;
    }
    
    /**
     * Gets the default safe state for traffic lights when intersection is in this status.
     * 
     * @return the safe light state for this intersection status
     */
    public LightState getSafeLightState() {
        return switch (this) {
            case ACTIVE -> null; // Normal operation - no default state
            case INACTIVE, MAINTENANCE, ERROR -> LightState.RED; // Safe default
            case EMERGENCY -> LightState.YELLOW; // Warning state
        };
    }
    
    /**
     * Checks if this status can transition to the target status.
     * 
     * @param targetStatus the status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(IntersectionStatus targetStatus) {
        // Define valid status transitions
        return switch (this) {
            case ACTIVE -> targetStatus != ACTIVE;
            case INACTIVE -> targetStatus == ACTIVE || targetStatus == MAINTENANCE;
            case MAINTENANCE -> targetStatus == ACTIVE || targetStatus == ERROR;
            case ERROR -> targetStatus == MAINTENANCE || targetStatus == INACTIVE;
            case EMERGENCY -> true; // Emergency can transition to any state
        };
    }
}