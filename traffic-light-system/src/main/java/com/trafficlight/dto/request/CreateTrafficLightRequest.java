package com.trafficlight.dto.request;

import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating a new traffic light.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class CreateTrafficLightRequest {
    
    @NotBlank(message = "Intersection ID is required")
    private String intersectionId;
    
    @NotNull(message = "Direction is required")
    private Direction direction;
    
    private LightState currentState = LightState.RED;
    
    @Positive(message = "State duration must be positive")
    private Integer stateDurationSeconds;
    
    private Boolean isActive = true;
    
    // Default constructor
    public CreateTrafficLightRequest() {}
    
    // Constructor with required fields
    public CreateTrafficLightRequest(String intersectionId, Direction direction) {
        this.intersectionId = intersectionId;
        this.direction = direction;
    }
    
    // Getters and Setters
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    
    public LightState getCurrentState() { return currentState; }
    public void setCurrentState(LightState currentState) { this.currentState = currentState; }
    
    public Integer getStateDurationSeconds() { return stateDurationSeconds; }
    public void setStateDurationSeconds(Integer stateDurationSeconds) { this.stateDurationSeconds = stateDurationSeconds; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    @Override
    public String toString() {
        return "CreateTrafficLightRequest{" +
                "intersectionId='" + intersectionId + '\'' +
                ", direction=" + direction +
                ", currentState=" + currentState +
                ", stateDurationSeconds=" + stateDurationSeconds +
                ", isActive=" + isActive +
                '}';
    }
}