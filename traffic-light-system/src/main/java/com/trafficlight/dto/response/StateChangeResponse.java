package com.trafficlight.dto.response;

import com.trafficlight.enums.LightState;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for state change operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class StateChangeResponse {
    
    private String trafficLightUuid;
    private String intersectionId;
    private LightState fromState;
    private LightState toState;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime changedAt;
    
    private Integer durationSeconds;
    private String reason;
    private String triggeredBy;
    private String correlationId;
    private Boolean success;
    private String message;
    private String stateHistoryUuid;
    
    // Default constructor
    public StateChangeResponse() {}
    
    // Constructor for successful state change
    public StateChangeResponse(String trafficLightUuid, LightState fromState, LightState toState, LocalDateTime changedAt) {
        this.trafficLightUuid = trafficLightUuid;
        this.fromState = fromState;
        this.toState = toState;
        this.changedAt = changedAt;
        this.success = true;
        this.message = "State changed successfully";
    }
    
    // Constructor for failed state change
    public StateChangeResponse(String trafficLightUuid, String errorMessage) {
        this.trafficLightUuid = trafficLightUuid;
        this.success = false;
        this.message = errorMessage;
    }
    
    // Static factory methods
    public static StateChangeResponse success(String trafficLightUuid, LightState fromState, LightState toState, LocalDateTime changedAt) {
        return new StateChangeResponse(trafficLightUuid, fromState, toState, changedAt);
    }
    
    public static StateChangeResponse failure(String trafficLightUuid, String errorMessage) {
        return new StateChangeResponse(trafficLightUuid, errorMessage);
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private StateChangeResponse response = new StateChangeResponse();
        
        public Builder trafficLightId(String trafficLightId) {
            response.trafficLightUuid = trafficLightId;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            response.intersectionId = intersectionId;
            return this;
        }
        
        public Builder fromState(LightState fromState) {
            response.fromState = fromState;
            return this;
        }
        
        public Builder toState(LightState toState) {
            response.toState = toState;
            return this;
        }
        
        public Builder changedAt(LocalDateTime changedAt) {
            response.changedAt = changedAt;
            return this;
        }
        
        public Builder durationSeconds(Integer durationSeconds) {
            response.durationSeconds = durationSeconds;
            return this;
        }
        
        public Builder reason(String reason) {
            response.reason = reason;
            return this;
        }
        
        public Builder triggeredBy(String triggeredBy) {
            response.triggeredBy = triggeredBy;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            response.correlationId = correlationId;
            return this;
        }
        
        public Builder success(Boolean success) {
            response.success = success;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder stateHistoryUuid(String stateHistoryUuid) {
            response.stateHistoryUuid = stateHistoryUuid;
            return this;
        }
        
        public StateChangeResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public String getTrafficLightUuid() { return trafficLightUuid; }
    public void setTrafficLightUuid(String trafficLightUuid) { this.trafficLightUuid = trafficLightUuid; }
    
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
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
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getStateHistoryUuid() { return stateHistoryUuid; }
    public void setStateHistoryUuid(String stateHistoryUuid) { this.stateHistoryUuid = stateHistoryUuid; }
    
    // Convenience methods
    public boolean isSuccessful() {
        return success != null && success;
    }
    
    public boolean isValidTransition() {
        return fromState != null && toState != null && fromState.canTransitionTo(toState);
    }
    
    @Override
    public String toString() {
        return "StateChangeResponse{" +
                "trafficLightUuid='" + trafficLightUuid + '\'' +
                ", fromState=" + fromState +
                ", toState=" + toState +
                ", changedAt=" + changedAt +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}