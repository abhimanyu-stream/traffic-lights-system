package com.trafficlight.dto.response;

import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for traffic light data.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class TrafficLightResponse {
    
    private Long id;
    private String uuid;
    private String intersectionId;
    private Direction direction;
    private LightState currentState;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime lastStateChange;
    
    private Integer stateDurationSeconds;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    private Long version;
    
    // Computed fields
    private Long timeSinceLastChangeSeconds;
    private Boolean hasExceededDuration;
    private Boolean isAllowingMovement;
    
    // Default constructor
    public TrafficLightResponse() {}
    
    // Constructor with essential fields
    public TrafficLightResponse(Long id, String uuid, String intersectionId, Direction direction, LightState currentState) {
        this.id = id;
        this.uuid = uuid;
        this.intersectionId = intersectionId;
        this.direction = direction;
        this.currentState = currentState;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private TrafficLightResponse response = new TrafficLightResponse();
        
        public Builder id(Long id) {
            response.id = id;
            return this;
        }
        
        public Builder uuid(String uuid) {
            response.uuid = uuid;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            response.intersectionId = intersectionId;
            return this;
        }
        
        public Builder direction(Direction direction) {
            response.direction = direction;
            return this;
        }
        
        public Builder currentState(LightState currentState) {
            response.currentState = currentState;
            return this;
        }
        
        public Builder lastStateChange(LocalDateTime lastStateChange) {
            response.lastStateChange = lastStateChange;
            return this;
        }
        
        public Builder stateDurationSeconds(Integer stateDurationSeconds) {
            response.stateDurationSeconds = stateDurationSeconds;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            response.isActive = isActive;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }
        
        public Builder version(Long version) {
            response.version = version;
            return this;
        }
        
        public TrafficLightResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    
    public LightState getCurrentState() { return currentState; }
    public void setCurrentState(LightState currentState) { this.currentState = currentState; }
    
    public LocalDateTime getLastStateChange() { return lastStateChange; }
    public void setLastStateChange(LocalDateTime lastStateChange) { this.lastStateChange = lastStateChange; }
    
    public Integer getStateDurationSeconds() { return stateDurationSeconds; }
    public void setStateDurationSeconds(Integer stateDurationSeconds) { this.stateDurationSeconds = stateDurationSeconds; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public Long getTimeSinceLastChangeSeconds() { return timeSinceLastChangeSeconds; }
    public void setTimeSinceLastChangeSeconds(Long timeSinceLastChangeSeconds) { this.timeSinceLastChangeSeconds = timeSinceLastChangeSeconds; }
    
    public Boolean getHasExceededDuration() { return hasExceededDuration; }
    public void setHasExceededDuration(Boolean hasExceededDuration) { this.hasExceededDuration = hasExceededDuration; }
    
    public Boolean getIsAllowingMovement() { return isAllowingMovement; }
    public void setIsAllowingMovement(Boolean isAllowingMovement) { this.isAllowingMovement = isAllowingMovement; }
    
    // Convenience methods
    public boolean canTransitionTo(LightState targetState) {
        return currentState != null && currentState.canTransitionTo(targetState);
    }
    
    public boolean isOperational() {
        return isActive != null && isActive;
    }
    
    @Override
    public String toString() {
        return "TrafficLightResponse{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", intersectionId='" + intersectionId + '\'' +
                ", direction=" + direction +
                ", currentState=" + currentState +
                ", isActive=" + isActive +
                ", timeSinceLastChangeSeconds=" + timeSinceLastChangeSeconds +
                '}';
    }
}