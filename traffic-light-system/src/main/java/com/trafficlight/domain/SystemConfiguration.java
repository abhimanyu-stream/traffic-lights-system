package com.trafficlight.domain;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entity representing system configuration key-value pairs.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Entity
@Table(name = "system_configurations")
public class SystemConfiguration extends Auditor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "config_key", nullable = false, unique = true)
    private String configKey;
    
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;
    
    @Column(name = "value_type", nullable = false, length = 50)
    private String valueType = "string";
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_by", length = 100)
    private String createdBy = "system";
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy = "system";
    
    // Default constructor
    public SystemConfiguration() {}
    
    // Constructor for builder pattern
    private SystemConfiguration(Builder builder) {
        this.configKey = builder.configKey;
        this.configValue = builder.configValue;
        this.valueType = builder.valueType;
        this.description = builder.description;
        this.category = builder.category;
        this.isActive = builder.isActive;
        this.createdBy = builder.createdBy;
        this.updatedBy = builder.updatedBy;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String configKey;
        private String configValue;
        private String valueType = "string";
        private String description;
        private String category;
        private Boolean isActive = true;
        private String createdBy = "system";
        private String updatedBy = "system";
        
        public Builder configKey(String configKey) {
            this.configKey = configKey;
            return this;
        }
        
        public Builder configValue(String configValue) {
            this.configValue = configValue;
            return this;
        }
        
        public Builder valueType(String valueType) {
            this.valueType = valueType;
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
        
        public SystemConfiguration build() {
            return new SystemConfiguration(this);
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public String getConfigKey() { return configKey; }
    public String getConfigValue() { return configValue; }
    public String getValueType() { return valueType; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Boolean getIsActive() { return isActive; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    /**
     * Get the configuration value as the specified type.
     */
    public Object getTypedValue() {
        if (configValue == null) {
            return null;
        }
        
        switch (valueType.toLowerCase()) {
            case "integer":
                return Integer.valueOf(configValue);
            case "long":
                return Long.valueOf(configValue);
            case "double":
                return Double.valueOf(configValue);
            case "boolean":
                return Boolean.valueOf(configValue);
            case "string":
            default:
                return configValue;
        }
    }
    
    /**
     * Set the configuration value from an object, determining the type automatically.
     */
    public void setTypedValue(Object value) {
        if (value == null) {
            this.configValue = null;
            this.valueType = "string";
            return;
        }
        
        this.configValue = value.toString();
        
        if (value instanceof Integer) {
            this.valueType = "integer";
        } else if (value instanceof Long) {
            this.valueType = "long";
        } else if (value instanceof Double || value instanceof Float) {
            this.valueType = "double";
        } else if (value instanceof Boolean) {
            this.valueType = "boolean";
        } else {
            this.valueType = "string";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemConfiguration that = (SystemConfiguration) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(configKey, that.configKey);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, configKey);
    }
    
    @Override
    public String toString() {
        return "SystemConfiguration{" +
                "id=" + id +
                ", configKey='" + configKey + '\'' +
                ", configValue='" + configValue + '\'' +
                ", valueType='" + valueType + '\'' +
                ", category='" + category + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}