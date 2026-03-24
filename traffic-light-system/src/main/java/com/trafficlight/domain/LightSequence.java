package com.trafficlight.domain;

import com.trafficlight.enums.LightState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a light sequence configuration.
 * 
 * This entity defines the timing and order of state transitions
 * for traffic lights at an intersection.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(
    name = "light_sequences",
    indexes = {
        @Index(name = "idx_sequence_intersection", columnList = "intersection_id"),
        @Index(name = "idx_sequence_state", columnList = "state"),
        @Index(name = "idx_sequence_order", columnList = "sequence_order"),
        @Index(name = "idx_sequence_active", columnList = "is_active")
    }
)
public class LightSequence extends Auditor {
    
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
    @Column(name = "state", nullable = false)
    private LightState state;
    
    @NotNull
    @Positive
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "redDurationSeconds", column = @Column(name = "timing_red_duration")),
        @AttributeOverride(name = "yellowDurationSeconds", column = @Column(name = "timing_yellow_duration")),
        @AttributeOverride(name = "greenDurationSeconds", column = @Column(name = "timing_green_duration"))
    })
    private TimingConfiguration timingConfiguration;
    
    @NotNull
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;
    
    @Column(name = "next_sequence_id", length = 36)
    private String nextSequenceId;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    // JPA requires default constructor
    protected LightSequence() {}
    
    /**
     * Private constructor for builder pattern.
     */
    private LightSequence(Builder builder) {
        this.uuid = builder.uuid;
        this.intersectionId = builder.intersectionId;
        this.state = builder.state;
        this.durationSeconds = builder.durationSeconds;
        this.timingConfiguration = builder.timingConfiguration;
        this.sequenceOrder = builder.sequenceOrder;
        this.nextSequenceId = builder.nextSequenceId;
        this.isActive = builder.isActive;
        this.description = builder.description;
    }
    
    /**
     * Creates a new builder for LightSequence.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for LightSequence following the builder pattern.
     */
    public static class Builder {
        private String uuid;
        private String intersectionId;
        private LightState state;
        private Integer durationSeconds;
        private TimingConfiguration timingConfiguration;
        private Integer sequenceOrder;
        private String nextSequenceId;
        private Boolean isActive = true;
        private String description;
        
        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder intersectionId(String intersectionId) {
            this.intersectionId = intersectionId;
            return this;
        }
        
        public Builder state(LightState state) {
            this.state = state;
            if (this.durationSeconds == null && state != null) {
                this.durationSeconds = state.getDefaultDurationSeconds();
            }
            return this;
        }
        
        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }
        
        public Builder timingConfiguration(TimingConfiguration timingConfiguration) {
            this.timingConfiguration = timingConfiguration;
            return this;
        }
        
        public Builder sequenceOrder(Integer sequenceOrder) {
            this.sequenceOrder = sequenceOrder;
            return this;
        }
        
        public Builder nextSequenceId(String nextSequenceId) {
            this.nextSequenceId = nextSequenceId;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public LightSequence build() {
            // Generate UUID if not provided
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            
            // Set default duration if not specified
            if (durationSeconds == null && state != null) {
                durationSeconds = state.getDefaultDurationSeconds();
            }
            
            return new LightSequence(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getIntersectionId() { return intersectionId; }
    public LightState getState() { return state; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public TimingConfiguration getTimingConfiguration() { return timingConfiguration; }
    public Integer getSequenceOrder() { return sequenceOrder; }
    public String getNextSequenceId() { return nextSequenceId; }
    public Boolean getIsActive() { return isActive; }
    public String getDescription() { return description; }
    public Long getVersion() { return version; }
    
    // Setters for mutable fields
    public void setState(LightState state) { this.state = state; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public void setTimingConfiguration(TimingConfiguration timingConfiguration) { this.timingConfiguration = timingConfiguration; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
    public void setNextSequenceId(String nextSequenceId) { this.nextSequenceId = nextSequenceId; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setDescription(String description) { this.description = description; }
    
    /**
     * Checks if this sequence has a next sequence defined.
     * 
     * @return true if has next sequence, false otherwise
     */
    public boolean hasNextSequence() {
        return nextSequenceId != null && !nextSequenceId.trim().isEmpty();
    }
    
    /**
     * Checks if this sequence is the last in the cycle.
     * 
     * @return true if last sequence, false otherwise
     */
    public boolean isLastInCycle() {
        return !hasNextSequence();
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate(); // Call parent to set audit fields
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightSequence that = (LightSequence) o;
        return Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "LightSequence{" +
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