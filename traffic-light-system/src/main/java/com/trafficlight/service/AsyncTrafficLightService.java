package com.trafficlight.service;

import com.trafficlight.domain.TrafficLight;
import com.trafficlight.dto.request.StateChangeRequest;
import com.trafficlight.dto.response.StateChangeResponse;
import com.trafficlight.enums.LightState;
import com.trafficlight.exception.TrafficLightNotFoundException;
import com.trafficlight.repository.TrafficLightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Async service for traffic light operations using CompletableFuture.
 * Demonstrates async processing patterns with proper error handling.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
public class AsyncTrafficLightService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncTrafficLightService.class);
    
    private final TrafficLightRepository trafficLightRepository;
    private final TrafficLightService trafficLightService;
    private final Executor trafficLightExecutor;
    
    @Autowired
    public AsyncTrafficLightService(TrafficLightRepository trafficLightRepository,
                                   TrafficLightService trafficLightService,
                                   @Qualifier("trafficLightExecutor") Executor trafficLightExecutor) {
        this.trafficLightRepository = trafficLightRepository;
        this.trafficLightService = trafficLightService;
        this.trafficLightExecutor = trafficLightExecutor;
    }
    
    /**
     * Asynchronously change traffic light state.
     */
    @Async("trafficLightExecutor")
    public CompletableFuture<StateChangeResponse> changeStateAsync(String lightUuid, StateChangeRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Async state change started for light: {}", lightUuid);
            try {
                StateChangeResponse response = trafficLightService.changeState(lightUuid, request);
                logger.info("Async state change completed for light: {}", lightUuid);
                return response;
            } catch (Exception e) {
                logger.error("Async state change failed for light: {}", lightUuid, e);
                throw new RuntimeException("Async state change failed", e);
            }
        }, trafficLightExecutor);
    }
    
    /**
     * Asynchronously get traffic light by UUID.
     */
    @Async("trafficLightExecutor")
    public CompletableFuture<TrafficLight> getTrafficLightAsync(String lightUuid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Async fetch started for light: {}", lightUuid);
            return trafficLightRepository.findByUuid(lightUuid)
                .orElseThrow(() -> new TrafficLightNotFoundException(lightUuid));
        }, trafficLightExecutor);
    }
    
    /**
     * Asynchronously get all traffic lights for an intersection.
     */
    @Async("trafficLightExecutor")
    public CompletableFuture<List<TrafficLight>> getTrafficLightsByIntersectionAsync(String intersectionId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Async fetch started for intersection: {}", intersectionId);
            return trafficLightRepository.findByIntersectionId(intersectionId);
        }, trafficLightExecutor);
    }
    
    /**
     * Asynchronously change multiple traffic lights in parallel.
     */
    public CompletableFuture<List<StateChangeResponse>> bulkStateChangeAsync(
            List<String> lightUuids, LightState targetState) {
        
        logger.info("Starting bulk async state change for {} lights to {}", lightUuids.size(), targetState);
        
        List<CompletableFuture<StateChangeResponse>> futures = lightUuids.stream()
            .map(lightUuid -> changeStateAsync(lightUuid, createStateChangeRequest(targetState)))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    /**
     * Chain multiple async operations with error handling.
     */
    public CompletableFuture<StateChangeResponse> changeStateWithValidation(
            String lightUuid, StateChangeRequest request) {
        
        return getTrafficLightAsync(lightUuid)
            .thenCompose(light -> {
                // Validate state transition
                if (!isValidTransition(light.getCurrentState(), request.getTargetState())) {
                    return CompletableFuture.failedFuture(
                        new IllegalStateException("Invalid state transition"));
                }
                return changeStateAsync(lightUuid, request);
            })
            .exceptionally(ex -> {
                logger.error("State change with validation failed for light: {}", lightUuid, ex);
                throw new RuntimeException("State change failed", ex);
            });
    }
    
    /**
     * Combine multiple async operations.
     */
    public CompletableFuture<IntersectionStateReport> getIntersectionStateReportAsync(String intersectionId) {
        CompletableFuture<List<TrafficLight>> lightsFuture = getTrafficLightsByIntersectionAsync(intersectionId);
        CompletableFuture<Long> countFuture = CompletableFuture.supplyAsync(
            () -> (long) trafficLightRepository.findByIntersectionId(intersectionId).size(),
            trafficLightExecutor
        );
        
        return lightsFuture.thenCombine(countFuture, (lights, count) -> {
            long greenCount = lights.stream()
                .filter(l -> l.getCurrentState() == LightState.GREEN)
                .count();
            long redCount = lights.stream()
                .filter(l -> l.getCurrentState() == LightState.RED)
                .count();
            long yellowCount = lights.stream()
                .filter(l -> l.getCurrentState() == LightState.YELLOW)
                .count();
            
            return new IntersectionStateReport(intersectionId, count, greenCount, redCount, yellowCount);
        });
    }
    
    /**
     * Async operation with timeout and fallback.
     */
    public CompletableFuture<StateChangeResponse> changeStateWithTimeout(
            String lightUuid, StateChangeRequest request, long timeoutMillis) {
        
        CompletableFuture<StateChangeResponse> future = changeStateAsync(lightUuid, request);
        
        return future.orTimeout(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            .exceptionally(ex -> {
                logger.warn("State change timed out for light: {}, using fallback", lightUuid);
                return createFallbackResponse(lightUuid);
            });
    }
    
    /**
     * Process lights in batches asynchronously.
     */
    public CompletableFuture<Void> processBatchAsync(List<String> lightUuids, int batchSize) {
        List<CompletableFuture<Void>> batchFutures = new java.util.ArrayList<>();
        
        for (int i = 0; i < lightUuids.size(); i += batchSize) {
            int end = Math.min(i + batchSize, lightUuids.size());
            List<String> batch = lightUuids.subList(i, end);
            
            CompletableFuture<Void> batchFuture = CompletableFuture.runAsync(() -> {
                logger.info("Processing batch of {} lights", batch.size());
                batch.forEach(uuid -> {
                    try {
                        trafficLightRepository.findByUuid(uuid);
                    } catch (Exception e) {
                        logger.error("Error processing light: {}", uuid, e);
                    }
                });
            }, trafficLightExecutor);
            
            batchFutures.add(batchFuture);
        }
        
        return CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0]));
    }
    
    // Helper methods
    
    private StateChangeRequest createStateChangeRequest(LightState targetState) {
        StateChangeRequest request = new StateChangeRequest();
        request.setTargetState(targetState);
        request.setReason("Bulk state change");
        return request;
    }
    
    private boolean isValidTransition(LightState current, LightState target) {
        // Simplified validation - can be enhanced
        return current != target;
    }
    
    private StateChangeResponse createFallbackResponse(String lightUuid) {
        return StateChangeResponse.builder()
            .trafficLightId(lightUuid)
            .success(false)
            .message("Operation timed out")
            .changedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Report class for intersection state.
     */
    public static class IntersectionStateReport {
        private final String intersectionId;
        private final Long totalLights;
        private final Long greenLights;
        private final Long redLights;
        private final Long yellowLights;
        
        public IntersectionStateReport(String intersectionId, Long totalLights, 
                                      Long greenLights, Long redLights, Long yellowLights) {
            this.intersectionId = intersectionId;
            this.totalLights = totalLights;
            this.greenLights = greenLights;
            this.redLights = redLights;
            this.yellowLights = yellowLights;
        }
        
        public String getIntersectionId() { return intersectionId; }
        public Long getTotalLights() { return totalLights; }
        public Long getGreenLights() { return greenLights; }
        public Long getRedLights() { return redLights; }
        public Long getYellowLights() { return yellowLights; }
    }
}
