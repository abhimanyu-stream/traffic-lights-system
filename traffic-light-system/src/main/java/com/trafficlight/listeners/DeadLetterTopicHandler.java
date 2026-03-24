package com.trafficlight.listeners;

import com.trafficlight.utils.CustomLogger;
import com.trafficlight.utils.StructuredLogger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Handler for Dead Letter Topic (DLT) messages.
 * Processes failed messages that could not be consumed successfully.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class DeadLetterTopicHandler {
    
    private static final CustomLogger logger = StructuredLogger.getLogger(DeadLetterTopicHandler.class);
    
    @KafkaListener(
        topics = "${kafka.topics.state-changed-dlt}",
        groupId = "dlt-handler-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStateChangedDLT(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(value = KafkaHeaders.EXCEPTION_STACKTRACE, required = false) String stackTrace) {
        
        logger.error("DLT Message received from topic: {}", topic);
        logger.error("Failed message: {}", message);
        logger.error("Exception: {}", exceptionMessage);
        
        // Store in database for manual review
        storeDLTMessage(topic, message, exceptionMessage);
        
        // Send alert notification
        sendAlert(topic, message, exceptionMessage);
    }
    
    @KafkaListener(
        topics = "${kafka.topics.intersection-updated-dlt}",
        groupId = "dlt-handler-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleIntersectionUpdatedDLT(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {
        
        logger.error("DLT Message received from topic: {}", topic);
        logger.error("Failed message: {}", message);
        logger.error("Exception: {}", exceptionMessage);
        
        storeDLTMessage(topic, message, exceptionMessage);
        sendAlert(topic, message, exceptionMessage);
    }
    
    @KafkaListener(
        topics = "${kafka.topics.system-events-dlt}",
        groupId = "dlt-handler-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSystemEventsDLT(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {
        
        logger.error("DLT Message received from topic: {}", topic);
        logger.error("Failed message: {}", message);
        logger.error("Exception: {}", exceptionMessage);
        
        storeDLTMessage(topic, message, exceptionMessage);
        sendAlert(topic, message, exceptionMessage);
    }
    
    private void storeDLTMessage(String topic, String message, String exceptionMessage) {
        // Store in database for manual review and retry
        logger.info("Storing DLT message for topic: {} in database", topic);
        // Implementation would store in a DLT_MESSAGES table
    }
    
    private void sendAlert(String topic, String message, String exceptionMessage) {
        // Send alert to monitoring system or email
        logger.warn("Sending alert for DLT message from topic: {}", topic);
        // Implementation would send to monitoring system
    }
}
