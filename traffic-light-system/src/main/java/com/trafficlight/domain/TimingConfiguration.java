package com.trafficlight.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.Objects;

/**
 * Value object representing timing configuration for traffic lights.
 * Immutable and validates timing constraints.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Embeddable
public class TimingConfiguration {
    
    @Column(name = "red_duration_seconds")
    private int redDurationSeconds;
    
    @Column(name = "yellow_duration_seconds")
    private int yellowDurationSeconds;
    
    @Column(name = "green_duration_seconds")
    private int greenDurationSeconds;
    
    private static final int MIN_RED_DURATION = 10;
    private static final int MAX_RED_DURATION = 120;
    private static final int MIN_YELLOW_DURATION = 3;
    private static final int MAX_YELLOW_DURATION = 10;
    private static final int MIN_GREEN_DURATION = 10;
    private static final int MAX_GREEN_DURATION = 120;
    
    // JPA requires default constructor
    protected TimingConfiguration() {}
    
    private TimingConfiguration(int redDurationSeconds, int yellowDurationSeconds, int greenDurationSeconds) {
        validateDuration("Red", redDurationSeconds, MIN_RED_DURATION, MAX_RED_DURATION);
        validateDuration("Yellow", yellowDurationSeconds, MIN_YELLOW_DURATION, MAX_YELLOW_DURATION);
        validateDuration("Green", greenDurationSeconds, MIN_GREEN_DURATION, MAX_GREEN_DURATION);
        
        this.redDurationSeconds = redDurationSeconds;
        this.yellowDurationSeconds = yellowDurationSeconds;
        this.greenDurationSeconds = greenDurationSeconds;
    }
    
    public static TimingConfiguration of(int redDurationSeconds, int yellowDurationSeconds, int greenDurationSeconds) {
        return new TimingConfiguration(redDurationSeconds, yellowDurationSeconds, greenDurationSeconds);
    }
    
    public static TimingConfiguration defaultConfiguration() {
        return new TimingConfiguration(30, 5, 25);
    }
    
    private void validateDuration(String lightType, int duration, int min, int max) {
        if (duration < min || duration > max) {
            throw new IllegalArgumentException(
                String.format("%s light duration must be between %d and %d seconds, got: %d", 
                    lightType, min, max, duration));
        }
    }
    
    public int getRedDurationSeconds() {
        return redDurationSeconds;
    }
    
    public int getYellowDurationSeconds() {
        return yellowDurationSeconds;
    }
    
    public int getGreenDurationSeconds() {
        return greenDurationSeconds;
    }
    
    public int getTotalCycleDuration() {
        return redDurationSeconds + yellowDurationSeconds + greenDurationSeconds;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimingConfiguration that = (TimingConfiguration) o;
        return redDurationSeconds == that.redDurationSeconds &&
               yellowDurationSeconds == that.yellowDurationSeconds &&
               greenDurationSeconds == that.greenDurationSeconds;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(redDurationSeconds, yellowDurationSeconds, greenDurationSeconds);
    }
    
    @Override
    public String toString() {
        return String.format("TimingConfiguration{red=%ds, yellow=%ds, green=%ds, total=%ds}",
            redDurationSeconds, yellowDurationSeconds, greenDurationSeconds, getTotalCycleDuration());
    }
}