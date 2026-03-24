package com.trafficlight.enums;

import java.util.Set;

/**
 * Enumeration representing the cardinal directions for traffic lights at an intersection.
 * 
 * This enum provides methods to determine conflicting directions and
 * manage traffic flow safety at intersections.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public enum Direction {
    
    /**
     * North direction
     */
    NORTH("NORTH"),
    
    /**
     * South direction
     */
    SOUTH("SOUTH"),
    
    /**
     * East direction
     */
    EAST("EAST"),
    
    /**
     * West direction
     */
    WEST("WEST");
    
    private final String displayName;
    
    /**
     * Constructor for Direction enum.
     * 
     * @param displayName the human-readable name of the direction
     */
    Direction(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Gets the display name of the direction.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the opposite direction.
     * 
     * @return the opposite direction
     */
    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
    
    /**
     * Gets the perpendicular directions (cross traffic).
     * 
     * @return set of perpendicular directions
     */
    public Set<Direction> getPerpendicularDirections() {
        return switch (this) {
            case NORTH, SOUTH -> Set.of(EAST, WEST);
            case EAST, WEST -> Set.of(NORTH, SOUTH);
        };
    }
    
    /**
     * Gets the parallel direction (same axis).
     * 
     * @return the parallel direction (opposite)
     */
    public Direction getParallelDirection() {
        return getOpposite();
    }
    
    /**
     * Checks if this direction conflicts with another direction.
     * 
     * Conflicting directions are perpendicular to each other.
     * For example, NORTH conflicts with EAST and WEST, but not with SOUTH.
     * 
     * @param other the other direction to check
     * @return true if the directions conflict, false otherwise
     */
    public boolean conflictsWith(Direction other) {
        return getPerpendicularDirections().contains(other);
    }
    
    /**
     * Checks if this direction is parallel to another direction.
     * 
     * Parallel directions are on the same axis (NORTH-SOUTH or EAST-WEST).
     * 
     * @param other the other direction to check
     * @return true if the directions are parallel, false otherwise
     */
    public boolean isParallelTo(Direction other) {
        return this == other || this == other.getOpposite();
    }
    
    /**
     * Gets all directions that can safely have green lights simultaneously with this direction.
     * 
     * @return set of safe concurrent directions (only the parallel direction)
     */
    public Set<Direction> getSafeConcurrentDirections() {
        return Set.of(getOpposite());
    }
    
    /**
     * Checks if this direction is on the north-south axis.
     * 
     * @return true if this is NORTH or SOUTH, false otherwise
     */
    public boolean isNorthSouthAxis() {
        return this == NORTH || this == SOUTH;
    }
    
    /**
     * Checks if this direction is on the east-west axis.
     * 
     * @return true if this is EAST or WEST, false otherwise
     */
    public boolean isEastWestAxis() {
        return this == EAST || this == WEST;
    }
}