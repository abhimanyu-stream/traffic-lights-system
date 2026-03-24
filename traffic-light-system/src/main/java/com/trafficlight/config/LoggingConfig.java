package com.trafficlight.config;

import org.springframework.context.annotation.Configuration;

/**
 * Logging configuration for the Traffic Light Controller application.
 * Configures structured logging with correlation IDs and performance tracking.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Configuration
public class LoggingConfig {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    // Log level configuration is handled in application.yml and logback-spring.xml
}
