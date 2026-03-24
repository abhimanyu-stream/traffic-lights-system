package com.trafficlight.service;

import com.trafficlight.events.SystemHealthEvent;
import com.trafficlight.events.publisher.TrafficLightEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for monitoring system health and publishing health events.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
public class SystemHealthService {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemHealthService.class);
    
    private final DataSource dataSource;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TrafficLightEventPublisher eventPublisher;
    
    @Autowired
    public SystemHealthService(DataSource dataSource,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              TrafficLightEventPublisher eventPublisher) {
        this.dataSource = dataSource;
        this.kafkaTemplate = kafkaTemplate;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Get comprehensive system health information.
     */
    public Map<String, Object> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            // Check database health
            boolean dbHealthy = checkDatabaseHealth();
            health.put("database", dbHealthy ? "UP" : "DOWN");
            
            // Check Kafka health
            boolean kafkaHealthy = checkKafkaHealth();
            health.put("kafka", kafkaHealthy ? "UP" : "DOWN");
            
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            health.put("memory", Map.of(
                "used", usedMemory,
                "free", freeMemory,
                "total", totalMemory,
                "max", maxMemory,
                "usagePercent", String.format("%.2f%%", memoryUsagePercent)
            ));
            
            // Check CPU usage (simplified)
            health.put("processors", runtime.availableProcessors());
            
            // Determine overall health
            boolean overallHealthy = dbHealthy && kafkaHealthy && memoryUsagePercent < 90;
            health.put("status", overallHealthy ? "UP" : "DOWN");
            
            return health;
            
        } catch (Exception e) {
            logger.error("Error checking system health", e);
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("error", e.getMessage());
            return errorHealth;
        }
    }
    
    /**
     * Check database connectivity and health.
     */
    public boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            logger.warn("Database health check failed", e);
            publishHealthEvent("DATABASE", "DOWN", "Database connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check Kafka connectivity and health.
     */
    public boolean checkKafkaHealth() {
        try {
            return eventPublisher.isKafkaAvailable();
        } catch (Exception e) {
            logger.warn("Kafka health check failed", e);
            publishHealthEvent("KAFKA", "DOWN", "Kafka connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get detailed system metrics.
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Runtime metrics
        Runtime runtime = Runtime.getRuntime();
        metrics.put("memory", Map.of(
            "used", runtime.totalMemory() - runtime.freeMemory(),
            "free", runtime.freeMemory(),
            "total", runtime.totalMemory(),
            "max", runtime.maxMemory()
        ));
        
        metrics.put("processors", runtime.availableProcessors());
        
        // System properties
        metrics.put("javaVersion", System.getProperty("java.version"));
        metrics.put("osName", System.getProperty("os.name"));
        metrics.put("osVersion", System.getProperty("os.version"));
        
        // Application metrics
        metrics.put("uptime", getApplicationUptime());
        metrics.put("timestamp", LocalDateTime.now());
        
        return metrics;
    }
    
    /**
     * Get application uptime in milliseconds.
     */
    public long getApplicationUptime() {
        return System.currentTimeMillis() - getStartTime();
    }
    
    /**
     * Get application start time (simplified - would be better to track this properly).
     */
    private long getStartTime() {
        // This is a simplified implementation
        // In a real application, you'd track the actual start time
        return System.currentTimeMillis() - 60000; // Assume started 1 minute ago
    }
    
    /**
     * Publish system health event.
     */
    public void publishHealthEvent(String component, String status, String message) {
        try {
            SystemHealthEvent event = SystemHealthEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .healthStatus(status)
                .component(component)
                .message(message)
                .timestamp(LocalDateTime.now())
                .source("system-health-service")
                .build();
            
            eventPublisher.publishSystemHealthEvent(event);
            
        } catch (Exception e) {
            logger.error("Failed to publish health event", e);
        }
    }
    
    /**
     * Perform comprehensive health check and publish results.
     */
    public void performHealthCheck() {
        logger.info("Performing comprehensive health check");
        
        try {
            // Check database
            boolean dbHealthy = checkDatabaseHealth();
            publishHealthEvent("DATABASE", dbHealthy ? "HEALTHY" : "DOWN", 
                             dbHealthy ? "Database is responsive" : "Database connection failed");
            
            // Check Kafka
            boolean kafkaHealthy = checkKafkaHealth();
            publishHealthEvent("KAFKA", kafkaHealthy ? "HEALTHY" : "DOWN",
                             kafkaHealthy ? "Kafka is responsive" : "Kafka connection failed");
            
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
            
            String memoryStatus = memoryUsagePercent > 90 ? "CRITICAL" : 
                                 memoryUsagePercent > 75 ? "DEGRADED" : "HEALTHY";
            
            publishHealthEvent("APPLICATION", memoryStatus, 
                             String.format("Memory usage: %.2f%%", memoryUsagePercent));
            
            logger.info("Health check completed - DB: {}, Kafka: {}, Memory: {}%", 
                       dbHealthy ? "UP" : "DOWN", 
                       kafkaHealthy ? "UP" : "DOWN", 
                       String.format("%.2f", memoryUsagePercent));
            
        } catch (Exception e) {
            logger.error("Error during health check", e);
            publishHealthEvent("APPLICATION", "CRITICAL", "Health check failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if system is ready to serve traffic.
     */
    public boolean isSystemReady() {
        return checkDatabaseHealth() && checkKafkaHealth();
    }
    
    /**
     * Check if system is alive (basic liveness check).
     */
    public boolean isSystemAlive() {
        try {
            // Basic check - if we can execute this method, the application is alive
            return true;
        } catch (Exception e) {
            logger.error("Liveness check failed", e);
            return false;
        }
    }
}