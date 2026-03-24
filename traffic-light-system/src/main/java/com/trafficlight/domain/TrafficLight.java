package com.trafficlight.domain;

import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a traffic light at an intersection.
 * 
 * This entity manages the state, timing, and operational status of individual
 * traffic lights. It includes optimistic locking for concurrent access control
 * and comprehensive validation with automatic UUID generation.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(
    name = "traffic_lights",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_intersection_direction", 
            columnNames = {"intersection_id", "direction"}
        )
    },
    indexes = {
        @Index(name = "idx_intersection_id", columnList = "intersection_id"),
        @Index(name = "idx_current_state", columnList = "current_state"),
        @Index(name = "idx_last_state_change", columnList = "last_state_change"),
        @Index(name = "idx_is_active", columnList = "is_active")
    }
)
public class TrafficLight extends Auditor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;
    
    @NotNull
    @Column(name = "intersection_id", length = 36, nullable = false)
    private String intersectionId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private Direction direction;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private LightState currentState;
    
    @NotNull
    @Column(name = "last_state_change", nullable = false)
    private LocalDateTime lastStateChange;
    
    @Positive
    @Column(name = "state_duration_seconds", nullable = false)
    private Integer stateDurationSeconds;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "redDurationSeconds", column = @Column(name = "timing_red_duration")),
        @AttributeOverride(name = "yellowDurationSeconds", column = @Column(name = "timing_yellow_duration")),
        @AttributeOverride(name = "greenDurationSeconds", column = @Column(name = "timing_green_duration"))
    })
    private TimingConfiguration timingConfiguration;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    // JPA requires default constructor
    protected TrafficLight() {}
    
    /**
     * Private constructor for builder pattern.
     */
    private TrafficLight(Builder builder) {
        this.uuid = builder.uuid;
        this.intersectionId = builder.intersectionId;
        this.direction = builder.direction;
        this.currentState = builder.currentState;
        this.lastStateChange = builder.lastStateChange;
        this.stateDurationSeconds = builder.stateDurationSeconds;
        this.timingConfiguration = builder.timingConfiguration;
        this.isActive = builder.isActive;
    }
    
    /**
     * Creates a new builder for TrafficLight.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for TrafficLight following the builder pattern.
     */
    public static class Builder {
        private String uuid;
        private String intersectionId;
        private Direction direction;
        private LightState currentState = LightState.RED; // Safe default
        private LocalDateTime lastStateChange = LocalDateTime.now();
        private Integer stateDurationSeconds;
        private TimingConfiguration timingConfiguration;
        private Boolean isActive = true;
        
        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            this.intersectionId = intersectionId;
            return this;
        }
        
        public Builder direction(Direction direction) {
            this.direction = direction;
            this.stateDurationSeconds = direction != null ? 
                LightState.RED.getDefaultDurationSeconds() : null;
            return this;
        }
        
        public Builder currentState(LightState currentState) {
            this.currentState = currentState;
            if (currentState != null && this.stateDurationSeconds == null) {
                this.stateDurationSeconds = currentState.getDefaultDurationSeconds();
            }
            return this;
        }
        
        public Builder lastStateChange(LocalDateTime lastStateChange) {
            this.lastStateChange = lastStateChange;
            return this;
        }
        
        public Builder stateDurationSeconds(Integer stateDurationSeconds) {
            this.stateDurationSeconds = stateDurationSeconds;
            return this;
        }
        
        public Builder timingConfiguration(TimingConfiguration timingConfiguration) {
            this.timingConfiguration = timingConfiguration;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public TrafficLight build() {
            // Generate UUID if not provided
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            
            // Set default duration if not specified
            if (stateDurationSeconds == null && currentState != null) {
                stateDurationSeconds = currentState.getDefaultDurationSeconds();
            }
            
            return new TrafficLight(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getIntersectionId() { return intersectionId; }
    public Direction getDirection() { return direction; }
    public LightState getCurrentState() { return currentState; }
    public LocalDateTime getLastStateChange() { return lastStateChange; }
    public Integer getStateDurationSeconds() { return stateDurationSeconds; }
    public TimingConfiguration getTimingConfiguration() { return timingConfiguration; }
    public Boolean getIsActive() { return isActive; }
    
    public boolean isActive() { return isActive != null && isActive; }
    public Long getVersion() { return version; }
    
    // Setters for mutable fields
    public void setIntersectionId(String intersectionId) {
        this.intersectionId = intersectionId;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public void setCurrentState(LightState currentState) {
        this.currentState = currentState;
        this.lastStateChange = LocalDateTime.now();
    }
    
    public void setActive(Boolean active) {
        this.isActive = active;
    }
    
    public void setLastStateChange(LocalDateTime lastStateChange) {
        this.lastStateChange = lastStateChange;
    }
    
    public void setStateDurationSeconds(Integer stateDurationSeconds) {
        this.stateDurationSeconds = stateDurationSeconds;
    }
    
    public void setTimingConfiguration(TimingConfiguration timingConfiguration) {
        this.timingConfiguration = timingConfiguration;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Gets the state duration as a Duration object.
     * 
     * @return the state duration
     */
    public Duration getStateDuration() {
        return Duration.ofSeconds(stateDurationSeconds != null ? stateDurationSeconds : 0);
    }
    
    /**
     * Checks if this traffic light can transition to the specified state.
     * 
     * @param targetState the target state
     * @return true if transition is valid, false otherwise
     */
    public boolean canTransitionTo(LightState targetState) {
        return currentState.canTransitionTo(targetState);
    }
    
    /**
     * Checks if this traffic light is currently allowing movement.
     * 
     * @return true if allowing movement (green), false otherwise
     */
    public boolean isAllowingMovement() {
        return isActive && currentState.allowsMovement();
    }
    
    /**
     * Checks if this traffic light conflicts with another traffic light.
     * 
     * @param other the other traffic light
     * @return true if they conflict (perpendicular directions), false otherwise
     */
    public boolean conflictsWith(TrafficLight other) {
        return !this.intersectionId.equals(other.intersectionId) || 
               this.direction.conflictsWith(other.direction);
    }
    
    /**
     * Gets the time elapsed since the last state change.
     * 
     * @return duration since last state change
     */
    public Duration getTimeSinceLastStateChange() {
        return Duration.between(lastStateChange, LocalDateTime.now());
    }
    
    /**
     * Checks if the current state has exceeded its duration.
     * 
     * @return true if state duration has been exceeded, false otherwise
     */
    public boolean hasExceededStateDuration() {
        return getTimeSinceLastStateChange().getSeconds() > stateDurationSeconds;
    }
    
    /**
     * Generates a new UUID for this traffic light.
     * This method can be called before persisting if manual ID generation is needed.
     */
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate(); // Call parent to set audit fields
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        if (lastStateChange == null) {
            lastStateChange = LocalDateTime.now();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrafficLight that = (TrafficLight) o;
        return Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "TrafficLight{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", intersectionId='" + intersectionId + '\'' +
                ", direction=" + direction +
                ", currentState=" + currentState +
                ", lastStateChange=" + lastStateChange +
                ", stateDurationSeconds=" + stateDurationSeconds +
                ", isActive=" + isActive +
                ", version=" + version +
                '}';
    }
}