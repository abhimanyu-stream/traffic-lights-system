package com.trafficlight.dto.request;

import com.trafficlight.enums.LightState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new light sequence.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class CreateLightSequenceRequest {
    
    @NotBlank(message = "Intersection ID is required")
    private String intersectionId;
    
    @NotNull(message = "Light state is required")
    private LightState state;
    
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer durationSeconds;
    
    @NotNull(message = "Sequence order is required")
    @Positive(message = "Sequence order must be positive")
    private Integer sequenceOrder;
    
    private String nextSequenceId;
    
    private Boolean isActive = true;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    // Default constructor
    public CreateLightSequenceRequest() {}
    
    // Constructor with required fields
    public CreateLightSequenceRequest(String intersectionId, LightState state, Integer durationSeconds, Integer sequenceOrder) {
        this.intersectionId = intersectionId;
        this.state = state;
        this.durationSeconds = durationSeconds;
        this.sequenceOrder = sequenceOrder;
    }
    
    // Getters and Setters
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
    public LightState getState() { return state; }
    public void setState(LightState state) { this.state = state; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    
    public String getNextSequenceId() { return nextSequenceId; }
    public void setNextSequenceId(String nextSequenceId) { this.nextSequenceId = nextSequenceId; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public String toString() {
        return "CreateLightSequenceRequest{" +
                "intersectionId='" + intersectionId + '\'' +
                ", state=" + state +
                ", durationSeconds=" + durationSeconds +
                ", sequenceOrder=" + sequenceOrder +
                ", nextSequenceId='" + nextSequenceId + '\'' +
                ", isActive=" + isActive +
                ", description='" + description + '\'' +
                '}';
    }
}