package com.trafficlight.service;

import com.trafficlight.domain.StateHistory;
import com.trafficlight.domain.TrafficLight;
import com.trafficlight.dto.request.StateChangeRequest;
import com.trafficlight.dto.response.StateChangeResponse;
import com.trafficlight.exception.ConcurrentModificationException;
import com.trafficlight.exception.TrafficLightNotFoundException;
import com.trafficlight.repository.StateHistoryRepository;
import com.trafficlight.repository.TrafficLightRepository;
import com.trafficlight.utils.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service demonstrating proper transaction propagation and optimistic locking handling.
 * Shows REQUIRED, REQUIRES_NEW, and other propagation levels with proper isolation.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
public class TransactionalTrafficLightService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionalTrafficLightService.class);
    
    private final TrafficLightRepository trafficLightRepository;
    private final StateHistoryRepository stateHistoryRepository;
    
    @Autowired
    public TransactionalTrafficLightService(TrafficLightRepository trafficLightRepository,
                                           StateHistoryRepository stateHistoryRepository) {
        this.trafficLightRepository = trafficLightRepository;
        this.stateHistoryRepository = stateHistoryRepository;
    }
    
    /**
     * REQUIRED propagation: Join existing transaction or create new one.
     * Default behavior - most common use case.
     */
    @Transactional(propagation = Propagation.REQUIRED, 
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public StateChangeResponse changeStateWithRequired(String lightUuid, StateChangeRequest request) {
        logger.info("Changing state with REQUIRED propagation for light: {}", lightUuid);
        
        TrafficLight light = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        // This will participate in the same transaction
        recordStateHistory(light, request);
        
        light.setCurrentState(request.getTargetState());
        light.setLastStateChange(LocalDateTime.now());
        TrafficLight updated = trafficLightRepository.save(light);
        
        return createResponse(updated, true, "State changed successfully");
    }
    
    /**
     * REQUIRES_NEW propagation: Always create a new transaction, suspend existing one.
     * Used for independent operations that should commit regardless of outer transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void recordStateHistoryIndependent(TrafficLight light, StateChangeRequest request) {
        logger.info("Recording state history with REQUIRES_NEW propagation for light: {}", light.getUuid());
        
        // This runs in its own transaction and will commit independently
        StateHistory history = StateHistory.builder()
            .uuid(UUID.randomUUID().toString())
            .trafficLightId(light.getUuid())
            .intersectionId(light.getIntersectionId())
            .direction(light.getDirection())
            .fromState(light.getCurrentState())
            .toState(request.getTargetState())
            .changedAt(LocalDateTime.now())
            .reason(request.getReason())
            .triggeredBy("SYSTEM")
            .build();
        
        stateHistoryRepository.save(history);
        logger.info("State history recorded independently");
    }
    
    /**
     * MANDATORY propagation: Must be called within an existing transaction.
     * Throws exception if no transaction exists.
     */
    @Transactional(propagation = Propagation.MANDATORY,
                   isolation = Isolation.READ_COMMITTED)
    public void updateLightStateMandatory(TrafficLight light, StateChangeRequest request) {
        logger.info("Updating light state with MANDATORY propagation");
        
        light.setCurrentState(request.getTargetState());
        light.setLastStateChange(LocalDateTime.now());
        trafficLightRepository.save(light);
    }
    
    /**
     * SUPPORTS propagation: Execute within transaction if one exists, otherwise non-transactional.
     */
    @Transactional(propagation = Propagation.SUPPORTS,
                   isolation = Isolation.READ_COMMITTED,
                   readOnly = true)
    public TrafficLight getTrafficLightWithSupports(String lightUuid) {
        logger.debug("Getting traffic light with SUPPORTS propagation");
        return trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
    }
    
    /**
     * NOT_SUPPORTED propagation: Execute non-transactionally, suspend existing transaction.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void logStateChangeNonTransactional(String lightUuid, String message) {
        logger.info("Non-transactional logging for light: {} - {}", lightUuid, message);
        // This executes outside any transaction
    }
    
    /**
     * NEVER propagation: Must NOT be called within a transaction.
     * Throws exception if transaction exists.
     */
    @Transactional(propagation = Propagation.NEVER)
    public void validateNonTransactional(String lightUuid) {
        logger.debug("Validation with NEVER propagation for light: {}", lightUuid);
        // This must execute outside any transaction
    }
    
    /**
     * NESTED propagation: Execute within nested transaction if supported.
     * Creates savepoint if outer transaction exists.
     */
    @Transactional(propagation = Propagation.NESTED,
                   isolation = Isolation.READ_COMMITTED)
    public void updateWithNested(TrafficLight light, StateChangeRequest request) {
        logger.info("Updating with NESTED propagation");
        
        light.setCurrentState(request.getTargetState());
        trafficLightRepository.save(light);
        // If this fails, only this nested transaction rolls back
    }
    
    /**
     * Handle optimistic locking failures with retry logic.
     */
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public StateChangeResponse changeStateWithOptimisticLockHandling(String lightUuid, StateChangeRequest request) {
        logger.info("Changing state with optimistic lock handling for light: {}", lightUuid);
        
        try {
            return RetryUtil.executeWithRetry(() -> {
                try {
                    return attemptStateChange(lightUuid, request);
                } catch (OptimisticLockingFailureException e) {
                    logger.warn("Optimistic locking failure for light: {}, retrying...", lightUuid);
                    throw e;
                }
            }, RetryUtil.config()
                .maxAttempts(5)
                .initialDelay(Duration.ofMillis(100))
                .multiplier(2.0)
                .retryOn(OptimisticLockingFailureException.class));
                        
        } catch (Exception e) {
            logger.error("Failed to change state after retries for light: {}", lightUuid, e);
            throw new ConcurrentModificationException("TrafficLight", lightUuid);
        }
    }
    
    /**
     * Attempt state change (can fail with optimistic locking exception).
     */
    private StateChangeResponse attemptStateChange(String lightUuid, StateChangeRequest request) {
        TrafficLight light = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        logger.debug("Attempting state change for light: {}, version: {}", lightUuid, light.getVersion());
        
        light.setCurrentState(request.getTargetState());
        light.setLastStateChange(LocalDateTime.now());
        
        // This will throw OptimisticLockingFailureException if version mismatch
        TrafficLight updated = trafficLightRepository.save(light);
        
        logger.info("State changed successfully for light: {}, new version: {}", 
                   lightUuid, updated.getVersion());
        
        return createResponse(updated, true, "State changed successfully");
    }
    
    /**
     * Demonstrate transaction isolation levels.
     */
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.SERIALIZABLE,
                   rollbackFor = Exception.class)
    public void updateWithSerializableIsolation(String lightUuid, StateChangeRequest request) {
        logger.info("Updating with SERIALIZABLE isolation for light: {}", lightUuid);
        
        TrafficLight light = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        light.setCurrentState(request.getTargetState());
        trafficLightRepository.save(light);
    }
    
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.REPEATABLE_READ,
                   rollbackFor = Exception.class)
    public void updateWithRepeatableReadIsolation(String lightUuid, StateChangeRequest request) {
        logger.info("Updating with REPEATABLE_READ isolation for light: {}", lightUuid);
        
        TrafficLight light = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        light.setCurrentState(request.getTargetState());
        trafficLightRepository.save(light);
    }
    
    /**
     * Batch update with proper transaction handling.
     */
    @Transactional(propagation = Propagation.REQUIRED,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void batchUpdateWithTransactionHandling(List<String> lightUuids, StateChangeRequest request) {
        logger.info("Batch updating {} lights with transaction handling", lightUuids.size());
        
        for (String lightUuid : lightUuids) {
            try {
                // Each update in independent transaction
                updateInIndependentTransaction(lightUuid, request);
            } catch (Exception e) {
                logger.error("Failed to update light: {}, continuing with others", lightUuid, e);
                // Continue with other lights even if one fails
            }
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW,
                   isolation = Isolation.READ_COMMITTED,
                   rollbackFor = Exception.class)
    public void updateInIndependentTransaction(String lightUuid, StateChangeRequest request) {
        TrafficLight light = trafficLightRepository.findByUuid(lightUuid)
            .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        
        light.setCurrentState(request.getTargetState());
        trafficLightRepository.save(light);
    }
    
    // Helper methods
    
    @Transactional(propagation = Propagation.REQUIRED)
    private void recordStateHistory(TrafficLight light, StateChangeRequest request) {
        StateHistory history = StateHistory.builder()
            .uuid(UUID.randomUUID().toString())
            .trafficLightId(light.getUuid())
            .intersectionId(light.getIntersectionId())
            .direction(light.getDirection())
            .fromState(light.getCurrentState())
            .toState(request.getTargetState())
            .changedAt(LocalDateTime.now())
            .reason(request.getReason())
            .triggeredBy("SYSTEM")
            .build();
        
        stateHistoryRepository.save(history);
    }
    
    private StateChangeResponse createResponse(TrafficLight light, boolean success, String message) {
        StateChangeResponse response = StateChangeResponse.builder()
            .trafficLightId(light.getUuid())
            .intersectionId(light.getIntersectionId())
            .fromState(light.getCurrentState())
            .toState(light.getCurrentState())
            .changedAt(LocalDateTime.now())
            .success(success)
            .message(message)
            .build();
        return response;
    }
}
