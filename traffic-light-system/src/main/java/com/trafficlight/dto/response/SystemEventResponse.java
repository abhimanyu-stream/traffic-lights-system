package com.trafficlight.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for system event data.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class SystemEventResponse {
    
    private Long id;
    private String uuid;
    private String eventType;
    private String level;
    private String message;
    private String details;
    private String source;
    private String intersectionId;
    private String trafficLightId;
    private String correlationId;
    private String userId;
    private String sessionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime occurredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime resolvedAt;
    
    private Boolean isResolved;
    
    // Default constructor
    public SystemEventResponse() {}
    
    // Constructor with essential fields
    public SystemEventResponse(Long id, String uuid, String eventType, String level, String message, LocalDateTime occurredAt) {
        this.id = id;
        this.uuid = uuid;
        this.eventType = eventType;
        this.level = level;
        this.message = message;
        this.occurredAt = occurredAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }
    
    public String getTrafficLightId() { return trafficLightId; }
    public void setTrafficLightId(String trafficLightId) { this.trafficLightId = trafficLightId; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
    
    // Convenience methods
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(level);
    }
    
    public boolean isWarning() {
        return "WARN".equalsIgnoreCase(level);
    }
    
    public boolean isInfo() {
        return "INFO".equalsIgnoreCase(level);
    }
    
    public boolean isResolved() {
        return isResolved != null && isResolved;
    }
    
    @Override
    public String toString() {
        return "SystemEventResponse{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", eventType='" + eventType + '\'' +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", occurredAt=" + occurredAt +
                ", isResolved=" + isResolved +
                '}';
    }
}