package com.trafficlight.utils;

import com.trafficlight.domain.TrafficLight;
import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder for creating TrafficLight objects with fluent API.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class TrafficLightBuilder {
    
    private Long id;
    private String uuid;
    private Long intersectionId;
    private Direction direction;
    private LightState currentState;
    private LocalDateTime lastStateChange;
    private Integer stateDurationSeconds;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    
    private TrafficLightBuilder() {
        // Default values
        this.uuid = UUID.randomUUID().toString();
        this.currentState = LightState.RED;
        this.lastStateChange = LocalDateTime.now();
        this.stateDurationSeconds = 30;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 0L;
    }
    
    public static TrafficLightBuilder builder() {
        return new TrafficLightBuilder();
    }
    
    public TrafficLightBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public TrafficLightBuilder uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
    
    public TrafficLightBuilder intersectionId(Long intersectionId) {
        this.intersectionId = intersectionId;
        return this;
    }
    
    public TrafficLightBuilder direction(Direction direction) {
        this.direction = direction;
        return this;
    }
    
    public TrafficLightBuilder currentState(LightState currentState) {
        this.currentState = currentState;
        return this;
    }
    
    public TrafficLightBuilder lastStateChange(LocalDateTime lastStateChange) {
        this.lastStateChange = lastStateChange;
        return this;
    }
    
    public TrafficLightBuilder stateDurationSeconds(Integer stateDurationSeconds) {
        this.stateDurationSeconds = stateDurationSeconds;
        return this;
    }
    
    public TrafficLightBuilder active(boolean isActive) {
        this.isActive = isActive;
        return this;
    }
    
    public TrafficLightBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public TrafficLightBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public TrafficLightBuilder version(Long version) {
        this.version = version;
        return this;
    }
    
    public TrafficLight build() {
        validate();
        
        return TrafficLight.builder()
            .uuid(this.uuid)
            .intersectionId(this.intersectionId != null ? this.intersectionId.toString() : null)
            .direction(this.direction)
            .currentState(this.currentState)
            .lastStateChange(this.lastStateChange)
            .stateDurationSeconds(this.stateDurationSeconds)
            .isActive(this.isActive)
            .build();
    }
    
    private void validate() {
        if (direction == null) {
            throw new IllegalStateException("Traffic light direction is required");
        }
    }
}
