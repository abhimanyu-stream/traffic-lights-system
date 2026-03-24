package com.trafficlight.exception;

/**
 * Exception thrown when a concurrent modification conflict occurs.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class ConcurrentModificationException extends TrafficLightException {
    
    public ConcurrentModificationException(String resourceType, String resourceId) {
        super("CONCURRENT_MODIFICATION", 
              "Concurrent modification detected for " + resourceType + ": " + resourceId,
              resourceType, resourceId);
    }
    
    public ConcurrentModificationException(String resourceType, String resourceId, Long expectedVersion, Long actualVersion) {
        super("CONCURRENT_MODIFICATION", 
              "Concurrent modification detected for " + resourceType + " " + resourceId + 
              ". Expected version: " + expectedVersion + ", actual version: " + actualVersion,
              resourceType, resourceId, expectedVersion, actualVersion);
    }
}