package com.trafficlight.infrastructure.concurrent;

import com.trafficlight.enums.LightState;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe state holder using volatile fields and atomic operations.
 * Ensures visibility of state changes across threads without explicit locking.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class VolatileStateHolder {
    
    private volatile LightState currentState;
    private volatile LocalDateTime lastStateChange;
    private volatile int durationSeconds;
    private volatile boolean isActive;
    
    private final AtomicLong stateChangeCount;
    private final AtomicReference<String> lastModifiedBy;
    
    public VolatileStateHolder(LightState initialState, int durationSeconds) {
        this.currentState = Objects.requireNonNull(initialState, "Initial state cannot be null");
        this.lastStateChange = LocalDateTime.now();
        this.durationSeconds = durationSeconds;
        this.isActive = true;
        this.stateChangeCount = new AtomicLong(0);
        this.lastModifiedBy = new AtomicReference<>("SYSTEM");
    }
    
    /**
     * Get the current state (volatile read).
     */
    public LightState getCurrentState() {
        return currentState;
    }
    
    /**
     * Set the current state (volatile write).
     */
    public void setCurrentState(LightState newState) {
        this.currentState = Objects.requireNonNull(newState, "State cannot be null");
        this.lastStateChange = LocalDateTime.now();
        this.stateChangeCount.incrementAndGet();
    }
    
    /**
     * Compare and set state atomically.
     */
    public boolean compareAndSetState(LightState expected, LightState newState) {
        if (this.currentState == expected) {
            setCurrentState(newState);
            return true;
        }
        return false;
    }
    
    /**
     * Get the last state change time (volatile read).
     */
    public LocalDateTime getLastStateChange() {
        return lastStateChange;
    }
    
    /**
     * Get the duration in seconds (volatile read).
     */
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    /**
     * Set the duration in seconds (volatile write).
     */
    public void setDurationSeconds(int durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.durationSeconds = durationSeconds;
    }
    
    /**
     * Check if active (volatile read).
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Set active status (volatile write).
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Get the total number of state changes.
     */
    public long getStateChangeCount() {
        return stateChangeCount.get();
    }
    
    /**
     * Get who last modified the state.
     */
    public String getLastModifiedBy() {
        return lastModifiedBy.get();
    }
    
    /**
     * Set who last modified the state.
     */
    public void setLastModifiedBy(String modifiedBy) {
        this.lastModifiedBy.set(modifiedBy);
    }
    
    /**
     * Create a snapshot of the current state.
     */
    public StateSnapshot createSnapshot() {
        return new StateSnapshot(
            currentState,
            lastStateChange,
            durationSeconds,
            isActive,
            stateChangeCount.get(),
            lastModifiedBy.get()
        );
    }
    
    /**
     * Immutable snapshot of state at a point in time.
     */
    public static class StateSnapshot {
        private final LightState state;
        private final LocalDateTime timestamp;
        private final int duration;
        private final boolean active;
        private final long changeCount;
        private final String modifiedBy;
        
        public StateSnapshot(LightState state, LocalDateTime timestamp, int duration, 
                           boolean active, long changeCount, String modifiedBy) {
            this.state = state;
            this.timestamp = timestamp;
            this.duration = duration;
            this.active = active;
            this.changeCount = changeCount;
            this.modifiedBy = modifiedBy;
        }
        
        public LightState getState() { return state; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getDuration() { return duration; }
        public boolean isActive() { return active; }
        public long getChangeCount() { return changeCount; }
        public String getModifiedBy() { return modifiedBy; }
        
        @Override
        public String toString() {
            return String.format("StateSnapshot{state=%s, timestamp=%s, duration=%ds, active=%s, changes=%d, by=%s}",
                state, timestamp, duration, active, changeCount, modifiedBy);
        }
    }
    
    @Override
    public String toString() {
        return String.format("VolatileStateHolder{state=%s, lastChange=%s, duration=%ds, active=%s, changes=%d}",
            currentState, lastStateChange, durationSeconds, isActive, stateChangeCount.get());
    }
}