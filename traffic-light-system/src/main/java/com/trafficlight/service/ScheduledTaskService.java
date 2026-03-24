package com.trafficlight.service;

import com.trafficlight.repository.StateHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for scheduled maintenance tasks.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
public class ScheduledTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
    
    private final StateHistoryRepository stateHistoryRepository;
    private final SystemHealthService systemHealthService;
    
    @Autowired
    public ScheduledTaskService(StateHistoryRepository stateHistoryRepository,
                               SystemHealthService systemHealthService) {
        this.stateHistoryRepository = stateHistoryRepository;
        this.systemHealthService = systemHealthService;
    }
    
    /**
     * Perform system health check every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Async("scheduledExecutor")
    public void performHealthCheck() {
        logger.debug("Starting scheduled health check");
        
        try {
            systemHealthService.performHealthCheck();
            logger.debug("Scheduled health check completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled health check", e);
        }
    }
    
    /**
     * Clean up old state history records every hour.
     * Keeps records for the last 30 days.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Async("scheduledExecutor")
    @Transactional
    public void cleanupOldStateHistory() {
        logger.info("Starting state history cleanup");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = stateHistoryRepository.deleteByChangedAtBefore(cutoffDate);
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} old state history records", deletedCount);
            } else {
                logger.debug("No old state history records to clean up");
            }
            
        } catch (Exception e) {
            logger.error("Error during state history cleanup", e);
        }
    }
    
    /**
     * Log system metrics every 15 minutes.
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    @Async("scheduledExecutor")
    public void logSystemMetrics() {
        logger.debug("Logging system metrics");
        
        try {
            var metrics = systemHealthService.getSystemMetrics();
            
            @SuppressWarnings("unchecked")
            var memoryInfo = (java.util.Map<String, Object>) metrics.get("memory");
            
            logger.info("System Metrics - Memory: {} used / {} max, Processors: {}, Uptime: {} ms",
                       memoryInfo.get("used"),
                       memoryInfo.get("max"),
                       metrics.get("processors"),
                       metrics.get("uptime"));
                       
        } catch (Exception e) {
            logger.error("Error logging system metrics", e);
        }
    }
    
    /**
     * Perform garbage collection hint every 30 minutes.
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    @Async("scheduledExecutor")
    public void performGarbageCollectionHint() {
        logger.debug("Suggesting garbage collection");
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long beforeGC = runtime.totalMemory() - runtime.freeMemory();
            
            // Suggest garbage collection
            System.gc();
            
            // Wait a moment for GC to potentially run
            Thread.sleep(1000);
            
            long afterGC = runtime.totalMemory() - runtime.freeMemory();
            long freed = beforeGC - afterGC;
            
            if (freed > 0) {
                logger.info("Garbage collection freed approximately {} bytes", freed);
            }
            
        } catch (Exception e) {
            logger.error("Error during garbage collection hint", e);
        }
    }
    
    /**
     * Check for system anomalies every 10 minutes.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Async("scheduledExecutor")
    public void checkSystemAnomalies() {
        logger.debug("Checking for system anomalies");
        
        try {
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            double memoryUsagePercent = (double) usedMemory / runtime.maxMemory() * 100;
            
            if (memoryUsagePercent > 85) {
                logger.warn("High memory usage detected: {:.2f}%", memoryUsagePercent);
                systemHealthService.publishHealthEvent("APPLICATION", "DEGRADED", 
                    String.format("High memory usage: %.2f%%", memoryUsagePercent));
            }
            
            // Check if system is responsive
            boolean systemReady = systemHealthService.isSystemReady();
            if (!systemReady) {
                logger.warn("System readiness check failed");
                systemHealthService.publishHealthEvent("APPLICATION", "CRITICAL", 
                    "System readiness check failed");
            }
            
        } catch (Exception e) {
            logger.error("Error during anomaly check", e);
        }
    }
    
    /**
     * Validate system configuration every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Async("scheduledExecutor")
    public void validateSystemConfiguration() {
        logger.debug("Validating system configuration");
        
        try {
            // Check database connectivity
            boolean dbHealthy = systemHealthService.checkDatabaseHealth();
            if (!dbHealthy) {
                logger.error("Database health check failed during configuration validation");
            }
            
            // Check Kafka connectivity
            boolean kafkaHealthy = systemHealthService.checkKafkaHealth();
            if (!kafkaHealthy) {
                logger.error("Kafka health check failed during configuration validation");
            }
            
            if (dbHealthy && kafkaHealthy) {
                logger.debug("System configuration validation passed");
            } else {
                logger.warn("System configuration validation detected issues");
            }
            
        } catch (Exception e) {
            logger.error("Error during configuration validation", e);
        }
    }
}