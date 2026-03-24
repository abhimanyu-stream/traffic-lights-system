package com.trafficlight.dto.request;

import com.trafficlight.enums.LightState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for changing traffic light state.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class StateChangeRequest {
    
    @NotNull(message = "Target state is required")
    private LightState targetState;
    
    @Positive(message = "Duration must be positive")
    private Integer durationSeconds;
    
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;
    
    @Size(max = 100, message = "Triggered by cannot exceed 100 characters")
    private String triggeredBy;
    
    private String correlationId;
    
    private Boolean forceChange = false;
    
    // Default constructor
    public StateChangeRequest() {}
    
    // Constructor with required fields
    public StateChangeRequest(LightState targetState) {
        this.targetState = targetState;
    }
    
    // Constructor with state and duration
    public StateChangeRequest(LightState targetState, Integer durationSeconds) {
        this.targetState = targetState;
        this.durationSeconds = durationSeconds;
    }
    
    // Getters and Setters
    public LightState getTargetState() { return targetState; }
    public void setTargetState(LightState targetState) { this.targetState = targetState; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public Boolean getForceChange() { return forceChange; }
    public void setForceChange(Boolean forceChange) { this.forceChange = forceChange; }
    
    @Override
    public String toString() {
        return "StateChangeRequest{" +
                "targetState=" + targetState +
                ", durationSeconds=" + durationSeconds +
                ", reason='" + reason + '\'' +
                ", triggeredBy='" + triggeredBy + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", forceChange=" + forceChange +
                '}';
    }
}