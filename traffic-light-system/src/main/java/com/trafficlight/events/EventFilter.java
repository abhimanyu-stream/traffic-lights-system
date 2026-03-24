package com.trafficlight.events;

import com.trafficlight.enums.IntersectionStatus;
import com.trafficlight.enums.LightState;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * Event filtering logic for Kafka events.
 * Filters events based on business rules before processing.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class EventFilter {
    
    /**
     * Filter for state changed events.
     * Only process events for active intersections with valid state transitions.
     */
    public Predicate<StateChangedEvent> stateChangedFilter() {
        return event -> {
            if (event == null) {
                return false;
            }
            
            // Filter out events with null or invalid states
            if (event.getToState() == null || event.getFromState() == null) {
                return false;
            }
            
            // Filter out duplicate state changes
            if (event.getToState().equals(event.getFromState())) {
                return false;
            }
            
            // Only process valid state transitions
            return isValidStateTransition(event.getFromState(), event.getToState());
        };
    }
    
    /**
     * Filter for intersection updated events.
     * Only process events for active intersections.
     */
    public Predicate<IntersectionUpdatedEvent> intersectionUpdatedFilter() {
        return event -> {
            if (event == null) {
                return false;
            }
            
            // Filter out events for inactive intersections
            if (event.getStatus() == IntersectionStatus.INACTIVE) {
                return false;
            }
            
            return true;
        };
    }
    
    /**
     * Filter for system health events.
     * Only process critical and warning events.
     */
    public Predicate<SystemHealthEvent> systemHealthFilter() {
        return event -> {
            if (event == null) {
                return false;
            }
            
            // Filter out HEALTHY status events, only process degraded and critical
            String status = event.getHealthStatus();
            return "DEGRADED".equals(status) || "CRITICAL".equals(status) || "DOWN".equals(status);
        };
    }
    
    private boolean isValidStateTransition(LightState oldState, LightState newState) {
        // Valid transitions:
        // RED -> GREEN
        // GREEN -> YELLOW
        // YELLOW -> RED
        
        if (oldState == LightState.RED && newState == LightState.GREEN) {
            return true;
        }
        if (oldState == LightState.GREEN && newState == LightState.YELLOW) {
            return true;
        }
        if (oldState == LightState.YELLOW && newState == LightState.RED) {
            return true;
        }
        
        return false;
    }
}
