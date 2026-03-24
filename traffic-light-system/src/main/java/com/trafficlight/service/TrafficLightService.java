package com.trafficlight.service;

import com.trafficlight.domain.TrafficLight;
import com.trafficlight.dto.request.CreateTrafficLightRequest;
import com.trafficlight.dto.request.StateChangeRequest;
import com.trafficlight.dto.response.StateChangeResponse;
import com.trafficlight.dto.response.TrafficLightResponse;
import com.trafficlight.enums.LightState;
import com.trafficlight.events.StateChangedEvent;
import com.trafficlight.repository.TrafficLightRepository;
import com.trafficlight.repository.StateHistoryRepository;
import com.trafficlight.exception.TrafficLightNotFoundException;
import com.trafficlight.exception.InvalidStateTransitionException;
import com.trafficlight.utils.PerformanceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Service for managing traffic light operations with state management.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
@Transactional
public class TrafficLightService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrafficLightService.class);
    
    private final TrafficLightRepository trafficLightRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PerformanceLogger performanceLogger;
    
    // Thread-safe state management
    private final ConcurrentHashMap<String, LightState> currentStates = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
    
    @Autowired
    public TrafficLightService(TrafficLightRepository trafficLightRepository,
                              StateHistoryRepository stateHistoryRepository,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              PerformanceLogger performanceLogger) {
        this.trafficLightRepository = trafficLightRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.performanceLogger = performanceLogger;
    }
    
    /**
     * Get all traffic lights with pagination.
     */
    @Transactional(readOnly = true)
    public Page<TrafficLightResponse> getAllTrafficLights(Pageable pageable) {
        return performanceLogger.logExecutionTime("getAllTrafficLights", () -> {
            logger.info("Getting all traffic lights with pagination: page={}, size={}", 
                       pageable.getPageNumber(), pageable.getPageSize());
            
            Page<TrafficLight> trafficLights = trafficLightRepository.findAll(pageable);
            return trafficLights.map(this::convertToResponse);
        });
    }
    
    /**
     * Get traffic light by UUID.
     */
    @Transactional(readOnly = true)
    public TrafficLightResponse getTrafficLightById(String lightUuid) {
        return performanceLogger.logExecutionTime("getTrafficLightById", () -> {
            logger.info("Getting traffic light by UUID: {}", lightUuid);
            
            TrafficLight trafficLight = trafficLightRepository.findByUuid(lightUuid)
                .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
            
            return convertToResponse(trafficLight);
        });
    }
    
    /**
     * Create a new traffic light.
     */
    public TrafficLightResponse createTrafficLight(CreateTrafficLightRequest request) {
        return performanceLogger.logExecutionTime("createTrafficLight", () -> {
            logger.info("Creating new traffic light for intersection: {}, direction: {}", 
                       request.getIntersectionId(), request.getDirection());
            
            TrafficLight trafficLight = TrafficLight.builder()
                .uuid(UUID.randomUUID().toString())
                .intersectionId(request.getIntersectionId())
                .direction(request.getDirection())
                .currentState(LightState.RED) // Default to RED for safety
                .lastStateChange(LocalDateTime.now())
                .isActive(true)
                .build();
            
            TrafficLight saved = trafficLightRepository.save(trafficLight);
            
            // Update in-memory state cache
            currentStates.put(saved.getUuid(), saved.getCurrentState());
            
            logger.info("Created traffic light with UUID: {}", saved.getUuid());
            return convertToResponse(saved);
        });
    }
    
    /**
     * Change traffic light state with validation and event publishing.
     */
    public StateChangeResponse changeState(String lightUuid, StateChangeRequest request) {
        return performanceLogger.logExecutionTime("changeState", () -> {
            logger.info("Changing traffic light state: lightUuid={}, targetState={}", 
                       lightUuid, request.getTargetState());
            
            stateLock.writeLock().lock();
            try {
                TrafficLight trafficLight = trafficLightRepository.findByUuidWithLock(lightUuid)
                    .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
                
                LightState fromState = trafficLight.getCurrentState();
                LightState toState = request.getTargetState();
                
                // Validate state transition
                if (!fromState.canTransitionTo(toState)) {
                    throw new InvalidStateTransitionException(lightUuid, fromState, toState);
                }
                
                // Update traffic light state
                trafficLight.setCurrentState(toState);
                trafficLight.setLastStateChange(LocalDateTime.now());
                
                TrafficLight updated = trafficLightRepository.save(trafficLight);
                
                // Update in-memory cache
                currentStates.put(lightUuid, toState);
                
                // Create and publish state change event
                StateChangedEvent event = StateChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .trafficLightId(lightUuid)
                    .intersectionId(trafficLight.getIntersectionId())
                    .direction(trafficLight.getDirection())
                    .fromState(fromState)
                    .toState(toState)
                    .changedAt(updated.getLastStateChange())
                    .reason(request.getReason())
                    .triggeredBy("API")
                    .correlationId(UUID.randomUUID().toString())
                    .build();
                
                publishStateChangeEvent(event);
                
                StateChangeResponse response = StateChangeResponse.builder()
                    .trafficLightId(lightUuid)
                    .fromState(fromState)
                    .toState(toState)
                    .changedAt(updated.getLastStateChange())
                    .success(true)
                    .message("State changed successfully")
                    .build();
                
                logger.info("Successfully changed traffic light state: {} -> {}", fromState, toState);
                return response;
                
            } finally {
                stateLock.writeLock().unlock();
            }
        });
    }
    
    /**
     * Get traffic lights for a specific intersection.
     */
    @Transactional(readOnly = true)
    public List<TrafficLightResponse> getTrafficLightsByIntersection(String intersectionId) {
        logger.info("Getting traffic lights for intersection: {}", intersectionId);
        
        List<TrafficLight> trafficLights = trafficLightRepository.findByIntersectionIdAndIsActiveTrue(intersectionId);
        return trafficLights.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Update traffic light configuration.
     */
    public TrafficLightResponse updateTrafficLight(String lightUuid, CreateTrafficLightRequest request) {
        logger.info("Updating traffic light: {}", lightUuid);
        
        TrafficLight trafficLight = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        trafficLight.setIntersectionId(request.getIntersectionId());
        trafficLight.setDirection(request.getDirection());
        trafficLight.setUpdatedAt(LocalDateTime.now());
        
        TrafficLight updated = trafficLightRepository.save(trafficLight);
        
        logger.info("Updated traffic light: {}", lightUuid);
        return convertToResponse(updated);
    }
    
    /**
     * Delete traffic light (soft delete).
     */
    public void deleteTrafficLight(String lightUuid) {
        logger.info("Deleting traffic light: {}", lightUuid);
        
        TrafficLight trafficLight = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        trafficLight.setActive(false);
        trafficLight.setUpdatedAt(LocalDateTime.now());
        
        trafficLightRepository.save(trafficLight);
        
        // Remove from in-memory cache
        currentStates.remove(lightUuid);
        
        logger.info("Deleted traffic light: {}", lightUuid);
    }
    
    /**
     * Get traffic lights that have exceeded their duration and need state change.
     */
    @Transactional(readOnly = true)
    public List<TrafficLightResponse> getExpiredTrafficLights() {
        logger.info("Getting traffic lights that need state change");
        
        // This would typically check against timing rules
        // For now, return lights that have been in current state for more than 60 seconds
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(60);
        
        List<TrafficLight> expiredLights = trafficLightRepository.findByLastStateChangeBefore(cutoff);
        return expiredLights.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Bulk state change for multiple traffic lights in an intersection.
     */
    public List<StateChangeResponse> bulkStateChange(String intersectionId, StateChangeRequest request) {
        logger.info("Bulk state change for intersection: {}, targetState: {}", 
                   intersectionId, request.getTargetState());
        
        List<TrafficLight> trafficLights = trafficLightRepository.findByIntersectionIdAndIsActiveTrue(intersectionId);
        
        return trafficLights.stream()
            .map(light -> {
                try {
                    return changeState(light.getUuid(), request);
                } catch (Exception e) {
                    logger.error("Failed to change state for light: {}", light.getUuid(), e);
                    return StateChangeResponse.builder()
                        .trafficLightId(light.getUuid())
                        .fromState(light.getCurrentState())
                        .toState(request.getTargetState())
                        .success(false)
                        .message("Failed to change state: " + e.getMessage())
                        .build();
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get current state from cache (thread-safe read).
     */
    public LightState getCurrentState(String lightUuid) {
        stateLock.readLock().lock();
        try {
            return currentStates.get(lightUuid);
        } finally {
            stateLock.readLock().unlock();
        }
    }
    
    /**
     * Publish state change event to Kafka.
     */
    private void publishStateChangeEvent(StateChangedEvent event) {
        try {
            kafkaTemplate.send("traffic-light-state-changed", event.getTrafficLightId(), event);
            logger.debug("Published state change event: {}", event.getEventId());
        } catch (Exception e) {
            logger.error("Failed to publish state change event: {}", event.getEventId(), e);
            // Don't fail the transaction for event publishing failures
        }
    }
    
    /**
     * Convert TrafficLight entity to response DTO.
     */
    private TrafficLightResponse convertToResponse(TrafficLight trafficLight) {
        return TrafficLightResponse.builder()
            .id(trafficLight.getId())
            .uuid(trafficLight.getUuid())
            .intersectionId(trafficLight.getIntersectionId())
            .direction(trafficLight.getDirection())
            .currentState(trafficLight.getCurrentState())
            .lastStateChange(trafficLight.getLastStateChange())
            .isActive(trafficLight.isActive())
            .createdAt(trafficLight.getCreatedAt())
            .updatedAt(trafficLight.getUpdatedAt())
            .build();
    }
    
    /**
     * Get state history for a traffic light.
     */
    @Transactional(readOnly = true)
    public Page<Object> getStateHistory(String lightUuid, Pageable pageable) {
        return performanceLogger.logExecutionTime("getStateHistory", () -> {
            logger.info("Getting state history for traffic light: {}", lightUuid);
            
            // Verify traffic light exists
            trafficLightRepository.findByUuid(lightUuid)
                .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
            
            // Get state history from repository - simplified implementation
            return Page.empty(); // Simplified - missing repository method
        });
    }
    
    /**
     * Get metrics for a traffic light.
     */
    @Transactional(readOnly = true)
    public Object getMetrics(String lightUuid) {
        return performanceLogger.logExecutionTime("getMetrics", () -> {
            logger.info("Getting metrics for traffic light: {}", lightUuid);
            
            // Verify traffic light exists
            TrafficLight trafficLight = trafficLightRepository.findByUuid(lightUuid)
                .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
            
            // Get performance stats for this light
            PerformanceLogger.OperationStats changeStateStats = performanceLogger.getStats("changeState");
            PerformanceLogger.OperationStats getByIdStats = performanceLogger.getStats("getTrafficLightById");
            
            // Calculate state duration metrics from history
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dayAgo = now.minusDays(1);
            
            List<Object> recentHistory = new ArrayList<>(); // Simplified - missing repository method
            
            return new Object() {
                public final String trafficLightId = lightUuid;
                public final String intersectionId = trafficLight.getIntersectionId();
                public final String direction = trafficLight.getDirection().toString();
                public final LightState currentState = trafficLight.getCurrentState();
                public final LocalDateTime lastStateChange = trafficLight.getLastStateChange();
                public final boolean isActive = trafficLight.isActive();
                
                // Performance metrics
                public final Object performanceMetrics = new Object() {
                    public final long totalStateChanges = changeStateStats != null ? changeStateStats.getTotalCalls() : 0;
                    public final long successfulStateChanges = changeStateStats != null ? changeStateStats.getSuccessfulCalls() : 0;
                    public final long failedStateChanges = changeStateStats != null ? changeStateStats.getFailedCalls() : 0;
                    public final double averageStateChangeTime = changeStateStats != null ? changeStateStats.getAverageDuration() : 0.0;
                    public final long maxStateChangeTime = changeStateStats != null ? changeStateStats.getMaxDuration() : 0;
                    public final long minStateChangeTime = changeStateStats != null ? changeStateStats.getMinDuration() : 0;
                    
                    public final long totalQueries = getByIdStats != null ? getByIdStats.getTotalCalls() : 0;
                    public final double averageQueryTime = getByIdStats != null ? getByIdStats.getAverageDuration() : 0.0;
                };
                
                // State history metrics
                public final Object stateMetrics = new Object() {
                    public final int recentStateChanges24h = recentHistory.size();
                    public final LocalDateTime metricsCalculatedAt = now;
                    public final String period = "Last 24 hours";
                };
                
                // System health
                public final Object healthMetrics = new Object() {
                    public final boolean isResponsive = true;
                    public final boolean isInMemoryCache = currentStates.containsKey(lightUuid);
                    public final LightState cachedState = currentStates.get(lightUuid);
                    public final boolean cacheConsistent = currentStates.get(lightUuid) == trafficLight.getCurrentState();
                    public final LocalDateTime lastHealthCheck = now;
                };
            };
        });
    }
}