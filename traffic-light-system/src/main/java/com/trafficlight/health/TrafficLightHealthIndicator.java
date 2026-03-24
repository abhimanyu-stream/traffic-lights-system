package com.trafficlight.health;

import com.trafficlight.repository.IntersectionRepository;
import com.trafficlight.repository.TrafficLightRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Traffic Light system.
 * Checks the health of intersections and traffic lights.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class TrafficLightHealthIndicator implements HealthIndicator {
    
    private final IntersectionRepository intersectionRepository;
    private final TrafficLightRepository trafficLightRepository;
    
    public TrafficLightHealthIndicator(IntersectionRepository intersectionRepository,
                                      TrafficLightRepository trafficLightRepository) {
        this.intersectionRepository = intersectionRepository;
        this.trafficLightRepository = trafficLightRepository;
    }
    
    @Override
    public Health health() {
        try {
            long intersectionCount = intersectionRepository.count();
            long trafficLightCount = trafficLightRepository.count();
            long activeIntersections = intersectionRepository.countByStatus(
                com.trafficlight.enums.IntersectionStatus.ACTIVE);
            
            if (intersectionCount == 0) {
                return Health.down()
                    .withDetail("reason", "No intersections configured")
                    .build();
            }
            
            return Health.up()
                .withDetail("totalIntersections", intersectionCount)
                .withDetail("activeIntersections", activeIntersections)
                .withDetail("totalTrafficLights", trafficLightCount)
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
