package com.trafficlight.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing system events for monitoring and auditing.
 * 
 * This entity tracks all significant system events including errors,
 * state changes, configuration updates, and operational events.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(
    name = "system_events",
    indexes = {
        @Index(name = "idx_system_event_type", columnList = "event_type"),
        @Index(name = "idx_system_event_level", columnList = "level"),
        @Index(name = "idx_system_event_timestamp", columnList = "occurred_at"),
        @Index(name = "idx_system_event_source", columnList = "source"),
        @Index(name = "idx_system_event_correlation", columnList = "correlation_id")
    }
)
public class SystemEvent extends Auditor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "level", nullable = false)
    private String level; // INFO, WARN, ERROR, DEBUG
    
    @NotBlank
    @Size(max = 500)
    @Column(name = "message", nullable = false)
    private String message;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Size(max = 100)
    @Column(name = "source")
    private String source;
    
    @Column(name = "intersection_id", length = 36)
    private String intersectionId;
    
    @Column(name = "traffic_light_id", length = 36)
    private String trafficLightId;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;
    
    @Column(name = "user_id", length = 36)
    private String userId;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @NotNull
    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;
    
    // JPA requires default constructor
    protected SystemEvent() {}
    
    /**
     * Private constructor for builder pattern.
     */
    private SystemEvent(Builder builder) {
        this.uuid = builder.uuid;
        this.eventType = builder.eventType;
        this.level = builder.level;
        this.message = builder.message;
        this.details = builder.details;
        this.source = builder.source;
        this.intersectionId = builder.intersectionId;
        this.trafficLightId = builder.trafficLightId;
        this.correlationId = builder.correlationId;
        this.userId = builder.userId;
        this.sessionId = builder.sessionId;
        this.occurredAt = builder.occurredAt;
        this.resolvedAt = builder.resolvedAt;
        this.isResolved = builder.isResolved;
    }
    
    /**
     * Creates a new builder for SystemEvent.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for SystemEvent following the builder pattern.
     */
    public static class Builder {
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
        private LocalDateTime occurredAt = LocalDateTime.now();
        private LocalDateTime resolvedAt;
        private Boolean isResolved = false;
        
        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder level(String level) {
            this.level = level;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder details(String details) {
            this.details = details;
            return this;
        }
        
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            this.intersectionId = intersectionId;
            return this;
        }
        
        public Builder trafficLightId(String trafficLightId) {
            this.trafficLightId = trafficLightId;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder occurredAt(LocalDateTime occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }
        
        public Builder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }
        
        public Builder isResolved(Boolean isResolved) {
            this.isResolved = isResolved;
            return this;
        }
        
        public SystemEvent build() {
            // Generate UUID if not provided
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            
            return new SystemEvent(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getEventType() { return eventType; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
    public String getSource() { return source; }
    public String getIntersectionId() { return intersectionId; }
    public String getTrafficLightId() { return trafficLightId; }
    public String getCorrelationId() { return correlationId; }
    public String getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public Boolean getIsResolved() { return isResolved; }
    
    // Setters for mutable fields
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
    
    /**
     * Marks this event as resolved.
     */
    public void markAsResolved() {
        this.isResolved = true;
        this.resolvedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if this is an error level event.
     * 
     * @return true if error level, false otherwise
     */
    public boolean isError() {
        return "ERROR".equalsIgnoreCase(level);
    }
    
    /**
     * Checks if this is a warning level event.
     * 
     * @return true if warning level, false otherwise
     */
    public boolean isWarning() {
        return "WARN".equalsIgnoreCase(level);
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate(); // Call parent to set audit fields
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemEvent that = (SystemEvent) o;
        return Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "SystemEvent{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", eventType='" + eventType + '\'' +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", source='" + source + '\'' +
                ", occurredAt=" + occurredAt +
                ", isResolved=" + isResolved +
                '}';
    }
}