package com.trafficlight.utils;

import com.trafficlight.domain.Intersection;
import com.trafficlight.domain.TrafficLight;
import com.trafficlight.enums.Direction;
import com.trafficlight.enums.IntersectionStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Builder for creating Intersection objects with fluent API.
 * Implements the Builder pattern for complex object construction.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class IntersectionBuilder {
    
    private Long id;
    private String uuid;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private IntersectionStatus status;
    private boolean isActive;
    private Set<TrafficLight> trafficLights;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    
    private IntersectionBuilder() {
        // Default values
        this.uuid = UUID.randomUUID().toString();
        this.status = IntersectionStatus.ACTIVE;
        this.isActive = true;
        this.trafficLights = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 0L;
    }
    
    public static IntersectionBuilder builder() {
        return new IntersectionBuilder();
    }
    
    public IntersectionBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public IntersectionBuilder uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
    
    public IntersectionBuilder name(String name) {
        this.name = name;
        return this;
    }
    
    public IntersectionBuilder description(String description) {
        this.description = description;
        return this;
    }
    
    public IntersectionBuilder latitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }
    
    public IntersectionBuilder longitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }
    
    public IntersectionBuilder coordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }
    
    public IntersectionBuilder status(IntersectionStatus status) {
        this.status = status;
        return this;
    }
    
    public IntersectionBuilder active(boolean isActive) {
        this.isActive = isActive;
        return this;
    }
    
    public IntersectionBuilder addTrafficLight(TrafficLight trafficLight) {
        this.trafficLights.add(trafficLight);
        return this;
    }
    
    public IntersectionBuilder trafficLights(Set<TrafficLight> trafficLights) {
        this.trafficLights = trafficLights;
        return this;
    }
    
    public IntersectionBuilder withStandardFourWayLights() {
        for (Direction direction : Direction.values()) {
            TrafficLight light = TrafficLightBuilder.builder()
                .direction(direction)
                .build();
            this.trafficLights.add(light);
        }
        return this;
    }
    
    public IntersectionBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public IntersectionBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public IntersectionBuilder version(Long version) {
        this.version = version;
        return this;
    }
    
    public Intersection build() {
        validate();
        
        return Intersection.builder()
            .uuid(this.uuid)
            .name(this.name)
            .description(this.description)
            .latitude(this.latitude)
            .longitude(this.longitude)
            .status(this.status)
            .isActive(this.isActive)
            .build();
    }
    
    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("Intersection name is required");
        }
    }
}
