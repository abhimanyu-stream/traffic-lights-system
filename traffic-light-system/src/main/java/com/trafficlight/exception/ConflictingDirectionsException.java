package com.trafficlight.exception;

import com.trafficlight.enums.Direction;

import java.util.List;

/**
 * Exception thrown when conflicting traffic light directions are detected.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class ConflictingDirectionsException extends TrafficLightException {
    
    public ConflictingDirectionsException(String intersectionId, List<Direction> conflictingDirections) {
        super("CONFLICTING_DIRECTIONS", 
              "Conflicting directions detected at intersection " + intersectionId + ": " + conflictingDirections,
              intersectionId, conflictingDirections);
    }
    
    public ConflictingDirectionsException(String intersectionId, Direction direction1, Direction direction2) {
        super("CONFLICTING_DIRECTIONS", 
              "Conflicting directions detected at intersection " + intersectionId + ": " + direction1 + " and " + direction2,
              intersectionId, direction1, direction2);
    }
}