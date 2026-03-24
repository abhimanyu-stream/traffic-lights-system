package com.trafficlight.domain;

import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing the history of traffic light state changes.
 * 
 * This entity provides an audit trail of all state transitions for
 * compliance, debugging, and analytics purposes.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(
    name = "state_history",
    indexes = {
        @Index(name = "idx_state_history_light_id", columnList = "traffic_light_id"),
        @Index(name = "idx_state_history_timestamp", columnList = "changed_at"),
        @Index(name = "idx_state_history_from_state", columnList = "from_state"),
        @Index(name = "idx_state_history_to_state", columnList = "to_state")
    }
)
public class StateHistory extends Auditor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;
    
    @NotNull
    @Column(name = "traffic_light_id", length = 36, nullable = false)
    private String trafficLightId;
    
    @NotNull
    @Column(name = "intersection_id", length = 36, nullable = false)
    private String intersectionId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private Direction direction;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_state")
    private LightState fromState;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false)
    private LightState toState;
    
    @NotNull
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "reason", length = 255)
    private String reason;
    
    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;
    
    @Column(name = "correlation_id", length = 36)
    private String correlationId;
    
    @Column(name = "event_id", length = 36)
    private String eventId;
    
    // JPA requires default constructor
    protected StateHistory() {}
    
    /**
     * Private constructor for builder pattern.
     */
    private StateHistory(Builder builder) {
        this.uuid = builder.uuid;
        this.trafficLightId = builder.trafficLightId;
        this.intersectionId = builder.intersectionId;
        this.direction = builder.direction;
        this.fromState = builder.fromState;
        this.toState = builder.toState;
        this.changedAt = builder.changedAt;
        this.durationSeconds = builder.durationSeconds;
        this.reason = builder.reason;
        this.triggeredBy = builder.triggeredBy;
        this.correlationId = builder.correlationId;
        this.eventId = builder.eventId;
    }
    
    /**
     * Creates a new builder for StateHistory.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for StateHistory following the builder pattern.
     */
    public static class Builder {
        private String uuid;
        private String trafficLightId;
        private String intersectionId;
        private Direction direction;
        private LightState fromState;
        private LightState toState;
        private LocalDateTime changedAt = LocalDateTime.now();
        private Integer durationSeconds;
        private String reason;
        private String triggeredBy;
        private String correlationId;
        private String eventId;
        
        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder trafficLightId(String trafficLightId) {
            this.trafficLightId = trafficLightId;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            this.intersectionId = intersectionId;
            return this;
        }
        
        public Builder direction(Direction direction) {
            this.direction = direction;
            return this;
        }
        
        public Builder fromState(LightState fromState) {
            this.fromState = fromState;
            return this;
        }
        
        public Builder toState(LightState toState) {
            this.toState = toState;
            return this;
        }
        
        public Builder changedAt(LocalDateTime changedAt) {
            this.changedAt = changedAt;
            return this;
        }
        
        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder triggeredBy(String triggeredBy) {
            this.triggeredBy = triggeredBy;
            return this;
        }
        
        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public StateHistory build() {
            // Generate UUID if not provided
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            
            return new StateHistory(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getTrafficLightId() { return trafficLightId; }
    public String getIntersectionId() { return intersectionId; }
    public Direction getDirection() { return direction; }
    public LightState getFromState() { return fromState; }
    public LightState getToState() { return toState; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public String getReason() { return reason; }
    public String getTriggeredBy() { return triggeredBy; }
    public String getCorrelationId() { return correlationId; }
    public String getEventId() { return eventId; }
    
    /**
     * Checks if this was a valid state transition.
     * 
     * @return true if transition is valid, false otherwise
     */
    public boolean isValidTransition() {
        if (fromState == null) return true; // Initial state
        return fromState.canTransitionTo(toState);
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate(); // Call parent to set audit fields
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateHistory that = (StateHistory) o;
        return Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "StateHistory{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", trafficLightId='" + trafficLightId + '\'' +
                ", fromState=" + fromState +
                ", toState=" + toState +
                ", changedAt=" + changedAt +
                ", reason='" + reason + '\'' +
                '}';
    }
}