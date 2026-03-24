package com.trafficlight.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Implementation of CustomLogger with structured logging, correlation IDs, and performance tracking.
 * Uses SLF4J with MDC (Mapped Diagnostic Context) for correlation ID propagation.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class StructuredLogger implements CustomLogger {
    
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String PERFORMANCE_MARKER = "[PERFORMANCE]";
    
    private final Logger logger;
    
    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }
    
    public StructuredLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }
    
    @Override
    public void debug(String message) {
        logger.debug(enrichMessage(message));
    }
    
    @Override
    public void debug(String message, Object... args) {
        logger.debug(enrichMessage(message), args);
    }
    
    @Override
    public void debug(String message, Throwable throwable) {
        logger.debug(enrichMessage(message), throwable);
    }
    
    @Override
    public void info(String message) {
        logger.info(enrichMessage(message));
    }
    
    @Override
    public void info(String message, Object... args) {
        logger.info(enrichMessage(message), args);
    }
    
    @Override
    public void info(String message, Throwable throwable) {
        logger.info(enrichMessage(message), throwable);
    }
    
    @Override
    public void warn(String message) {
        logger.warn(enrichMessage(message));
    }
    
    @Override
    public void warn(String message, Object... args) {
        logger.warn(enrichMessage(message), args);
    }
    
    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(enrichMessage(message), throwable);
    }
    
    @Override
    public void error(String message) {
        logger.error(enrichMessage(message));
    }
    
    @Override
    public void error(String message, Object... args) {
        logger.error(enrichMessage(message), args);
    }
    
    @Override
    public void error(String message, Throwable throwable) {
        logger.error(enrichMessage(message), throwable);
    }
    
    @Override
    public void logPerformance(String operation, long durationMs) {
        logPerformance(operation, durationMs, null);
    }
    
    @Override
    public void logPerformance(String operation, long durationMs, String details) {
        String message = String.format("%s Operation: %s, Duration: %dms", 
            PERFORMANCE_MARKER, operation, durationMs);
        
        if (details != null && !details.isEmpty()) {
            message += ", Details: " + details;
        }
        
        if (durationMs > 1000) {
            logger.warn(enrichMessage(message));
        } else {
            logger.info(enrichMessage(message));
        }
    }
    
    @Override
    public String getCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            setCorrelationId(correlationId);
        }
        return correlationId;
    }
    
    @Override
    public void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isEmpty()) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        } else {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
    
    private String enrichMessage(String message) {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId != null) {
            return String.format("[%s] %s", correlationId, message);
        }
        return message;
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    public static CustomLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }
    
    public static CustomLogger getLogger(String name) {
        return new StructuredLogger(name);
    }
}
