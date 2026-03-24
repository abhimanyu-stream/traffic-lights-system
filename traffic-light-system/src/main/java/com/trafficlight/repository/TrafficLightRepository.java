package com.trafficlight.repository;

import com.trafficlight.domain.TrafficLight;
import com.trafficlight.enums.Direction;
import com.trafficlight.enums.LightState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TrafficLight entity operations.
 * 
 * Provides CRUD operations and custom queries for traffic light management
 * with support for optimistic and pessimistic locking.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface TrafficLightRepository extends JpaRepository<TrafficLight, Long> {
    
    /**
     * Find traffic light by UUID.
     * 
     * @param uuid the UUID to search for
     * @return optional traffic light
     */
    Optional<TrafficLight> findByUuid(String uuid);
    
    /**
     * Find all traffic lights for a specific intersection.
     * 
     * @param intersectionId the intersection ID
     * @return list of traffic lights
     */
    List<TrafficLight> findByIntersectionId(String intersectionId);
    
    /**
     * Find active traffic lights for a specific intersection.
     * 
     * @param intersectionId the intersection ID
     * @param isActive active status
     * @return list of active traffic lights
     */
    List<TrafficLight> findByIntersectionIdAndIsActive(String intersectionId, Boolean isActive);
    
    /**
     * Count active traffic lights by intersection ID.
     * 
     * @param intersectionId the intersection ID
     * @return count of active traffic lights
     */
    long countByIntersectionIdAndIsActiveTrue(String intersectionId);
    
    /**
     * Find traffic light by intersection and direction.
     * 
     * @param intersectionId the intersection ID
     * @param direction the direction
     * @return optional traffic light
     */
    Optional<TrafficLight> findByIntersectionIdAndDirection(String intersectionId, Direction direction);
    
    /**
     * Find traffic lights by current state.
     * 
     * @param currentState the current state
     * @return list of traffic lights
     */
    List<TrafficLight> findByCurrentState(LightState currentState);
    
    /**
     * Find traffic lights by intersection and current state.
     * 
     * @param intersectionId the intersection ID
     * @param currentState the current state
     * @return list of traffic lights
     */
    List<TrafficLight> findByIntersectionIdAndCurrentState(String intersectionId, LightState currentState);
    
    /**
     * Find traffic lights by intersection ID and active status.
     * 
     * @param intersectionId the intersection ID
     * @return list of active traffic lights
     */
    List<TrafficLight> findByIntersectionIdAndIsActiveTrue(String intersectionId);
    
    /**
     * Find traffic lights that have exceeded their state duration.
     * 
     * @param cutoffTime the cutoff time for state changes
     * @return list of traffic lights that need state change
     */
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.isActive = true AND " +
           "tl.lastStateChange < :cutoffTime")
    List<TrafficLight> findByLastStateChangeBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find conflicting traffic lights (same intersection, different directions).
     * 
     * @param intersectionId the intersection ID
     * @param excludeDirection the direction to exclude
     * @param state the state to check for conflicts
     * @return list of conflicting traffic lights
     */
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.intersectionId = :intersectionId AND " +
           "tl.direction != :excludeDirection AND tl.currentState = :state AND tl.isActive = true")
    List<TrafficLight> findConflictingTrafficLights(
        @Param("intersectionId") String intersectionId,
        @Param("excludeDirection") Direction excludeDirection,
        @Param("state") LightState state
    );
    
    /**
     * Find traffic light with pessimistic lock for concurrent updates.
     * 
     * @param uuid the UUID
     * @return optional traffic light with lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.uuid = :uuid")
    Optional<TrafficLight> findByUuidWithLock(@Param("uuid") String uuid);
    
    /**
     * Batch update traffic light states for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @param newState the new state
     * @param updateTime the update timestamp
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE TrafficLight tl SET tl.currentState = :newState, " +
           "tl.lastStateChange = :updateTime WHERE tl.intersectionId = :intersectionId " +
           "AND tl.isActive = true")
    int batchUpdateStateByIntersection(
        @Param("intersectionId") String intersectionId,
        @Param("newState") LightState newState,
        @Param("updateTime") LocalDateTime updateTime
    );
    
    /**
     * Count active traffic lights by intersection.
     * 
     * @param intersectionId the intersection ID
     * @return count of active traffic lights
     */
    @Query("SELECT COUNT(tl) FROM TrafficLight tl WHERE tl.intersectionId = :intersectionId " +
           "AND tl.isActive = true")
    long countActiveByIntersection(@Param("intersectionId") String intersectionId);
    
    /**
     * Find traffic lights by state and last change time range.
     * 
     * @param state the traffic light state
     * @param startTime the start time
     * @param endTime the end time
     * @return list of traffic lights
     */
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.currentState = :state AND " +
           "tl.lastStateChange BETWEEN :startTime AND :endTime")
    List<TrafficLight> findByStateAndTimeRange(
        @Param("state") LightState state,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Check if traffic light exists by intersection and direction.
     * 
     * @param intersectionId the intersection ID
     * @param direction the direction
     * @return true if exists, false otherwise
     */
    boolean existsByIntersectionIdAndDirection(String intersectionId, Direction direction);
    
    /**
     * Delete traffic lights by intersection ID.
     * 
     * @param intersectionId the intersection ID
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM TrafficLight tl WHERE tl.intersectionId = :intersectionId")
    int deleteByIntersectionId(@Param("intersectionId") String intersectionId);
    
    /**
     * Count traffic lights by active status.
     * 
     * @param isActive the active status
     * @return count of traffic lights
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * Find all traffic lights with their intersection details.
     * Custom native query for performance optimization.
     * 
     * @param intersectionId the intersection ID
     * @return list of traffic lights with intersection data
     */
    @Query(value = "SELECT tl.*, i.name as intersection_name FROM traffic_lights tl " +
                   "JOIN intersections i ON tl.intersection_id = i.uuid " +
                   "WHERE tl.intersection_id = :intersectionId AND tl.is_active = true",
           nativeQuery = true)
    List<Object[]> findTrafficLightsWithIntersectionDetails(@Param("intersectionId") String intersectionId);
}