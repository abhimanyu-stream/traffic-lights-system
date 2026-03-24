package com.trafficlight.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Value object representing geographic coordinates.
 * Immutable and validates coordinate ranges.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Embeddable
public class Coordinates {
    
    private double latitude;
    private double longitude;
    
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    
    // JPA requires default constructor
    protected Coordinates() {}
    
    private Coordinates(double latitude, double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public static Coordinates of(double latitude, double longitude) {
        return new Coordinates(latitude, longitude);
    }
    
    private void validateLatitude(double latitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException(
                String.format("Latitude must be between %.1f and %.1f, got: %.6f", 
                    MIN_LATITUDE, MAX_LATITUDE, latitude));
        }
    }
    
    private void validateLongitude(double longitude) {
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException(
                String.format("Longitude must be between %.1f and %.1f, got: %.6f", 
                    MIN_LONGITUDE, MAX_LONGITUDE, longitude));
        }
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Calculate distance to another coordinate in kilometers using Haversine formula.
     */
    public double distanceTo(Coordinates other) {
        final double EARTH_RADIUS_KM = 6371.0;
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.latitude, latitude) == 0 &&
               Double.compare(that.longitude, longitude) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
    
    @Override
    public String toString() {
        return String.format("Coordinates{lat=%.6f, lon=%.6f}", latitude, longitude);
    }
}