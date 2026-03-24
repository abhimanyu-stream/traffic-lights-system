package com.trafficlight.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Event published for system health monitoring.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class SystemHealthEvent {
    
    private String eventId;
    private String healthStatus; // HEALTHY, DEGRADED, CRITICAL, DOWN
    private String component; // DATABASE, KAFKA, APPLICATION, EXTERNAL_SERVICE
    private String previousStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String message;
    private String details;
    private Map<String, Object> metrics;
    private String correlationId;
    private String source;
    private String version;
    
    // Health check specific fields
    private Long responseTimeMs;
    private Double cpuUsage;
    private Double memoryUsage;
    private Long activeConnections;
    private Long errorCount;
    private String errorRate;
    
    // Default constructor for JSON deserialization
    public SystemHealthEvent() {}
    
    // Constructor with required fields
    public SystemHealthEvent(String healthStatus, String component, LocalDateTime timestamp) {
        this.healthStatus = healthStatus;
        this.component = component;
        this.timestamp = timestamp;
        this.source = "traffic-light-controller";
        this.version = "1.0";
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private SystemHealthEvent event = new SystemHealthEvent();
        
        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }
        
        public Builder healthStatus(String healthStatus) {
            event.healthStatus = healthStatus;
            return this;
        }
        
        public Builder component(String component) {
            event.component = component;
            return this;
        }
        
        public Builder previousStatus(String previousStatus) {
            event.previousStatus = previousStatus;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            event.timestamp = timestamp;
            return this;
        }
        
        public Builder message(String message) {
            event.message = message;
            return this;
        }
        
        public Builder details(String details) {
            event.details = details;
            return this;
        }
        
        public Builder metrics(Map<String, Object> metrics) {
            event.metrics = metrics;
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
        
        public Builder responseTimeMs(Long responseTimeMs) {
            event.responseTimeMs = responseTimeMs;
            return this;
        }
        
        public Builder cpuUsage(Double cpuUsage) {
            event.cpuUsage = cpuUsage;
            return this;
        }
        
        public Builder memoryUsage(Double memoryUsage) {
            event.memoryUsage = memoryUsage;
            return this;
        }
        
        public Builder activeConnections(Long activeConnections) {
            event.activeConnections = activeConnections;
            return this;
        }
        
        public Builder errorCount(Long errorCount) {
            event.errorCount = errorCount;
            return this;
        }
        
        public Builder errorRate(String errorRate) {
            event.errorRate = errorRate;
            return this;
        }
        
        public SystemHealthEvent build() {
            // Set defaults if not provided
            if (event.timestamp == null) {
                event.timestamp = LocalDateTime.now();
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
    
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public Double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
    
    public Double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }
    
    public Long getActiveConnections() { return activeConnections; }
    public void setActiveConnections(Long activeConnections) { this.activeConnections = activeConnections; }
    
    public Long getErrorCount() { return errorCount; }
    public void setErrorCount(Long errorCount) { this.errorCount = errorCount; }
    
    public String getErrorRate() { return errorRate; }
    public void setErrorRate(String errorRate) { this.errorRate = errorRate; }
    
    // Convenience methods
    public boolean isHealthy() {
        return "HEALTHY".equals(healthStatus);
    }
    
    public boolean isDegraded() {
        return "DEGRADED".equals(healthStatus);
    }
    
    public boolean isCritical() {
        return "CRITICAL".equals(healthStatus);
    }
    
    public boolean isDown() {
        return "DOWN".equals(healthStatus);
    }
    
    public boolean isStatusChanged() {
        return previousStatus != null && !previousStatus.equals(healthStatus);
    }
    
    public boolean isImprovement() {
        if (previousStatus == null) return false;
        
        int currentLevel = getStatusLevel(healthStatus);
        int previousLevel = getStatusLevel(previousStatus);
        
        return currentLevel > previousLevel;
    }
    
    public boolean isDegradation() {
        if (previousStatus == null) return false;
        
        int currentLevel = getStatusLevel(healthStatus);
        int previousLevel = getStatusLevel(previousStatus);
        
        return currentLevel < previousLevel;
    }
    
    private int getStatusLevel(String status) {
        switch (status) {
            case "DOWN": return 0;
            case "CRITICAL": return 1;
            case "DEGRADED": return 2;
            case "HEALTHY": return 3;
            default: return 0;
        }
    }
    
    public boolean isDatabaseComponent() {
        return "DATABASE".equals(component);
    }
    
    public boolean isKafkaComponent() {
        return "KAFKA".equals(component);
    }
    
    public boolean isApplicationComponent() {
        return "APPLICATION".equals(component);
    }
    
    public boolean isExternalServiceComponent() {
        return "EXTERNAL_SERVICE".equals(component);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemHealthEvent that = (SystemHealthEvent) o;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(component, that.component) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventId, component, timestamp);
    }
    
    @Override
    public String toString() {
        return "SystemHealthEvent{" +
                "eventId='" + eventId + '\'' +
                ", healthStatus='" + healthStatus + '\'' +
                ", component='" + component + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", responseTimeMs=" + responseTimeMs +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }
}