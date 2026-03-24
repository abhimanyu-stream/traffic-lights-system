package com.trafficlight.utils;

import com.trafficlight.utils.CustomLogger;
import com.trafficlight.utils.StructuredLogger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit logging component for tracking security-relevant events.
 * Logs user actions, authentication attempts, and data modifications.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class AuditLogger {
    
    private static final CustomLogger logger = StructuredLogger.getLogger(AuditLogger.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public void logAccess(String userId, String resource, String action) {
        String message = String.format("ACCESS: User=%s, Resource=%s, Action=%s, Time=%s",
            userId, resource, action, LocalDateTime.now().format(FORMATTER));
        logger.info(message);
    }
    
    public void logModification(String userId, String entityType, String entityId, String action) {
        String message = String.format("MODIFICATION: User=%s, Entity=%s, ID=%s, Action=%s, Time=%s",
            userId, entityType, entityId, action, LocalDateTime.now().format(FORMATTER));
        logger.info(message);
    }
    
    public void logSecurityEvent(String eventType, String details) {
        String message = String.format("SECURITY: Event=%s, Details=%s, Time=%s",
            eventType, details, LocalDateTime.now().format(FORMATTER));
        logger.warn(message);
    }
    
    public void logAuthenticationAttempt(String userId, boolean success, String ipAddress) {
        String message = String.format("AUTH: User=%s, Success=%s, IP=%s, Time=%s",
            userId, success, ipAddress, LocalDateTime.now().format(FORMATTER));
        if (success) {
            logger.info(message);
        } else {
            logger.warn(message);
        }
    }
    
    public void logDataExport(String userId, String dataType, int recordCount) {
        String message = String.format("EXPORT: User=%s, DataType=%s, Records=%d, Time=%s",
            userId, dataType, recordCount, LocalDateTime.now().format(FORMATTER));
        logger.info(message);
    }
}
