package com.trafficlight.dto.response;

import com.trafficlight.enums.LightState;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for light sequence data.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class LightSequenceResponse {
    
    private Long id;
    private String uuid;
    private String intersectionId;
    private LightState state;
    private Integer durationSeconds;
    private Integer sequenceOrder;
    private String nextSequenceId;
    private Boolean isActive;
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    private Long version;
    
    // Computed fields
    private Boolean hasNextSequence;
    private Boolean isLastInCycle;
    
    // Default constructor
    public LightSequenceResponse() {}
    
    // Constructor with essential fields
    public LightSequenceResponse(Long id, String uuid, String intersectionId, LightState state, Integer durationSeconds, Integer sequenceOrder) {
        this.id = id;
        this.uuid = uuid;
        this.intersectionId = intersectionId;
        this.state = state;
        this.durationSeconds = durationSeconds;
        this.sequenceOrder = sequenceOrder;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public Boolean getHasNextSequence() { return hasNextSequence; }
    public void setHasNextSequence(Boolean hasNextSequence) { this.hasNextSequence = hasNextSequence; }
    
    public Boolean getIsLastInCycle() { return isLastInCycle; }
    public void setIsLastInCycle(Boolean isLastInCycle) { this.isLastInCycle = isLastInCycle; }
    
    // Convenience methods
    public boolean hasNext() {
        return nextSequenceId != null && !nextSequenceId.trim().isEmpty();
    }
    
    public boolean isLast() {
        return !hasNext();
    }
    
    @Override
    public String toString() {
        return "LightSequenceResponse{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", intersectionId='" + intersectionId + '\'' +
                ", state=" + state +
                ", durationSeconds=" + durationSeconds +
                ", sequenceOrder=" + sequenceOrder +
                ", isActive=" + isActive +
                '}';
    }
}