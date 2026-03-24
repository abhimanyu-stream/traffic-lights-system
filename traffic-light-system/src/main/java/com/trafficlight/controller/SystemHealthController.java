package com.trafficlight.controller;

import com.trafficlight.constants.ApiConstants;
import com.trafficlight.dto.response.ApiResponse;
import com.trafficlight.utils.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for System Health operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@RestController
@RequestMapping(ApiConstants.TRAFFIC_SERVICE_BASE)
public class SystemHealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemHealthController.class);
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    /**
     * Simple health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        logger.info("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseBuilder.success(health);
    }
    
    /**
     * Get system health status.
     */
    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        logger.info("Getting system health status");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("status", "UP");
        healthStatus.put("application", "Traffic Light Controller");
        healthStatus.put("version", "1.0.0");
        
        // Check database connectivity
        Map<String, Object> databaseHealth = checkDatabaseHealth();
        healthStatus.put("database", databaseHealth);
        
        // Check Kafka connectivity (simplified)
        Map<String, Object> kafkaHealth = checkKafkaHealth();
        healthStatus.put("kafka", kafkaHealth);
        
        // Overall status
        boolean isHealthy = (Boolean) databaseHealth.get("healthy") && (Boolean) kafkaHealth.get("healthy");
        healthStatus.put("overall", isHealthy ? "HEALTHY" : "DEGRADED");
        
        return ResponseBuilder.success(healthStatus, ApiConstants.SYSTEM_HEALTHY);
    }
    
    /**
     * Get detailed system metrics.
     */
    @GetMapping("/system/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemMetrics() {
        logger.info("Getting system metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now());
        
        // JVM metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmMetrics = new HashMap<>();
        jvmMetrics.put("totalMemory", runtime.totalMemory());
        jvmMetrics.put("freeMemory", runtime.freeMemory());
        jvmMetrics.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvmMetrics.put("maxMemory", runtime.maxMemory());
        jvmMetrics.put("availableProcessors", runtime.availableProcessors());
        metrics.put("jvm", jvmMetrics);
        
        // System properties
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("javaVersion", System.getProperty("java.version"));
        systemMetrics.put("osName", System.getProperty("os.name"));
        systemMetrics.put("osVersion", System.getProperty("os.version"));
        systemMetrics.put("osArch", System.getProperty("os.arch"));
        metrics.put("system", systemMetrics);
        
        // Application metrics (placeholder)
        Map<String, Object> appMetrics = new HashMap<>();
        appMetrics.put("uptime", "N/A"); // Would calculate actual uptime
        appMetrics.put("activeConnections", "N/A"); // Would get from connection pool
        appMetrics.put("requestCount", "N/A"); // Would get from metrics registry
        metrics.put("application", appMetrics);
        
        return ResponseBuilder.success(metrics);
    }
    
    /**
     * Perform system health check.
     */
    @PostMapping("/system/health/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> performHealthCheck() {
        logger.info("Performing comprehensive health check");
        
        Map<String, Object> healthCheck = new HashMap<>();
        healthCheck.put("timestamp", LocalDateTime.now());
        healthCheck.put("checkType", "COMPREHENSIVE");
        
        // Database health check
        Map<String, Object> dbCheck = performDatabaseHealthCheck();
        healthCheck.put("database", dbCheck);
        
        // Kafka health check
        Map<String, Object> kafkaCheck = performKafkaHealthCheck();
        healthCheck.put("kafka", kafkaCheck);
        
        // Memory health check
        Map<String, Object> memoryCheck = performMemoryHealthCheck();
        healthCheck.put("memory", memoryCheck);
        
        // Overall health assessment
        boolean allHealthy = (Boolean) dbCheck.get("healthy") && 
                           (Boolean) kafkaCheck.get("healthy") && 
                           (Boolean) memoryCheck.get("healthy");
        
        healthCheck.put("overall", allHealthy ? "HEALTHY" : "DEGRADED");
        healthCheck.put("recommendations", generateHealthRecommendations(dbCheck, kafkaCheck, memoryCheck));
        
        return ResponseBuilder.success(healthCheck);
    }
    
    /**
     * Get system configuration.
     */
    @GetMapping("/system/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfiguration() {
        logger.info("Getting system configuration");
        
        Map<String, Object> config = new HashMap<>();
        config.put("timestamp", LocalDateTime.now());
        
        // Application configuration
        Map<String, Object> appConfig = new HashMap<>();
        appConfig.put("name", "Traffic Light Controller");
        appConfig.put("version", "1.0.0");
        appConfig.put("profile", System.getProperty("spring.profiles.active", "default"));
        config.put("application", appConfig);
        
        // Database configuration (non-sensitive)
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("driver", "MySQL");
        dbConfig.put("poolSize", "10"); // Would get from actual config
        dbConfig.put("connectionTimeout", "30000");
        config.put("database", dbConfig);
        
        // Kafka configuration (non-sensitive)
        Map<String, Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrapServers", "localhost:9092"); // Would get from actual config
        kafkaConfig.put("groupId", "traffic-light-controller");
        kafkaConfig.put("autoOffsetReset", "earliest");
        config.put("kafka", kafkaConfig);
        
        return ResponseBuilder.success(config);
    }
    
    /**
     * Check database health.
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(5); // 5 second timeout
                    dbHealth.put("healthy", isValid);
                    dbHealth.put("status", isValid ? "UP" : "DOWN");
                    dbHealth.put("responseTime", "< 5s");
                }
            } else {
                dbHealth.put("healthy", false);
                dbHealth.put("status", "DOWN");
                dbHealth.put("error", "DataSource not available");
            }
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            dbHealth.put("healthy", false);
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        
        return dbHealth;
    }
    
    /**
     * Check Kafka health (simplified).
     */
    private Map<String, Object> checkKafkaHealth() {
        Map<String, Object> kafkaHealth = new HashMap<>();
        
        // Simplified Kafka health check
        // In a real implementation, you would check Kafka connectivity
        kafkaHealth.put("healthy", true);
        kafkaHealth.put("status", "UP");
        kafkaHealth.put("note", "Simplified check - implement actual Kafka connectivity test");
        
        return kafkaHealth;
    }
    
    /**
     * Perform detailed database health check.
     */
    private Map<String, Object> performDatabaseHealthCheck() {
        Map<String, Object> dbCheck = new HashMap<>();
        
        try {
            if (dataSource != null) {
                long startTime = System.currentTimeMillis();
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(10);
                    long responseTime = System.currentTimeMillis() - startTime;
                    
                    dbCheck.put("healthy", isValid);
                    dbCheck.put("status", isValid ? "UP" : "DOWN");
                    dbCheck.put("responseTimeMs", responseTime);
                    dbCheck.put("connectionValid", isValid);
                    dbCheck.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                    dbCheck.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
                }
            } else {
                dbCheck.put("healthy", false);
                dbCheck.put("status", "DOWN");
                dbCheck.put("error", "DataSource not configured");
            }
        } catch (Exception e) {
            logger.error("Detailed database health check failed", e);
            dbCheck.put("healthy", false);
            dbCheck.put("status", "DOWN");
            dbCheck.put("error", e.getMessage());
        }
        
        return dbCheck;
    }
    
    /**
     * Perform Kafka health check.
     */
    private Map<String, Object> performKafkaHealthCheck() {
        Map<String, Object> kafkaCheck = new HashMap<>();
        
        // Placeholder for actual Kafka health check
        kafkaCheck.put("healthy", true);
        kafkaCheck.put("status", "UP");
        kafkaCheck.put("note", "Implement actual Kafka admin client connectivity test");
        
        return kafkaCheck;
    }
    
    /**
     * Perform memory health check.
     */
    private Map<String, Object> performMemoryHealthCheck() {
        Map<String, Object> memoryCheck = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        boolean healthy = memoryUsagePercent < 85.0; // Consider unhealthy if > 85% memory usage
        
        memoryCheck.put("healthy", healthy);
        memoryCheck.put("status", healthy ? "UP" : "WARNING");
        memoryCheck.put("usedMemoryMB", usedMemory / (1024 * 1024));
        memoryCheck.put("totalMemoryMB", totalMemory / (1024 * 1024));
        memoryCheck.put("maxMemoryMB", maxMemory / (1024 * 1024));
        memoryCheck.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100.0) / 100.0);
        
        return memoryCheck;
    }
    
    /**
     * Generate health recommendations.
     */
    private Map<String, Object> generateHealthRecommendations(Map<String, Object> dbCheck, 
                                                            Map<String, Object> kafkaCheck, 
                                                            Map<String, Object> memoryCheck) {
        Map<String, Object> recommendations = new HashMap<>();
        
        if (!(Boolean) dbCheck.get("healthy")) {
            recommendations.put("database", "Check database connectivity and configuration");
        }
        
        if (!(Boolean) kafkaCheck.get("healthy")) {
            recommendations.put("kafka", "Check Kafka broker connectivity and configuration");
        }
        
        if (!(Boolean) memoryCheck.get("healthy")) {
            recommendations.put("memory", "Consider increasing JVM heap size or optimizing memory usage");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.put("overall", "System is healthy - no recommendations");
        }
        
        return recommendations;
    }
}