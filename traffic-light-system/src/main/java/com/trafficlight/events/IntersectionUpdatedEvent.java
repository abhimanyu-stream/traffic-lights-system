package com.trafficlight.events;

import com.trafficlight.enums.IntersectionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event published when an intersection is updated.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class IntersectionUpdatedEvent {
    
    private String eventId;
    private String intersectionId;
    private String name;
    private Double latitude;
    private Double longitude;
    private IntersectionStatus status;
    private IntersectionStatus previousStatus;
    private Boolean isActive;
    private Boolean previousIsActive;
    private String updateType; // CREATED, UPDATED, DELETED, STATUS_CHANGED
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
    
    private String updatedBy;
    private String reason;
    private String correlationId;
    private String source;
    private String version;
    
    // Default constructor for JSON deserialization
    public IntersectionUpdatedEvent() {}
    
    // Constructor with required fields
    public IntersectionUpdatedEvent(String intersectionId, String name, IntersectionStatus status, 
                                  String updateType, LocalDateTime updatedAt) {
        this.intersectionId = intersectionId;
        this.name = name;
        this.status = status;
        this.updateType = updateType;
        this.updatedAt = updatedAt;
        this.source = "traffic-light-controller";
        this.version = "1.0";
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private IntersectionUpdatedEvent event = new IntersectionUpdatedEvent();
        
        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            event.intersectionId = intersectionId;
            return this;
        }
        
        public Builder name(String name) {
            event.name = name;
            return this;
        }
        
        public Builder latitude(Double latitude) {
            event.latitude = latitude;
            return this;
        }
        
        public Builder longitude(Double longitude) {
            event.longitude = longitude;
            return this;
        }
        
        public Builder status(IntersectionStatus status) {
            event.status = status;
            return this;
        }
        
        public Builder previousStatus(IntersectionStatus previousStatus) {
            event.previousStatus = previousStatus;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            event.isActive = isActive;
            return this;
        }
        
        public Builder previousIsActive(Boolean previousIsActive) {
            event.previousIsActive = previousIsActive;
            return this;
        }
        
        public Builder updateType(String updateType) {
            event.updateType = updateType;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            event.updatedAt = updatedAt;
            return this;
        }
        
        public Builder updatedBy(String updatedBy) {
            event.updatedBy = updatedBy;
            return this;
        }
        
        public Builder reason(String reason) {
            event.reason = reason;
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
        
        public IntersectionUpdatedEvent build() {
            // Set defaults if not provided
            if (event.updatedAt == null) {
                event.updatedAt = LocalDateTime.now();
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
    
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public IntersectionStatus getStatus() { return status; }
    public void setStatus(IntersectionStatus status) { this.status = status; }
    
    public IntersectionStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(IntersectionStatus previousStatus) { this.previousStatus = previousStatus; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getPreviousIsActive() { return previousIsActive; }
    public void setPreviousIsActive(Boolean previousIsActive) { this.previousIsActive = previousIsActive; }
    
    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    // Convenience methods
    public boolean isCreated() {
        return "CREATED".equals(updateType);
    }
    
    public boolean isUpdated() {
        return "UPDATED".equals(updateType);
    }
    
    public boolean isDeleted() {
        return "DELETED".equals(updateType);
    }
    
    public boolean isStatusChanged() {
        return "STATUS_CHANGED".equals(updateType) || 
               (previousStatus != null && !previousStatus.equals(status));
    }
    
    public boolean isActivationChanged() {
        return previousIsActive != null && !previousIsActive.equals(isActive);
    }
    
    public boolean isBecameOperational() {
        return IntersectionStatus.ACTIVE.equals(status) && 
               !IntersectionStatus.ACTIVE.equals(previousStatus);
    }
    
    public boolean isBecameNonOperational() {
        return !IntersectionStatus.ACTIVE.equals(status) && 
               IntersectionStatus.ACTIVE.equals(previousStatus);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntersectionUpdatedEvent that = (IntersectionUpdatedEvent) o;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(intersectionId, that.intersectionId) &&
               Objects.equals(updatedAt, that.updatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId, intersectionId, updatedAt);
    }
    
    @Override
    public String toString() {
        return "IntersectionUpdatedEvent{" +
                "eventId='" + eventId + '\'' +
                ", intersectionId='" + intersectionId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", updateType='" + updateType + '\'' +
                ", updatedAt=" + updatedAt +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}