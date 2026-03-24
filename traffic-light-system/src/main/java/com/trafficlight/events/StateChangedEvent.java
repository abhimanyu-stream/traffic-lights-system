package com.trafficlight.events;

import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event published when a traffic light state changes.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class StateChangedEvent {
    
    private String eventId;
    private String trafficLightId;
    private String intersectionId;
    private Direction direction;
    private LightState fromState;
    private LightState toState;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime changedAt;
    
    private Integer durationSeconds;
    private String reason;
    private String triggeredBy;
    private String correlationId;
    private String source;
    private String version;
    
    // Default constructor for JSON deserialization
    public StateChangedEvent() {}
    
    // Constructor with required fields
    public StateChangedEvent(String trafficLightId, String intersectionId, Direction direction, 
                           LightState fromState, LightState toState, LocalDateTime changedAt) {
        this.trafficLightId = trafficLightId;
        this.intersectionId = intersectionId;
        this.direction = direction;
        this.fromState = fromState;
        this.toState = toState;
        this.changedAt = changedAt;
        this.source = "traffic-light-controller";
        this.version = "1.0";
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private StateChangedEvent event = new StateChangedEvent();
        
        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }
        
        public Builder trafficLightId(String trafficLightId) {
            event.trafficLightId = trafficLightId;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            event.intersectionId = intersectionId;
            return this;
        }
        
        public Builder direction(Direction direction) {
            event.direction = direction;
            return this;
        }
        
        public Builder fromState(LightState fromState) {
            event.fromState = fromState;
            return this;
        }
        
        public Builder toState(LightState toState) {
            event.toState = toState;
            return this;
        }
        
        public Builder changedAt(LocalDateTime changedAt) {
            event.changedAt = changedAt;
            return this;
        }
        
        public Builder durationSeconds(Integer durationSeconds) {
            event.durationSeconds = durationSeconds;
            return this;
        }
        
        public Builder reason(String reason) {
            event.reason = reason;
            return this;
        }
        
        public Builder triggeredBy(String triggeredBy) {
            event.triggeredBy = triggeredBy;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            event.correlationId = correlationId;
            return this;
        }
        
        public Builder source(String source) {
            event.source = source;
            return this;
        }
        
        public Builder version(String version) {
            event.version = version;
            return this;
        }
        
        public StateChangedEvent build() {
            // Set defaults if not provided
            if (event.changedAt == null) {
                event.changedAt = LocalDateTime.now();
            }
            if (event.source == null) {
                event.source = "traffic-light-controller";
            }
            if (event.version == null) {
                event.version = "1.0";
            }
            return event;
        }
    }
    
    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public String getTrafficLightId() { return trafficLightId; }
    public void setTrafficLightId(String trafficLightId) { this.trafficLightId = trafficLightId; }
    
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    
    public LightState getFromState() { return fromState; }
    public void setFromState(LightState fromState) { this.fromState = fromState; }
    
    public LightState getToState() { return toState; }
    public void setToState(LightState toState) { this.toState = toState; }
    
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    // Convenience methods
    public boolean isValidTransition() {
        return fromState != null && toState != null && fromState.canTransitionTo(toState);
    }
    
    public boolean isToGreenState() {
        return LightState.GREEN.equals(toState);
    }
    
    public boolean isToRedState() {
        return LightState.RED.equals(toState);
    }
    
    public boolean isToYellowState() {
        return LightState.YELLOW.equals(toState);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateChangedEvent that = (StateChangedEvent) o;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(trafficLightId, that.trafficLightId) &&
               Objects.equals(changedAt, that.changedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId, trafficLightId, changedAt);
    }
    
    @Override
    public String toString() {
        return "StateChangedEvent{" +
                "eventId='" + eventId + '\'' +
                ", trafficLightId='" + trafficLightId + '\'' +
                ", intersectionId='" + intersectionId + '\'' +
                ", direction=" + direction +
                ", fromState=" + fromState +
                ", toState=" + toState +
                ", changedAt=" + changedAt +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}