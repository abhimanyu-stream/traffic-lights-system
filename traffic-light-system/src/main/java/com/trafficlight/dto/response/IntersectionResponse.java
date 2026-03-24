package com.trafficlight.dto.response;

import com.trafficlight.enums.IntersectionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for intersection data.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class IntersectionResponse {
    
    private Long id;
    private String uuid;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private IntersectionStatus status;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    private Long version;
    private List<TrafficLightResponse> trafficLights;
    private Long activeTrafficLightCount;
    
    // Default constructor
    public IntersectionResponse() {}
    
    // Constructor with essential fields
    public IntersectionResponse(Long id, String uuid, String name, IntersectionStatus status, Boolean isActive) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.status = status;
        this.isActive = isActive;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private IntersectionResponse response = new IntersectionResponse();
        
        public Builder id(Long id) {
            response.id = id;
            return this;
        }
        
        public Builder uuid(String uuid) {
            response.uuid = uuid;
            return this;
        }
        
        public Builder name(String name) {
            response.name = name;
            return this;
        }
        
        public Builder description(String description) {
            response.description = description;
            return this;
        }
        
        public Builder latitude(Double latitude) {
            response.latitude = latitude;
            return this;
        }
        
        public Builder longitude(Double longitude) {
            response.longitude = longitude;
            return this;
        }
        
        public Builder status(IntersectionStatus status) {
            response.status = status;
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
        
        public IntersectionResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public IntersectionStatus getStatus() { return status; }
    public void setStatus(IntersectionStatus status) { this.status = status; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public List<TrafficLightResponse> getTrafficLights() { return trafficLights; }
    public void setTrafficLights(List<TrafficLightResponse> trafficLights) { this.trafficLights = trafficLights; }
    
    public Long getActiveTrafficLightCount() { return activeTrafficLightCount; }
    public void setActiveTrafficLightCount(Long activeTrafficLightCount) { this.activeTrafficLightCount = activeTrafficLightCount; }
    
    // Convenience methods
    public boolean isOperational() {
        return isActive != null && isActive && status == IntersectionStatus.ACTIVE;
    }
    
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
    
    @Override
    public String toString() {
        return "IntersectionResponse{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", isActive=" + isActive +
                ", activeTrafficLightCount=" + activeTrafficLightCount +
                '}';
    }
}