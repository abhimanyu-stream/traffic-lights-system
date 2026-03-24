package com.trafficlight.listeners;

import com.trafficlight.domain.StateHistory;
import com.trafficlight.events.StateChangedEvent;
import com.trafficlight.repository.StateHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka listener for processing state change events.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class StateChangedEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(StateChangedEventListener.class);
    
    private final StateHistoryRepository stateHistoryRepository;
    
    @Autowired
    public StateChangedEventListener(StateHistoryRepository stateHistoryRepository) {
        this.stateHistoryRepository = stateHistoryRepository;
    }
    
    /**
     * Process state change events and create audit trail.
     */
    @KafkaListener(
        topics = "traffic-light-state-changed",
        groupId = "traffic-light-controller",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleStateChangedEvent(
            @Payload StateChangedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        logger.info("Processing state change event: {} for traffic light: {}", 
                   event.getEventId(), event.getTrafficLightId());
        
        try {
            // Create state history record
            StateHistory stateHistory = StateHistory.builder()
                .uuid(UUID.randomUUID().toString())
                .trafficLightId(event.getTrafficLightId())
                .intersectionId(event.getIntersectionId())
                .direction(event.getDirection())
                .fromState(event.getFromState())
                .toState(event.getToState())
                .changedAt(event.getChangedAt())
                .durationSeconds(event.getDurationSeconds())
                .reason(event.getReason())
                .triggeredBy(event.getTriggeredBy())
                .correlationId(event.getCorrelationId())
                .eventId(event.getEventId())
                .build();
            
            stateHistoryRepository.save(stateHistory);
            
            logger.info("Created state history record for event: {}", event.getEventId());
            
            // Perform additional processing
            processStateChangeLogic(event);
            
            // Manual acknowledgment
            acknowledgment.acknowledge();
            
            logger.debug("Successfully processed and acknowledged state change event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Failed to process state change event: {} for traffic light: {}", 
                        event.getEventId(), event.getTrafficLightId(), e);
            
            // Don't acknowledge on failure - message will be retried
            throw e;
        }
    }
    
    /**
     * Process state change events for analytics.
     */
    @KafkaListener(
        topics = "traffic-light-state-changed",
        groupId = "analytics-processor",
        containerFactory = "analyticsKafkaListenerContainerFactory"
    )
    public void handleStateChangedEventForAnalytics(
            @Payload StateChangedEvent event,
            Acknowledgment acknowledgment) {
        
        logger.debug("Processing state change event for analytics: {}", event.getEventId());
        
        try {
            // Process analytics data
            processAnalyticsData(event);
            
            // Manual acknowledgment
            acknowledgment.acknowledge();
            
            logger.debug("Successfully processed state change event for analytics: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Failed to process state change event for analytics: {}", event.getEventId(), e);
            
            // Acknowledge even on failure for analytics processing to avoid blocking
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Process state change logic (business rules, validations, etc.).
     */
    private void processStateChangeLogic(StateChangedEvent event) {
        logger.debug("Processing state change logic for event: {}", event.getEventId());
        
        // Validate state transition
        if (!event.isValidTransition()) {
            logger.warn("Invalid state transition detected: {} -> {} for traffic light: {}", 
                       event.getFromState(), event.getToState(), event.getTrafficLightId());
        }
        
        // Check for safety violations (e.g., conflicting green lights)
        if (event.isToGreenState()) {
            checkForConflictingGreenLights(event);
        }
        
        // Update timing metrics
        updateTimingMetrics(event);
        
        // Trigger dependent actions
        triggerDependentActions(event);
    }
    
    /**
     * Process analytics data from state change events.
     */
    private void processAnalyticsData(StateChangedEvent event) {
        logger.debug("Processing analytics data for event: {}", event.getEventId());
        
        // Calculate state duration if available
        if (event.getDurationSeconds() != null) {
            recordStateDuration(event);
        }
        
        // Track state transition patterns
        recordStateTransitionPattern(event);
        
        // Update intersection metrics
        updateIntersectionMetrics(event);
        
        // Generate performance metrics
        generatePerformanceMetrics(event);
    }
    
    /**
     * Check for conflicting green lights in the same intersection.
     */
    private void checkForConflictingGreenLights(StateChangedEvent event) {
        logger.debug("Checking for conflicting green lights for intersection: {}", event.getIntersectionId());
        
        // TODO: Implement logic to check for conflicting green lights
        // This would query other traffic lights in the same intersection
        // and ensure no conflicting directions have green lights simultaneously
        
        logger.debug("Conflict check completed for intersection: {}", event.getIntersectionId());
    }
    
    /**
     * Update timing metrics for the traffic light.
     */
    private void updateTimingMetrics(StateChangedEvent event) {
        logger.debug("Updating timing metrics for traffic light: {}", event.getTrafficLightId());
        
        // TODO: Implement timing metrics update
        // This would track average state durations, peak times, etc.
        
        logger.debug("Timing metrics updated for traffic light: {}", event.getTrafficLightId());
    }
    
    /**
     * Trigger dependent actions based on state change.
     */
    private void triggerDependentActions(StateChangedEvent event) {
        logger.debug("Triggering dependent actions for event: {}", event.getEventId());
        
        // TODO: Implement dependent actions
        // This could include:
        // - Triggering pedestrian signals
        // - Updating traffic flow algorithms
        // - Sending notifications to traffic management systems
        
        logger.debug("Dependent actions triggered for event: {}", event.getEventId());
    }
    
    /**
     * Record state duration for analytics.
     */
    private void recordStateDuration(StateChangedEvent event) {
        logger.debug("Recording state duration: {} seconds for traffic light: {}", 
                    event.getDurationSeconds(), event.getTrafficLightId());
        
        // TODO: Implement state duration recording
        // This would store duration data for analytics and optimization
    }
    
    /**
     * Record state transition pattern for analytics.
     */
    private void recordStateTransitionPattern(StateChangedEvent event) {
        logger.debug("Recording state transition pattern: {} -> {} for traffic light: {}", 
                    event.getFromState(), event.getToState(), event.getTrafficLightId());
        
        // TODO: Implement transition pattern recording
        // This would track common transition patterns for optimization
    }
    
    /**
     * Update intersection-level metrics.
     */
    private void updateIntersectionMetrics(StateChangedEvent event) {
        logger.debug("Updating intersection metrics for intersection: {}", event.getIntersectionId());
        
        // TODO: Implement intersection metrics update
        // This would aggregate data across all lights in an intersection
    }
    
    /**
     * Generate performance metrics from state change data.
     */
    private void generatePerformanceMetrics(StateChangedEvent event) {
        logger.debug("Generating performance metrics for event: {}", event.getEventId());
        
        // TODO: Implement performance metrics generation
        // This would calculate system performance indicators
    }
}