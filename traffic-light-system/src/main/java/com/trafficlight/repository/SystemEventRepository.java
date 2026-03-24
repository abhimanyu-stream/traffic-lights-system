package com.trafficlight.repository;

import com.trafficlight.domain.SystemEvent;
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
 * Repository interface for SystemEvent entity operations.
 * 
 * Provides CRUD operations and custom queries for system event tracking
 * with support for monitoring and analytics queries.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface SystemEventRepository extends JpaRepository<SystemEvent, Long> {
    
    /**
     * Find system event by UUID.
     * 
     * @param uuid the UUID to search for
     * @return optional system event
     */
    Optional<SystemEvent> findByUuid(String uuid);
    
    /**
     * Find events by type.
     * 
     * @param eventType the event type
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findByEventType(String eventType, Pageable pageable);
    
    /**
     * Find events by level.
     * 
     * @param level the event level (INFO, WARN, ERROR, DEBUG)
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findByLevel(String level, Pageable pageable);
    
    /**
     * Find events by source.
     * 
     * @param source the event source
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findBySource(String source, Pageable pageable);
    
    /**
     * Find events for a specific intersection.
     * 
     * @param intersectionId the intersection ID
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findByIntersectionId(String intersectionId, Pageable pageable);
    
    /**
     * Find events for a specific traffic light.
     * 
     * @param trafficLightId the traffic light ID
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findByTrafficLightId(String trafficLightId, Pageable pageable);
    
    /**
     * Find events by correlation ID.
     * 
     * @param correlationId the correlation ID
     * @return list of related system events
     */
    List<SystemEvent> findByCorrelationId(String correlationId);
    
    /**
     * Find events within time range.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findByOccurredAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Find unresolved events.
     * 
     * @param isResolved resolved status
     * @param pageable pagination information
     * @return page of unresolved system events
     */
    Page<SystemEvent> findByIsResolved(Boolean isResolved, Pageable pageable);
    
    /**
     * Find error events within time range.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of error events
     */
    @Query("SELECT se FROM SystemEvent se WHERE se.level = 'ERROR' " +
           "AND se.occurredAt BETWEEN :startTime AND :endTime " +
           "ORDER BY se.occurredAt DESC")
    List<SystemEvent> findErrorEvents(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find recent events by level.
     * 
     * @param level the event level
     * @param hours the number of hours to look back
     * @return list of recent events
     */
    @Query("SELECT se FROM SystemEvent se WHERE se.level = :level " +
           "AND se.occurredAt >= :cutoffTime ORDER BY se.occurredAt DESC")
    List<SystemEvent> findRecentEventsByLevel(
        @Param("level") String level,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );
    
    /**
     * Count events by type within time range.
     * 
     * @param eventType the event type
     * @param startTime the start time
     * @param endTime the end time
     * @return count of events
     */
    @Query("SELECT COUNT(se) FROM SystemEvent se WHERE se.eventType = :eventType " +
           "AND se.occurredAt BETWEEN :startTime AND :endTime")
    long countEventsByType(
        @Param("eventType") String eventType,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get event statistics by level.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of [level, count] pairs
     */
    @Query("SELECT se.level, COUNT(se) FROM SystemEvent se " +
           "WHERE se.occurredAt BETWEEN :startTime AND :endTime " +
           "GROUP BY se.level ORDER BY COUNT(se) DESC")
    List<Object[]> getEventStatisticsByLevel(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Get event statistics by type.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of [eventType, count] pairs
     */
    @Query("SELECT se.eventType, COUNT(se) FROM SystemEvent se " +
           "WHERE se.occurredAt BETWEEN :startTime AND :endTime " +
           "GROUP BY se.eventType ORDER BY COUNT(se) DESC")
    List<Object[]> getEventStatisticsByType(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find events by message containing text.
     * 
     * @param messageText the text to search for
     * @param pageable pagination information
     * @return page of matching system events
     */
    @Query("SELECT se FROM SystemEvent se WHERE se.message LIKE %:messageText%")
    Page<SystemEvent> findByMessageContaining(@Param("messageText") String messageText, Pageable pageable);
    
    /**
     * Find events by user ID.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find events by session ID.
     * 
     * @param sessionId the session ID
     * @param pageable pagination information
     * @return page of system events
     */
    Page<SystemEvent> findBySessionId(String sessionId, Pageable pageable);
    
    /**
     * Mark events as resolved by correlation ID.
     * 
     * @param correlationId the correlation ID
     * @param resolvedAt the resolution timestamp
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE SystemEvent se SET se.isResolved = true, se.resolvedAt = :resolvedAt " +
           "WHERE se.correlationId = :correlationId AND se.isResolved = false")
    int markEventsAsResolvedByCorrelationId(
        @Param("correlationId") String correlationId,
        @Param("resolvedAt") LocalDateTime resolvedAt
    );
    
    /**
     * Delete old resolved events.
     * 
     * @param cutoffDate the cutoff date
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM SystemEvent se WHERE se.isResolved = true AND se.resolvedAt < :cutoffDate")
    int deleteOldResolvedEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get hourly event counts for analytics.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of [hour, count] pairs
     */
    @Query(value = "SELECT HOUR(occurred_at) as hour, COUNT(*) as count " +
                   "FROM system_events " +
                   "WHERE occurred_at BETWEEN :startTime AND :endTime " +
                   "GROUP BY HOUR(occurred_at) " +
                   "ORDER BY hour",
           nativeQuery = true)
    List<Object[]> getHourlyEventCounts(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Find top error sources.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @param limit the maximum number of sources
     * @return list of [source, errorCount] pairs
     */
    @Query("SELECT se.source, COUNT(se) FROM SystemEvent se " +
           "WHERE se.level = 'ERROR' AND se.occurredAt BETWEEN :startTime AND :endTime " +
           "GROUP BY se.source ORDER BY COUNT(se) DESC")
    List<Object[]> getTopErrorSources(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    /**
     * Count unresolved events by level.
     * 
     * @param level the event level
     * @return count of unresolved events
     */
    @Query("SELECT COUNT(se) FROM SystemEvent se WHERE se.level = :level AND se.isResolved = false")
    long countUnresolvedEventsByLevel(@Param("level") String level);
    
    /**
     * Count unresolved events (system alerts).
     * 
     * @return count of unresolved events
     */
    long countByIsResolvedFalse();
    
    /**
     * Count events after a specific time.
     * 
     * @param occurredAt the time after which to count events
     * @return count of events
     */
    long countByOccurredAtAfter(LocalDateTime occurredAt);
    
    /**
     * Find events with details containing text.
     * 
     * @param detailsText the text to search for in details
     * @param pageable pagination information
     * @return page of matching system events
     */
    @Query("SELECT se FROM SystemEvent se WHERE se.details LIKE %:detailsText%")
    Page<SystemEvent> findByDetailsContaining(@Param("detailsText") String detailsText, Pageable pageable);
    
    /**
     * Get system health summary.
     * 
     * @param hours the number of hours to look back
     * @return array with [totalEvents, errorCount, warningCount, unresolvedCount]
     */
    @Query(value = "SELECT " +
                   "COUNT(*) as total_events, " +
                   "SUM(CASE WHEN level = 'ERROR' THEN 1 ELSE 0 END) as error_count, " +
                   "SUM(CASE WHEN level = 'WARN' THEN 1 ELSE 0 END) as warning_count, " +
                   "SUM(CASE WHEN is_resolved = false THEN 1 ELSE 0 END) as unresolved_count " +
                   "FROM system_events " +
                   "WHERE occurred_at >= :cutoffTime",
           nativeQuery = true)
    Object[] getSystemHealthSummary(@Param("cutoffTime") LocalDateTime cutoffTime);
}