package com.trafficlight.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom metrics for Traffic Light system.
 * Tracks state changes, API calls, and performance metrics.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class TrafficLightMetrics {
    
    private final Counter stateChangeCounter;
    private final Counter intersectionCreatedCounter;
    private final Counter validationErrorCounter;
    private final Timer stateChangeTimer;
    private final Timer apiRequestTimer;
    
    public TrafficLightMetrics(MeterRegistry meterRegistry) {
        this.stateChangeCounter = Counter.builder("traffic.light.state.changes")
            .description("Total number of traffic light state changes")
            .tag("type", "state_change")
            .register(meterRegistry);
            
        this.intersectionCreatedCounter = Counter.builder("traffic.light.intersections.created")
            .description("Total number of intersections created")
            .tag("type", "intersection")
            .register(meterRegistry);
            
        this.validationErrorCounter = Counter.builder("traffic.light.validation.errors")
            .description("Total number of validation errors")
            .tag("type", "validation")
            .register(meterRegistry);
            
        this.stateChangeTimer = Timer.builder("traffic.light.state.change.duration")
            .description("Time taken to change traffic light state")
            .tag("operation", "state_change")
            .register(meterRegistry);
            
        this.apiRequestTimer = Timer.builder("traffic.light.api.request.duration")
            .description("Time taken to process API requests")
            .tag("operation", "api_request")
            .register(meterRegistry);
    }
    
    public void recordStateChange() {
        stateChangeCounter.increment();
    }
    
    public void recordIntersectionCreated() {
        intersectionCreatedCounter.increment();
    }
    
    public void recordValidationError() {
        validationErrorCounter.increment();
    }
    
    public void recordStateChangeDuration(long durationMs) {
        stateChangeTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordApiRequestDuration(long durationMs) {
        apiRequestTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
}
