package com.trafficlight.enums;

/**
 * Enumeration representing the possible states of a traffic light.
 * 
 * Each state has a default duration in seconds for timing control.
 * This enum is used throughout the system for state management,
 * validation, and event processing.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public enum LightState {
    
    /**
     * Red light state - vehicles must stop
     * Default duration: 30 seconds
     */
    RED("RED", 30),
    
    /**
     * Yellow light state - vehicles should prepare to stop
     * Default duration: 5 seconds
     */
    YELLOW("YELLOW", 5),
    
    /**
     * Green light state - vehicles may proceed
     * Default duration: 25 seconds
     */
    GREEN("GREEN", 25);
    
    private final String displayName;
    private final int defaultDurationSeconds;
    
    /**
     * Constructor for LightState enum.
     * 
     * @param displayName the human-readable name of the state
     * @param defaultDurationSeconds the default duration for this state in seconds
     */
    LightState(String displayName, int defaultDurationSeconds) {
        this.displayName = displayName;
        this.defaultDurationSeconds = defaultDurationSeconds;
    }
    
    /**
     * Gets the display name of the light state.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the default duration for this light state.
     * 
     * @return the default duration in seconds
     */
    public int getDefaultDurationSeconds() {
        return defaultDurationSeconds;
    }
    
    /**
     * Checks if the current state can transition to the target state.
     * 
     * Valid transitions:
     * - RED -> GREEN
     * - GREEN -> YELLOW
     * - YELLOW -> RED
     * 
     * @param targetState the state to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(LightState targetState) {
        return switch (this) {
            case RED -> targetState == GREEN;
            case GREEN -> targetState == YELLOW;
            case YELLOW -> targetState == RED;
        };
    }
    
    /**
     * Gets the next state in the normal traffic light sequence.
     * 
     * @return the next state in the sequence
     */
    public LightState getNextState() {
        return switch (this) {
            case RED -> GREEN;
            case GREEN -> YELLOW;
            case YELLOW -> RED;
        };
    }
    
    /**
     * Checks if this state allows vehicle movement.
     * 
     * @return true if vehicles can move (GREEN), false otherwise
     */
    public boolean allowsMovement() {
        return this == GREEN;
    }
    
    /**
     * Checks if this state requires vehicles to stop.
     * 
     * @return true if vehicles must stop (RED), false otherwise
     */
    public boolean requiresStop() {
        return this == RED;
    }
    
    /**
     * Checks if this state is a warning state.
     * 
     * @return true if this is a warning state (YELLOW), false otherwise
     */
    public boolean isWarning() {
        return this == YELLOW;
    }
}