package com.trafficlight.events.publisher;

import com.trafficlight.events.StateChangedEvent;
import com.trafficlight.events.IntersectionUpdatedEvent;
import com.trafficlight.events.SystemHealthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Publisher for traffic light related events with transaction support.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class TrafficLightEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(TrafficLightEventPublisher.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Topic names
    private static final String STATE_CHANGED_TOPIC = "traffic-light-state-changed";
    private static final String INTERSECTION_UPDATED_TOPIC = "intersection-updated";
    private static final String SYSTEM_HEALTH_TOPIC = "system-health";
    
    @Autowired
    public TrafficLightEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publish state changed event with transaction support.
     * 
     * @param event the state changed event
     */
    @Transactional
    public void publishStateChangedEvent(StateChangedEvent event) {
        try {
            // Add correlation ID from MDC
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                event.setCorrelationId(correlationId);
            }
            
            logger.info("Publishing state changed event: trafficLightId={}, fromState={}, toState={}, eventId={}", 
                       event.getTrafficLightId(), event.getFromState(), event.getToState(), event.getEventId());
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                STATE_CHANGED_TOPIC, 
                event.getTrafficLightId(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Successfully published state changed event: eventId={}, offset={}", 
                                event.getEventId(), result.getRecordMetadata().offset());
                    
                    // Add Kafka metadata to MDC for logging
                    MDC.put("kafkaTopic", STATE_CHANGED_TOPIC);
                    MDC.put("kafkaPartition", String.valueOf(result.getRecordMetadata().partition()));
                    MDC.put("kafkaOffset", String.valueOf(result.getRecordMetadata().offset()));
                    MDC.put("kafkaKey", event.getTrafficLightId());
                    
                } else {
                    logger.error("Failed to publish state changed event: eventId={}, error={}", 
                                event.getEventId(), ex.getMessage(), ex);
                    
                    // In a production system, you might want to:
                    // 1. Store failed events in a dead letter queue
                    // 2. Retry with exponential backoff
                    // 3. Alert monitoring systems
                }
            });
            
        } catch (Exception e) {
            logger.error("Exception while publishing state changed event: eventId={}", event.getEventId(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }
    
    /**
     * Publish intersection updated event with transaction support.
     * 
     * @param event the intersection updated event
     */
    @Transactional
    public void publishIntersectionUpdatedEvent(IntersectionUpdatedEvent event) {
        try {
            // Add correlation ID from MDC
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                event.setCorrelationId(correlationId);
            }
            
            logger.info("Publishing intersection updated event: intersectionId={}, updateType={}, eventId={}", 
                       event.getIntersectionId(), event.getUpdateType(), event.getEventId());
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                INTERSECTION_UPDATED_TOPIC, 
                event.getIntersectionId(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Successfully published intersection updated event: eventId={}, offset={}", 
                                event.getEventId(), result.getRecordMetadata().offset());
                    
                    // Add Kafka metadata to MDC for logging
                    MDC.put("kafkaTopic", INTERSECTION_UPDATED_TOPIC);
                    MDC.put("kafkaPartition", String.valueOf(result.getRecordMetadata().partition()));
                    MDC.put("kafkaOffset", String.valueOf(result.getRecordMetadata().offset()));
                    MDC.put("kafkaKey", event.getIntersectionId());
                    
                } else {
                    logger.error("Failed to publish intersection updated event: eventId={}, error={}", 
                                event.getEventId(), ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Exception while publishing intersection updated event: eventId={}", event.getEventId(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }
    
    /**
     * Publish system health event without requiring a transaction.
     * Uses executeInTransaction to handle transaction internally.
     * 
     * @param event the system health event
     */
    public void publishSystemHealthEvent(SystemHealthEvent event) {
        try {
            // Add correlation ID from MDC
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                event.setCorrelationId(correlationId);
            }
            
            logger.info("Publishing system health event: healthStatus={}, source={}, eventId={}", 
                       event.getHealthStatus(), event.getSource(), event.getEventId());
            
            // Use executeInTransaction to handle transaction internally
            kafkaTemplate.executeInTransaction(operations -> {
                CompletableFuture<SendResult<String, Object>> future = operations.send(
                    SYSTEM_HEALTH_TOPIC, 
                    event.getSource(), 
                    event
                );
                
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.debug("Successfully published system health event: eventId={}, offset={}", 
                                    event.getEventId(), result.getRecordMetadata().offset());
                        
                        // Add Kafka metadata to MDC for logging
                        MDC.put("kafkaTopic", SYSTEM_HEALTH_TOPIC);
                        MDC.put("kafkaPartition", String.valueOf(result.getRecordMetadata().partition()));
                        MDC.put("kafkaOffset", String.valueOf(result.getRecordMetadata().offset()));
                        MDC.put("kafkaKey", event.getSource());
                        
                    } else {
                        logger.error("Failed to publish system health event: eventId={}, error={}", 
                                    event.getEventId(), ex.getMessage(), ex);
                    }
                });
                
                return true;
            });
            
        } catch (Exception e) {
            logger.error("Exception while publishing system health event: eventId={}", event.getEventId(), e);
            // Don't re-throw for health events as they shouldn't affect business transactions
        }
    }
    
    /**
     * Publish event with custom topic and key.
     * 
     * @param topic the Kafka topic
     * @param key the message key
     * @param event the event object
     */
    public void publishEvent(String topic, String key, Object event) {
        try {
            logger.info("Publishing custom event: topic={}, key={}", topic, key);
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.debug("Successfully published custom event: topic={}, key={}, offset={}", 
                                topic, key, result.getRecordMetadata().offset());
                    
                    // Add Kafka metadata to MDC for logging
                    MDC.put("kafkaTopic", topic);
                    MDC.put("kafkaPartition", String.valueOf(result.getRecordMetadata().partition()));
                    MDC.put("kafkaOffset", String.valueOf(result.getRecordMetadata().offset()));
                    MDC.put("kafkaKey", key);
                    
                } else {
                    logger.error("Failed to publish custom event: topic={}, key={}, error={}", 
                                topic, key, ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Exception while publishing custom event: topic={}, key={}", topic, key, e);
        }
    }
    
    /**
     * Check if Kafka is available.
     * Uses executeInTransaction to handle transaction internally.
     * 
     * @return true if Kafka is available, false otherwise
     */
    public boolean isKafkaAvailable() {
        try {
            // Simple health check by sending a test message using executeInTransaction
            kafkaTemplate.executeInTransaction(operations -> {
                operations.send("health-check", "ping", "pong");
                return true;
            });
            return true;
        } catch (Exception e) {
            logger.warn("Kafka health check failed: {}", e.getMessage());
            return false;
        }
    }
}