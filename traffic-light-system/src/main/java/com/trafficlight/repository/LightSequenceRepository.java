package com.trafficlight.repository;

import com.trafficlight.domain.LightSequence;
import com.trafficlight.enums.LightState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for LightSequence entity operations.
 * 
 * Provides CRUD operations and custom queries for light sequence management
 * with support for fetch joins and sequence ordering.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface LightSequenceRepository extends JpaRepository<LightSequence, Long> {
    
    /**
     * Find light sequence by UUID.
     * 
     * @param uuid the UUID to search for
     * @return optional light sequence
     */
    Optional<LightSequence> findByUuid(String uuid);
    
    /**
     * Find all sequences for a specific intersection ordered by sequence order.
     * 
     * @param intersectionId the intersection ID
     * @return list of light sequences in order
     */
    List<LightSequence> findByIntersectionIdOrderBySequenceOrder(String intersectionId);
    
    /**
     * Find active sequences for a specific intersection.
     * 
     * @param intersectionId the intersection ID
     * @param isActive active status
     * @return list of active light sequences in order
     */
    List<LightSequence> findByIntersectionIdAndIsActiveOrderBySequenceOrder(String intersectionId, Boolean isActive);
    
    /**
     * Find sequence by intersection and state.
     * 
     * @param intersectionId the intersection ID
     * @param state the light state
     * @return optional light sequence
     */
    Optional<LightSequence> findByIntersectionIdAndState(String intersectionId, LightState state);
    
    /**
     * Find sequence by intersection and sequence order.
     * 
     * @param intersectionId the intersection ID
     * @param sequenceOrder the sequence order
     * @return optional light sequence
     */
    Optional<LightSequence> findByIntersectionIdAndSequenceOrder(String intersectionId, Integer sequenceOrder);
    
    /**
     * Find sequences by state.
     * 
     * @param state the light state
     * @return list of light sequences
     */
    List<LightSequence> findByState(LightState state);
    
    /**
     * Find next sequence in the cycle.
     * 
     * @param intersectionId the intersection ID
     * @param currentOrder the current sequence order
     * @return optional next light sequence
     */
    @Query("SELECT ls FROM LightSequence ls WHERE ls.intersectionId = :intersectionId " +
           "AND ls.sequenceOrder > :currentOrder AND ls.isActive = true " +
           "ORDER BY ls.sequenceOrder ASC")
    Optional<LightSequence> findNextSequence(
        @Param("intersectionId") String intersectionId,
        @Param("currentOrder") Integer currentOrder
    );
    
    /**
     * Find first sequence in the cycle.
     * 
     * @param intersectionId the intersection ID
     * @return optional first light sequence
     */
    @Query("SELECT ls FROM LightSequence ls WHERE ls.intersectionId = :intersectionId " +
           "AND ls.isActive = true ORDER BY ls.sequenceOrder ASC")
    Optional<LightSequence> findFirstSequence(@Param("intersectionId") String intersectionId);
    
    /**
     * Find sequence with pessimistic lock for concurrent updates.
     * 
     * @param uuid the UUID
     * @return optional light sequence with lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ls FROM LightSequence ls WHERE ls.uuid = :uuid")
    Optional<LightSequence> findByUuidWithLock(@Param("uuid") String uuid);
    
    /**
     * Count sequences for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @return count of sequences
     */
    long countByIntersectionId(String intersectionId);
    
    /**
     * Count active sequences for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @param isActive active status
     * @return count of active sequences
     */
    long countByIntersectionIdAndIsActive(String intersectionId, Boolean isActive);
    
    /**
     * Find sequences with duration greater than specified value.
     * 
     * @param minDuration minimum duration in seconds
     * @return list of light sequences
     */
    @Query("SELECT ls FROM LightSequence ls WHERE ls.durationSeconds > :minDuration")
    List<LightSequence> findSequencesWithDurationGreaterThan(@Param("minDuration") Integer minDuration);
    
    /**
     * Get total cycle duration for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @return total duration in seconds
     */
    @Query("SELECT SUM(ls.durationSeconds) FROM LightSequence ls " +
           "WHERE ls.intersectionId = :intersectionId AND ls.isActive = true")
    Long getTotalCycleDuration(@Param("intersectionId") String intersectionId);
    
    /**
     * Find sequences by next sequence ID.
     * 
     * @param nextSequenceId the next sequence ID
     * @return list of light sequences that point to this sequence
     */
    List<LightSequence> findByNextSequenceId(String nextSequenceId);
    
    /**
     * Batch update sequence durations.
     * 
     * @param intersectionId the intersection ID
     * @param state the light state
     * @param newDuration the new duration
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE LightSequence ls SET ls.durationSeconds = :newDuration " +
           "WHERE ls.intersectionId = :intersectionId AND ls.state = :state")
    int batchUpdateDuration(
        @Param("intersectionId") String intersectionId,
        @Param("state") LightState state,
        @Param("newDuration") Integer newDuration
    );
    
    /**
     * Find sequences with broken next sequence references.
     * 
     * @return list of sequences with invalid next sequence IDs
     */
    @Query("SELECT ls FROM LightSequence ls WHERE ls.nextSequenceId IS NOT NULL " +
           "AND ls.nextSequenceId NOT IN (SELECT ls2.uuid FROM LightSequence ls2)")
    List<LightSequence> findSequencesWithBrokenReferences();
    
    /**
     * Find sequences that form cycles (detect infinite loops).
     * 
     * @param intersectionId the intersection ID
     * @return list of sequences that may form cycles
     */
    @Query(value = "WITH RECURSIVE sequence_path AS ( " +
                   "  SELECT uuid, next_sequence_id, intersection_id, sequence_order, " +
                   "         CAST(uuid AS CHAR(1000)) as path, 1 as depth " +
                   "  FROM light_sequences " +
                   "  WHERE intersection_id = :intersectionId AND is_active = true " +
                   "  UNION ALL " +
                   "  SELECT ls.uuid, ls.next_sequence_id, ls.intersection_id, ls.sequence_order, " +
                   "         CONCAT(sp.path, '->', ls.uuid), sp.depth + 1 " +
                   "  FROM light_sequences ls " +
                   "  JOIN sequence_path sp ON ls.uuid = sp.next_sequence_id " +
                   "  WHERE sp.depth < 10 AND FIND_IN_SET(ls.uuid, sp.path) = 0 " +
                   ") " +
                   "SELECT DISTINCT uuid FROM sequence_path " +
                   "WHERE depth > 1",
           nativeQuery = true)
    List<String> findSequenceCycles(@Param("intersectionId") String intersectionId);
    
    /**
     * Delete sequences by intersection ID.
     * 
     * @param intersectionId the intersection ID
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM LightSequence ls WHERE ls.intersectionId = :intersectionId")
    int deleteByIntersectionId(@Param("intersectionId") String intersectionId);
    
    /**
     * Check if sequence order exists for intersection.
     * 
     * @param intersectionId the intersection ID
     * @param sequenceOrder the sequence order
     * @return true if exists, false otherwise
     */
    boolean existsByIntersectionIdAndSequenceOrder(String intersectionId, Integer sequenceOrder);
    
    /**
     * Find sequences with description containing text.
     * 
     * @param descriptionText the text to search for
     * @return list of matching light sequences
     */
    @Query("SELECT ls FROM LightSequence ls WHERE ls.description LIKE %:descriptionText%")
    List<LightSequence> findByDescriptionContaining(@Param("descriptionText") String descriptionText);
    
    /**
     * Get sequence statistics for an intersection.
     * 
     * @param intersectionId the intersection ID
     * @return array with [total, active, avgDuration, maxDuration, minDuration]
     */
    @Query("SELECT COUNT(ls), " +
           "SUM(CASE WHEN ls.isActive = true THEN 1 ELSE 0 END), " +
           "AVG(ls.durationSeconds), " +
           "MAX(ls.durationSeconds), " +
           "MIN(ls.durationSeconds) " +
           "FROM LightSequence ls WHERE ls.intersectionId = :intersectionId")
    Object[] getSequenceStatistics(@Param("intersectionId") String intersectionId);
}