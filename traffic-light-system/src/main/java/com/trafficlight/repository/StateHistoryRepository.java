package com.trafficlight.repository;

import com.trafficlight.domain.StateHistory;
import com.trafficlight.enums.LightState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StateHistory entity operations.
 * 
 * Provides CRUD operations and custom queries for state history management
 * with support for pagination and analytics queries.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface StateHistoryRepository extends JpaRepository<StateHistory, Long> {
    
    /**
     * Find state history by UUID.
     * 
     * @param uuid the UUID to search for
     * @return optional state history
     */
    Optional<StateHistory> findByUuid(String uuid);
    
    /**
     * Find state history for a specific traffic light.
     * 
     * @param trafficLightId the traffic light ID
     * @param pageable pagination information
     * @return page of state history records
     */
    Page<StateHistory> findByTrafficLightId(String trafficLightId, Pageable pageable);
    
    /**
     * Find state history for a specific intersection.
     * 
     * @param intersectionId the intersection ID
     * @param pageable pagination information
     * @return page of state history records
     */
    Page<StateHistory> findByIntersectionId(String intersectionId, Pageable pageable);
    
    /**
     * Find state history by state transition.
     * 
     * @param fromState the from state
     * @param toState the to state
     * @param pageable pagination information
     * @return page of state history records
     */
    Page<StateHistory> findByFromStateAndToState(LightState fromState, LightState toState, Pageable pageable);
    
    /**
     * Find state history within time range.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination information
     * @return page of state history records
     */
    Page<StateHistory> findByChangedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Find state history for traffic light within time range.
     * 
     * @param trafficLightId the traffic light ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of state history records
     */
    List<StateHistory> findByTrafficLightIdAndChangedAtBetween(
        String trafficLightId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    /**
     * Find recent state changes for a traffic light.
     * 
     * @param trafficLightId the traffic light ID
     * @param limit the maximum number of records
     * @return list of recent state history records
     */
    @Query("SELECT sh FROM StateHistory sh WHERE sh.trafficLightId = :trafficLightId " +
           "ORDER BY sh.changedAt DESC")
    List<StateHistory> findRecentStateChanges(@Param("trafficLightId") String trafficLightId, Pageable pageable);
    
    /**
     * Count state transitions by type.
     * 
     * @param fromState the from state
     * @param toState the to state
     * @param startTime the start time
     * @param endTime the end time
     * @return count of transitions
     */
    @Query("SELECT COUNT(sh) FROM StateHistory sh WHERE sh.fromState = :fromState " +
           "AND sh.toState = :toState AND sh.changedAt BETWEEN :startTime AND :endTime")
    long countTransitions(
        @Param("fromState") LightState fromState,
        @Param("toState") LightState toState,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get state transition statistics for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of transition statistics [fromState, toState, count]
     */
    @Query("SELECT sh.fromState, sh.toState, COUNT(sh) FROM StateHistory sh " +
           "WHERE sh.intersectionId = :intersectionId " +
           "AND sh.changedAt BETWEEN :startTime AND :endTime " +
           "GROUP BY sh.fromState, sh.toState " +
           "ORDER BY COUNT(sh) DESC")
    List<Object[]> getTransitionStatistics(
        @Param("intersectionId") String intersectionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find invalid state transitions.
     * 
     * @return list of invalid state history records
     */
    @Query("SELECT sh FROM StateHistory sh WHERE " +
           "(sh.fromState = 'RED' AND sh.toState = 'GREEN') OR " +
           "(sh.fromState = 'GREEN' AND sh.toState = 'RED') OR " +
           "(sh.fromState = 'YELLOW' AND sh.toState = 'YELLOW')")
    List<StateHistory> findInvalidTransitions();
    
    /**
     * Get average state duration by state type.
     * 
     * @param intersectionId the intersection ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of [state, avgDuration] pairs
     */
    @Query("SELECT sh.toState, AVG(sh.durationSeconds) FROM StateHistory sh " +
           "WHERE sh.intersectionId = :intersectionId " +
           "AND sh.changedAt BETWEEN :startTime AND :endTime " +
           "AND sh.durationSeconds IS NOT NULL " +
           "GROUP BY sh.toState")
    List<Object[]> getAverageStateDurations(
        @Param("intersectionId") String intersectionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find state history by correlation ID.
     * 
     * @param correlationId the correlation ID
     * @return list of related state history records
     */
    List<StateHistory> findByCorrelationId(String correlationId);
    
    /**
     * Find state history by triggered by.
     * 
     * @param triggeredBy who triggered the change
     * @param pageable pagination information
     * @return page of state history records
     */
    Page<StateHistory> findByTriggeredBy(String triggeredBy, Pageable pageable);
    
    /**
     * Delete old state history records.
     * 
     * @param cutoffDate the cutoff date
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM StateHistory sh WHERE sh.changedAt < :cutoffDate")
    int deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete state history records before a specific date.
     * 
     * @param cutoffDate the cutoff date
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM StateHistory sh WHERE sh.changedAt < :cutoffDate")
    int deleteByChangedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get hourly state change counts for analytics.
     * 
     * @param intersectionId the intersection ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of [hour, count] pairs
     */
    @Query(value = "SELECT HOUR(changed_at) as hour, COUNT(*) as count " +
                   "FROM state_history " +
                   "WHERE intersection_id = :intersectionId " +
                   "AND changed_at BETWEEN :startTime AND :endTime " +
                   "GROUP BY HOUR(changed_at) " +
                   "ORDER BY hour",
           nativeQuery = true)
    List<Object[]> getHourlyStateChangeCounts(
        @Param("intersectionId") String intersectionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find longest state durations.
     * 
     * @param intersectionId the intersection ID
     * @param limit the maximum number of records
     * @return list of state history records with longest durations
     */
    @Query("SELECT sh FROM StateHistory sh WHERE sh.intersectionId = :intersectionId " +
           "AND sh.durationSeconds IS NOT NULL " +
           "ORDER BY sh.durationSeconds DESC")
    List<StateHistory> findLongestStateDurations(@Param("intersectionId") String intersectionId, Pageable pageable);
    
    /**
     * Count total state changes for a traffic light.
     * 
     * @param trafficLightId the traffic light ID
     * @return total count of state changes
     */
    long countByTrafficLightId(String trafficLightId);
    
    /**
     * Count state changes after a specific date.
     * 
     * @param changedAt the date after which to count changes
     * @return count of state changes
     */
    long countByChangedAtAfter(LocalDateTime changedAt);
    
    /**
     * Count state changes for a specific intersection after a date.
     * 
     * @param intersectionId the intersection ID
     * @param changedAt the date after which to count changes
     * @return count of state changes
     */
    long countByIntersectionIdAndChangedAtAfter(String intersectionId, LocalDateTime changedAt);
    
    /**
     * Count state changes for a specific traffic light after a date.
     * 
     * @param trafficLightId the traffic light ID
     * @param changedAt the date after which to count changes
     * @return count of state changes
     */
    long countByTrafficLightIdAndChangedAtAfter(String trafficLightId, LocalDateTime changedAt);
    
    /**
     * Count state changes by state for a traffic light after a date.
     * 
     * @param trafficLightId the traffic light ID
     * @param toState the target state
     * @param changedAt the date after which to count changes
     * @return count of state changes to specific state
     */
    long countByTrafficLightIdAndToStateAndChangedAtAfter(String trafficLightId, LightState toState, LocalDateTime changedAt);
    
    /**
     * Get average duration for a specific state and traffic light.
     * 
     * @param trafficLightId the traffic light ID
     * @param toState the target state
     * @param startTime the start time
     * @param endTime the end time
     * @return average duration in seconds
     */
    @Query("SELECT AVG(sh.durationSeconds) FROM StateHistory sh " +
           "WHERE sh.trafficLightId = :trafficLightId " +
           "AND sh.toState = :toState " +
           "AND sh.changedAt BETWEEN :startTime AND :endTime " +
           "AND sh.durationSeconds IS NOT NULL")
    Double getAverageStateDuration(
        @Param("trafficLightId") String trafficLightId,
        @Param("toState") LightState toState,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find the most recent state change for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @return optional most recent state history record
     */
    Optional<StateHistory> findTopByIntersectionIdOrderByChangedAtDesc(String intersectionId);
    
    /**
     * Find the most recent state change for a traffic light by ID.
     * 
     * @param trafficLightId the traffic light ID
     * @return optional most recent state history record
     */
    Optional<StateHistory> findTopByTrafficLightIdOrderByChangedAtDesc(String trafficLightId);
    
    /**
     * Find state history with reason containing text.
     * 
     * @param reasonText the text to search for in reason
     * @param pageable pagination information
     * @return page of matching state history records
     */
    @Query("SELECT sh FROM StateHistory sh WHERE sh.reason LIKE %:reasonText%")
    Page<StateHistory> findByReasonContaining(@Param("reasonText") String reasonText, Pageable pageable);
}