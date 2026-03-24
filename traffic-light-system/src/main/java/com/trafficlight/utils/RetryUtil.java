package com.trafficlight.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Utility class for implementing retry logic with exponential backoff.
 * Provides configurable retry mechanisms for operations that may fail transiently.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class RetryUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);
    
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_INITIAL_DELAY_MS = 1000;
    private static final double DEFAULT_MULTIPLIER = 2.0;
    private static final long DEFAULT_MAX_DELAY_MS = 30000;
    
    /**
     * Retry configuration builder.
     */
    public static class RetryConfig {
        private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
        private long initialDelayMs = DEFAULT_INITIAL_DELAY_MS;
        private double multiplier = DEFAULT_MULTIPLIER;
        private long maxDelayMs = DEFAULT_MAX_DELAY_MS;
        private Class<? extends Exception>[] retryableExceptions;
        
        public RetryConfig maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }
        
        public RetryConfig initialDelay(Duration initialDelay) {
            this.initialDelayMs = initialDelay.toMillis();
            return this;
        }
        
        public RetryConfig multiplier(double multiplier) {
            this.multiplier = multiplier;
            return this;
        }
        
        public RetryConfig maxDelay(Duration maxDelay) {
            this.maxDelayMs = maxDelay.toMillis();
            return this;
        }
        
        @SafeVarargs
        public final RetryConfig retryOn(Class<? extends Exception>... exceptions) {
            this.retryableExceptions = exceptions;
            return this;
        }
        
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public long getInitialDelayMs() {
            return initialDelayMs;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
        
        public long getMaxDelayMs() {
            return maxDelayMs;
        }
        
        public Class<? extends Exception>[] getRetryableExceptions() {
            return retryableExceptions;
        }
    }
    
    /**
     * Execute a supplier with retry logic and exponential backoff.
     * 
     * @param <T> the return type
     * @param supplier the operation to execute
     * @param config retry configuration
     * @return the result of the operation
     * @throws Exception if all retry attempts fail
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, RetryConfig config) throws Exception {
        Exception lastException = null;
        long delayMs = config.getInitialDelayMs();
        
        for (int attempt = 1; attempt <= config.getMaxAttempts(); attempt++) {
            try {
                logger.debug("Executing operation, attempt {}/{}", attempt, config.getMaxAttempts());
                return supplier.get();
                
            } catch (Exception e) {
                lastException = e;
                
                // Check if exception is retryable
                if (!isRetryable(e, config)) {
                    logger.error("Non-retryable exception occurred: {}", e.getMessage());
                    throw e;
                }
                
                // Check if we should retry
                if (attempt < config.getMaxAttempts()) {
                    logger.warn("Operation failed on attempt {}/{}, retrying after {}ms. Error: {}", 
                               attempt, config.getMaxAttempts(), delayMs, e.getMessage());
                    
                    // Wait before retry
                    sleep(delayMs);
                    
                    // Calculate next delay with exponential backoff
                    delayMs = Math.min((long) (delayMs * config.getMultiplier()), config.getMaxDelayMs());
                } else {
                    logger.error("Operation failed after {} attempts", config.getMaxAttempts(), e);
                }
            }
        }
        
        throw lastException;
    }
    
    /**
     * Execute a runnable with retry logic and exponential backoff.
     * 
     * @param runnable the operation to execute
     * @param config retry configuration
     * @throws Exception if all retry attempts fail
     */
    public static void executeWithRetry(Runnable runnable, RetryConfig config) throws Exception {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        }, config);
    }
    
    /**
     * Execute with default retry configuration.
     * 
     * @param <T> the return type
     * @param supplier the operation to execute
     * @return the result of the operation
     * @throws Exception if all retry attempts fail
     */
    public static <T> T executeWithDefaultRetry(Supplier<T> supplier) throws Exception {
        return executeWithRetry(supplier, new RetryConfig());
    }
    
    /**
     * Execute with default retry configuration.
     * 
     * @param runnable the operation to execute
     * @throws Exception if all retry attempts fail
     */
    public static void executeWithDefaultRetry(Runnable runnable) throws Exception {
        executeWithRetry(runnable, new RetryConfig());
    }
    
    /**
     * Check if an exception is retryable based on configuration.
     */
    private static boolean isRetryable(Exception e, RetryConfig config) {
        if (config.getRetryableExceptions() == null || config.getRetryableExceptions().length == 0) {
            // If no specific exceptions configured, retry all
            return true;
        }
        
        for (Class<? extends Exception> retryableException : config.getRetryableExceptions()) {
            if (retryableException.isInstance(e)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Sleep for the specified duration.
     */
    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sleep interrupted during retry backoff");
        }
    }
    
    /**
     * Create a new retry configuration builder.
     */
    public static RetryConfig config() {
        return new RetryConfig();
    }
}
