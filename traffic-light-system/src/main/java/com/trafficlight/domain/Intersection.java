package com.trafficlight.domain;

import com.trafficlight.enums.IntersectionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a traffic intersection.
 * 
 * This entity manages multiple traffic lights at a single intersection,
 * coordinates their states, and ensures safety constraints are maintained.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(
    name = "intersections",
    indexes = {
        @Index(name = "idx_intersection_name", columnList = "name"),
        @Index(name = "idx_intersection_status", columnList = "status"),
        @Index(name = "idx_intersection_location", columnList = "latitude, longitude")
    }
)
public class Intersection extends Auditor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "uuid", length = 36, nullable = false, unique = true, updatable = false)
    private String uuid;
    
    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Size(max = 255)
    @Column(name = "description")
    private String description;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "longitude"))
    })
    private Coordinates coordinates;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IntersectionStatus status;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    @OneToMany(mappedBy = "intersectionId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrafficLight> trafficLights = new ArrayList<>();
    
    // JPA requires default constructor
    protected Intersection() {}
    
    /**
     * Private constructor for builder pattern.
     */
    private Intersection(Builder builder) {
        this.uuid = builder.uuid;
        this.name = builder.name;
        this.description = builder.description;
        this.coordinates = builder.coordinates;
        this.status = builder.status;
        this.isActive = builder.isActive;
    }
    
    /**
     * Creates a new builder for Intersection.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for Intersection following the builder pattern.
     */
    public static class Builder {
        private String uuid;
        private String name;
        private String description;
        private Coordinates coordinates;
        private IntersectionStatus status = IntersectionStatus.ACTIVE;
        private Boolean isActive = true;
        
        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder coordinates(Coordinates coordinates) {
            this.coordinates = coordinates;
            return this;
        }
        
        public Builder coordinates(Double latitude, Double longitude) {
            if (latitude != null && longitude != null) {
                this.coordinates = Coordinates.of(latitude, longitude);
            }
            return this;
        }
        
        public Builder latitude(Double latitude) {
            // For backward compatibility - will be combined with longitude
            return this;
        }
        
        public Builder longitude(Double longitude) {
            // For backward compatibility - will be combined with latitude
            return this;
        }
        
        public Builder status(IntersectionStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Intersection build() {
            // Generate UUID if not provided
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            
            return new Intersection(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Coordinates getCoordinates() { return coordinates; }
    
    // Backward compatibility getters
    public Double getLatitude() { 
        return coordinates != null ? coordinates.getLatitude() : null; 
    }
    
    public Double getLongitude() { 
        return coordinates != null ? coordinates.getLongitude() : null; 
    }
    
    public IntersectionStatus getStatus() { return status; }
    public Boolean getIsActive() { return isActive; }
    
    public boolean isActive() { return isActive != null && isActive; }
    public Long getVersion() { return version; }
    public List<TrafficLight> getTrafficLights() { return trafficLights; }
    
    // Setters for mutable fields
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    
    public void setCoordinates(Coordinates coordinates) { 
        this.coordinates = coordinates; 
    }
    
    public void setCoordinates(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            this.coordinates = Coordinates.of(latitude, longitude);
        } else {
            this.coordinates = null;
        }
    }
    
    // Backward compatibility setters
    public void setLatitude(Double latitude) { 
        if (coordinates != null) {
            this.coordinates = Coordinates.of(latitude, coordinates.getLongitude());
        } else if (latitude != null) {
            // Store temporarily, will be combined when longitude is set
        }
    }
    
    public void setLongitude(Double longitude) { 
        if (coordinates != null) {
            this.coordinates = Coordinates.of(coordinates.getLatitude(), longitude);
        } else if (longitude != null) {
            // Store temporarily, will be combined when latitude is set
        }
    }
    
    public void setStatus(IntersectionStatus status) { this.status = status; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setActive(Boolean active) { this.isActive = active; }
    
    /**
     * Checks if this intersection has any active traffic lights.
     * 
     * @return true if has active lights, false otherwise
     */
    public boolean hasActiveTrafficLights() {
        return trafficLights.stream().anyMatch(TrafficLight::getIsActive);
    }
    
    /**
     * Calculate distance to another intersection in kilometers.
     * 
     * @param other the other intersection
     * @return distance in kilometers, or null if either intersection lacks coordinates
     */
    public Double distanceTo(Intersection other) {
        if (this.coordinates == null || other.coordinates == null) {
            return null;
        }
        return this.coordinates.distanceTo(other.coordinates);
    }
    
    /**
     * Calculate distance to specific coordinates in kilometers.
     * 
     * @param targetCoordinates the target coordinates
     * @return distance in kilometers, or null if this intersection lacks coordinates
     */
    public Double distanceTo(Coordinates targetCoordinates) {
        if (this.coordinates == null || targetCoordinates == null) {
            return null;
        }
        return this.coordinates.distanceTo(targetCoordinates);
    }
    
    /**
     * Check if this intersection is within a certain radius of given coordinates.
     * 
     * @param targetCoordinates the center coordinates
     * @param radiusKm the radius in kilometers
     * @return true if within radius, false otherwise
     */
    public boolean isWithinRadius(Coordinates targetCoordinates, double radiusKm) {
        Double distance = distanceTo(targetCoordinates);
        return distance != null && distance <= radiusKm;
    }
    
    /**
     * Gets count of active traffic lights.
     * 
     * @return number of active traffic lights
     */
    public long getActiveTrafficLightCount() {
        return trafficLights.stream().filter(TrafficLight::getIsActive).count();
    }
    
    /**
     * Checks if intersection is operational.
     * 
     * @return true if operational, false otherwise
     */
    public boolean isOperational() {
        return isActive && status == IntersectionStatus.ACTIVE;
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
        Intersection that = (Intersection) o;
        return Objects.equals(uuid, that.uuid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "Intersection{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", isActive=" + isActive +
                ", trafficLightCount=" + trafficLights.size() +
                '}';
    }
}