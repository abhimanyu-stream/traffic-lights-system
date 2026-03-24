package com.trafficlight.service;

import com.trafficlight.repository.TrafficLightRepository;
import com.trafficlight.repository.IntersectionRepository;
import com.trafficlight.repository.StateHistoryRepository;
import com.trafficlight.repository.SystemEventRepository;
import com.trafficlight.utils.PerformanceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for traffic analytics operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
@Transactional(readOnly = true)
public class TrafficAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrafficAnalyticsService.class);
    
    private final TrafficLightRepository trafficLightRepository;
    private final IntersectionRepository intersectionRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final SystemEventRepository systemEventRepository;
    private final PerformanceLogger performanceLogger;
    
    @Autowired
    public TrafficAnalyticsService(TrafficLightRepository trafficLightRepository,
                                  IntersectionRepository intersectionRepository,
                                  StateHistoryRepository stateHistoryRepository,
                                  SystemEventRepository systemEventRepository,
                                  PerformanceLogger performanceLogger) {
        this.trafficLightRepository = trafficLightRepository;
        this.intersectionRepository = intersectionRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.systemEventRepository = systemEventRepository;
        this.performanceLogger = performanceLogger;
    }
    
    /**
     * Get system-wide analytics.
     */
    public Object getSystemAnalytics() {
        return performanceLogger.logExecutionTime("getSystemAnalytics", () -> {
            logger.info("Calculating system-wide analytics");
            
            long totalIntersectionsCount = intersectionRepository.count();
            long activeIntersectionsCount = intersectionRepository.countByIsActive(true);
            long totalTrafficLightsCount = trafficLightRepository.count();
            long activeTrafficLightsCount = trafficLightRepository.countByIsActive(true);
            
            LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
            long totalStateChangesCount = stateHistoryRepository.countByChangedAtAfter(dayAgo);
            
            return new Object() {
                public final String message = "System analytics data";
                public final long totalIntersections = totalIntersectionsCount;
                public final long totalTrafficLights = totalTrafficLightsCount;
                public final long activeIntersections = activeIntersectionsCount;
                public final long activeTrafficLights = activeTrafficLightsCount;
                public final long totalStateChanges = totalStateChangesCount;
                public final LocalDateTime lastUpdated = LocalDateTime.now();
                public final String period = "Last 24 hours";
            };
        });
    }
    
    /**
     * Get analytics for a specific intersection.
     */
    public Object getIntersectionAnalytics(String intersectionId) {
        return performanceLogger.logExecutionTime("getIntersectionAnalytics", () -> {
            logger.info("Calculating analytics for intersection: {}", intersectionId);
            
            long trafficLightCountValue = trafficLightRepository.countByIntersectionIdAndIsActiveTrue(intersectionId);
            
            LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
            long totalStateChangesValue = stateHistoryRepository.countByIntersectionIdAndChangedAtAfter(intersectionId, dayAgo);
            
            // Calculate average cycle time based on actual data
            double averageCycleTimeValue = totalStateChangesValue > 0 ? (double) totalStateChangesValue / (trafficLightCountValue > 0 ? trafficLightCountValue : 1) * 20.0 : 0.0;
            long totalCyclesValue = totalStateChangesValue / 3; // Assuming 3 states per cycle
            
            LocalDateTime lastStateChangeValue = stateHistoryRepository.findTopByIntersectionIdOrderByChangedAtDesc(intersectionId)
                .map(history -> history.getChangedAt())
                .orElse(null);
            
            return new Object() {
                public final String id = intersectionId;
                public final String message = "Intersection analytics data";
                public final long trafficLightCount = trafficLightCountValue;
                public final long totalStateChanges = totalStateChangesValue;
                public final double averageCycleTime = averageCycleTimeValue;
                public final long totalCycles = totalCyclesValue;
                public final LocalDateTime lastStateChange = lastStateChangeValue;
                public final String period = "Last 24 hours";
                public final LocalDateTime calculatedAt = LocalDateTime.now();
            };
        });
    }
    
    /**
     * Get analytics for a specific traffic light.
     */
    public Object getTrafficLightAnalytics(String lightId) {
        return performanceLogger.logExecutionTime("getTrafficLightAnalytics", () -> {
            logger.info("Calculating analytics for traffic light: {}", lightId);
            
            LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
            
            long totalStateChangesValue = stateHistoryRepository.countByTrafficLightIdAndChangedAtAfter(lightId, dayAgo);
            long redStateCountValue = stateHistoryRepository.countByTrafficLightIdAndToStateAndChangedAtAfter(lightId, com.trafficlight.enums.LightState.RED, dayAgo);
            long yellowStateCountValue = stateHistoryRepository.countByTrafficLightIdAndToStateAndChangedAtAfter(lightId, com.trafficlight.enums.LightState.YELLOW, dayAgo);
            long greenStateCountValue = stateHistoryRepository.countByTrafficLightIdAndToStateAndChangedAtAfter(lightId, com.trafficlight.enums.LightState.GREEN, dayAgo);
            
            // Calculate average durations from actual data
            LocalDateTime now = LocalDateTime.now();
            Double avgRedDuration = stateHistoryRepository.getAverageStateDuration(lightId, com.trafficlight.enums.LightState.RED, dayAgo, now);
            Double avgYellowDuration = stateHistoryRepository.getAverageStateDuration(lightId, com.trafficlight.enums.LightState.YELLOW, dayAgo, now);
            Double avgGreenDuration = stateHistoryRepository.getAverageStateDuration(lightId, com.trafficlight.enums.LightState.GREEN, dayAgo, now);
            
            double averageRedDurationValue = avgRedDuration != null ? avgRedDuration : 0.0;
            double averageYellowDurationValue = avgYellowDuration != null ? avgYellowDuration : 0.0;
            double averageGreenDurationValue = avgGreenDuration != null ? avgGreenDuration : 0.0;
            
            LocalDateTime lastStateChangeValue = stateHistoryRepository.findTopByTrafficLightIdOrderByChangedAtDesc(lightId)
                .map(history -> history.getChangedAt())
                .orElse(null);
            
            return new Object() {
                public final String id = lightId;
                public final String message = "Traffic light analytics data";
                public final long totalStateChanges = totalStateChangesValue;
                public final long redStateCount = redStateCountValue;
                public final long yellowStateCount = yellowStateCountValue;
                public final long greenStateCount = greenStateCountValue;
                public final double averageRedDuration = averageRedDurationValue;
                public final double averageYellowDuration = averageYellowDurationValue;
                public final double averageGreenDuration = averageGreenDurationValue;
                public final LocalDateTime lastStateChange = lastStateChangeValue;
                public final String period = "Last 24 hours";
                public final LocalDateTime calculatedAt = LocalDateTime.now();
            };
        });
    }
    
    /**
     * Get performance analytics.
     */
    public Object getPerformanceAnalytics() {
        return performanceLogger.logExecutionTime("getPerformanceAnalytics", () -> {
            logger.info("Calculating system performance analytics");
            
            // Get performance stats from PerformanceLogger
            PerformanceLogger.OperationStats changeStateStats = performanceLogger.getStats("changeState");
            PerformanceLogger.OperationStats getAllStats = performanceLogger.getStats("getAllTrafficLights");
            PerformanceLogger.OperationStats getByIdStats = performanceLogger.getStats("getTrafficLightById");
            
            long totalRequestsValue = 0;
            long successfulRequestsValue = 0;
            long failedRequestsValue = 0;
            double averageResponseTimeValue = 0.0;
            double maxResponseTimeValue = 0.0;
            double minResponseTimeValue = 0.0;
            
            if (changeStateStats != null) {
                totalRequestsValue += changeStateStats.getTotalCalls();
                successfulRequestsValue += changeStateStats.getSuccessfulCalls();
                failedRequestsValue += changeStateStats.getFailedCalls();
                averageResponseTimeValue = changeStateStats.getAverageDuration();
                maxResponseTimeValue = Math.max(maxResponseTimeValue, changeStateStats.getMaxDuration());
                minResponseTimeValue = changeStateStats.getMinDuration();
            }
            
            if (getAllStats != null) {
                totalRequestsValue += getAllStats.getTotalCalls();
                successfulRequestsValue += getAllStats.getSuccessfulCalls();
                failedRequestsValue += getAllStats.getFailedCalls();
                maxResponseTimeValue = Math.max(maxResponseTimeValue, getAllStats.getMaxDuration());
                minResponseTimeValue = Math.min(minResponseTimeValue, getAllStats.getMinDuration());
            }
            
            if (getByIdStats != null) {
                totalRequestsValue += getByIdStats.getTotalCalls();
                successfulRequestsValue += getByIdStats.getSuccessfulCalls();
                failedRequestsValue += getByIdStats.getFailedCalls();
                maxResponseTimeValue = Math.max(maxResponseTimeValue, getByIdStats.getMaxDuration());
                minResponseTimeValue = Math.min(minResponseTimeValue, getByIdStats.getMinDuration());
            }
            
            double successRateValue = totalRequestsValue > 0 ? (double) successfulRequestsValue / totalRequestsValue * 100.0 : 100.0;
            
            // Make variables effectively final for anonymous object
            final double finalAverageResponseTime = averageResponseTimeValue;
            final double finalMaxResponseTime = maxResponseTimeValue;
            final double finalMinResponseTime = minResponseTimeValue;
            final long finalTotalRequests = totalRequestsValue;
            final long finalSuccessfulRequests = successfulRequestsValue;
            final long finalFailedRequests = failedRequestsValue;
            final double finalSuccessRate = successRateValue;
            
            return new Object() {
                public final String message = "Performance analytics data";
                public final double averageResponseTime = finalAverageResponseTime;
                public final double maxResponseTime = finalMaxResponseTime;
                public final double minResponseTime = finalMinResponseTime;
                public final long totalRequests = finalTotalRequests;
                public final long successfulRequests = finalSuccessfulRequests;
                public final long failedRequests = finalFailedRequests;
                public final double successRate = finalSuccessRate;
                public final LocalDateTime calculatedAt = LocalDateTime.now();
            };
        });
    }
    
    /**
     * Get dashboard data.
     */
    public Object getDashboardData() {
        return performanceLogger.logExecutionTime("getDashboardData", () -> {
            logger.info("Calculating dashboard analytics");
            
            long activeIntersectionsValue = intersectionRepository.countByIsActive(true);
            long activeTrafficLightsValue = trafficLightRepository.countByIsActive(true);
            
            LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
            long recentStateChangesValue = stateHistoryRepository.countByChangedAtAfter(hourAgo);
            
            long systemAlertsValue = systemEventRepository.countByIsResolvedFalse();
            
            // Get performance stats
            PerformanceLogger.OperationStats stats = performanceLogger.getStats("changeState");
            double responseTimeValue = stats != null ? stats.getAverageDuration() : 0.0;
            
            double systemHealthValue = systemAlertsValue == 0 ? 100.0 : Math.max(0, 100.0 - (systemAlertsValue * 5));
            String systemStatusValue = systemAlertsValue == 0 ? "OPERATIONAL" : systemAlertsValue < 5 ? "WARNING" : "CRITICAL";
            long currentLoadValue = Math.round(responseTimeValue / 10); // Mock calculation
            
            return new Object() {
                public final String message = "Dashboard analytics data";
                public final LocalDateTime timestamp = LocalDateTime.now();
                public final long activeIntersections = activeIntersectionsValue;
                public final long activeTrafficLights = activeTrafficLightsValue;
                public final long recentStateChanges = recentStateChangesValue;
                public final long systemAlerts = systemAlertsValue;
                public final double systemHealth = systemHealthValue;
                public final String systemStatus = systemStatusValue;
                public final long currentLoad = currentLoadValue;
                public final double responseTime = responseTimeValue;
                public final String period = "Last hour";
            };
        });
    }
}