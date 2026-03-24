package com.trafficlight.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Performance logging utility for tracking operation execution times.
 * Provides method execution timing and statistics collection.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class PerformanceLogger {
    
    private static final Logger performanceLogger = LoggerFactory.getLogger("com.trafficlight.performance");
    private final CustomLogger logger = StructuredLogger.getLogger(PerformanceLogger.class);
    private final ConcurrentHashMap<String, OperationStats> operationStats = new ConcurrentHashMap<>();
    
    public <T> T logExecutionTime(String operation, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        try {
            T result = supplier.get();
            long duration = System.currentTimeMillis() - startTime;
            logPerformance(operation, duration, true);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logPerformance(operation, duration, false);
            throw e;
        }
    }
    
    public void logExecutionTime(String operation, Runnable runnable) {
        logExecutionTime(operation, () -> {
            runnable.run();
            return null;
        });
    }
    
    private void logPerformance(String operation, long duration, boolean success) {
        // Log to performance logger with MDC context
        MDC.put("operation", operation);
        MDC.put("duration", String.valueOf(duration));
        
        String message = String.format("Operation: %s completed in %dms - Success: %s", 
                                      operation, duration, success);
        
        if (duration > 1000) {
            performanceLogger.warn(message);
        } else {
            performanceLogger.info(message);
        }
        
        // Clear MDC
        MDC.remove("operation");
        MDC.remove("duration");
        
        // Also log to regular logger for debugging
        logger.logPerformance(operation, duration, "Success: " + success);
        updateStats(operation, duration, success);
    }
    
    private void updateStats(String operation, long duration, boolean success) {
        operationStats.computeIfAbsent(operation, k -> new OperationStats())
            .record(duration, success);
    }
    
    public OperationStats getStats(String operation) {
        return operationStats.get(operation);
    }
    
    public void resetStats() {
        operationStats.clear();
    }
    
    public static class OperationStats {
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong failedCalls = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong(0);
        
        public void record(long duration, boolean success) {
            totalCalls.incrementAndGet();
            if (success) {
                successfulCalls.incrementAndGet();
            } else {
                failedCalls.incrementAndGet();
            }
            totalDuration.addAndGet(duration);
            
            updateMin(duration);
            updateMax(duration);
        }
        
        private void updateMin(long duration) {
            long current;
            do {
                current = minDuration.get();
                if (duration >= current) break;
            } while (!minDuration.compareAndSet(current, duration));
        }
        
        private void updateMax(long duration) {
            long current;
            do {
                current = maxDuration.get();
                if (duration <= current) break;
            } while (!maxDuration.compareAndSet(current, duration));
        }
        
        public long getTotalCalls() {
            return totalCalls.get();
        }
        
        public long getSuccessfulCalls() {
            return successfulCalls.get();
        }
        
        public long getFailedCalls() {
            return failedCalls.get();
        }
        
        public double getAverageDuration() {
            long calls = totalCalls.get();
            return calls > 0 ? (double) totalDuration.get() / calls : 0.0;
        }
        
        public long getMinDuration() {
            long min = minDuration.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxDuration() {
            return maxDuration.get();
        }
    }
}
