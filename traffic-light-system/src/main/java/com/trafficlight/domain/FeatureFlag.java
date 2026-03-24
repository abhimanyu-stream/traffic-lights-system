package com.trafficlight.domain;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entity representing feature flags for system functionality control.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(name = "feature_flags")
public class FeatureFlag extends Auditor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "feature_name", nullable = false, unique = true)
    private String featureName;
    
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "environment", length = 50)
    private String environment = "all";
    
    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage = 100;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_by", length = 100)
    private String createdBy = "system";
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy = "system";
    
    // Default constructor
    public FeatureFlag() {}
    
    // Constructor for builder pattern
    private FeatureFlag(Builder builder) {
        this.featureName = builder.featureName;
        this.isEnabled = builder.isEnabled;
        this.description = builder.description;
        this.category = builder.category;
        this.environment = builder.environment;
        this.rolloutPercentage = builder.rolloutPercentage;
        this.isActive = builder.isActive;
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String featureName;
        private Boolean isEnabled = false;
        private String description;
        private String category;
        private String environment = "all";
        private Integer rolloutPercentage = 100;
        private Boolean isActive = true;
        private String createdBy = "system";
        private String updatedBy = "system";
        
        public Builder featureName(String featureName) {
            this.featureName = featureName;
            return this;
        }
        
        public Builder isEnabled(Boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }
        
        public Builder rolloutPercentage(Integer rolloutPercentage) {
            this.rolloutPercentage = rolloutPercentage;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }
        
        public FeatureFlag build() {
            return new FeatureFlag(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getFeatureName() { return featureName; }
    public Boolean getIsEnabled() { return isEnabled; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getEnvironment() { return environment; }
    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public Boolean getIsActive() { return isActive; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    /**
     * Check if the feature is enabled for the current environment.
     */
    public boolean isEnabledForEnvironment(String currentEnvironment) {
        if (!isActive || !isEnabled) {
            return false;
        }
        
        return "all".equals(environment) || currentEnvironment.equals(environment);
    }
    
    /**
     * Check if the feature should be enabled based on rollout percentage.
     * This is a simple implementation - in production you might use user ID hashing.
     */
    public boolean isEnabledForRollout() {
        if (!isActive || !isEnabled) {
            return false;
        }
        
        if (rolloutPercentage >= 100) {
            return true;
        }
        
        // Simple random rollout - in production, use deterministic user-based rollout
        return Math.random() * 100 < rolloutPercentage;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureFlag that = (FeatureFlag) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(featureName, that.featureName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, featureName);
    }
    
    @Override
    public String toString() {
        return "FeatureFlag{" +
                "id=" + id +
                ", featureName='" + featureName + '\'' +
                ", isEnabled=" + isEnabled +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}