package com.trafficlight.repository;

import com.trafficlight.domain.Intersection;
import com.trafficlight.enums.IntersectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Repository interface for Intersection entity operations.
 * 
 * Provides CRUD operations and custom queries for intersection management
 * with support for pagination and complex queries.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface IntersectionRepository extends JpaRepository<Intersection, Long> {
    
    /**
     * Find intersection by UUID.
     * 
     * @param uuid the UUID to search for
     * @return optional intersection
     */
    Optional<Intersection> findByUuid(String uuid);
    
    /**
     * Find intersection by name.
     * 
     * @param name the intersection name
     * @return optional intersection
     */
    Optional<Intersection> findByName(String name);
    
    /**
     * Find intersections by status.
     * 
     * @param status the intersection status
     * @return list of intersections
     */
    List<Intersection> findByStatus(IntersectionStatus status);
    
    /**
     * Find active intersections.
     * 
     * @param isActive active status
     * @return list of active intersections
     */
    List<Intersection> findByIsActive(Boolean isActive);
    
    /**
     * Find intersections by status and active flag.
     * 
     * @param status the intersection status
     * @param isActive active status
     * @return list of intersections
     */
    List<Intersection> findByStatusAndIsActive(IntersectionStatus status, Boolean isActive);
    
    /**
     * Find intersections within geographic bounds.
     * 
     * @param minLat minimum latitude
     * @param maxLat maximum latitude
     * @param minLng minimum longitude
     * @param maxLng maximum longitude
     * @return list of intersections within bounds
     */
    @Query("SELECT i FROM Intersection i WHERE i.coordinates.latitude BETWEEN :minLat AND :maxLat " +
           "AND i.coordinates.longitude BETWEEN :minLng AND :maxLng AND i.isActive = true")
    List<Intersection> findIntersectionsWithinBounds(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng
    );
    
    /**
     * Find intersections by name containing (case-insensitive search).
     * 
     * @param namePattern the name pattern to search for
     * @param pageable pagination information
     * @return page of intersections
     */
    @Query("SELECT i FROM Intersection i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    Page<Intersection> findByNameContainingIgnoreCase(@Param("namePattern") String namePattern, Pageable pageable);
    
    /**
     * Find intersection with pessimistic lock for concurrent updates.
     * 
     * @param uuid the UUID
     * @return optional intersection with lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Intersection i WHERE i.uuid = :uuid")
    Optional<Intersection> findByUuidWithLock(@Param("uuid") String uuid);
    
    /**
     * Count intersections by status.
     * 
     * @param status the intersection status
     * @return count of intersections
     */
    long countByStatus(IntersectionStatus status);
    
    /**
     * Count active intersections.
     * 
     * @param isActive active status
     * @return count of active intersections
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * Find intersections with traffic light count.
     * 
     * @return list of intersections with their traffic light counts
     */
    @Query("SELECT i, COUNT(tl) FROM Intersection i LEFT JOIN TrafficLight tl ON i.uuid = tl.intersectionId " +
           "WHERE i.isActive = true GROUP BY i")
    List<Object[]> findIntersectionsWithTrafficLightCount();
    
    /**
     * Check if intersection name exists.
     * 
     * @param name the intersection name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find intersections by status and active flag.
     * 
     * @param status the intersection status
     * @return list of intersections
     */
    List<Intersection> findByStatusAndIsActiveTrue(IntersectionStatus status);
    
    /**
     * Find intersections by name pattern (case insensitive) and active flag.
     * 
     * @param namePattern the name pattern
     * @return list of intersections
     */
    List<Intersection> findByNameContainingIgnoreCaseAndIsActiveTrue(String namePattern);
    
    /**
     * Find operational intersections (active and operational status).
     * 
     * @return list of operational intersections
     */
    @Query("SELECT i FROM Intersection i WHERE i.isActive = true AND i.status = 'ACTIVE'")
    List<Intersection> findOperationalIntersections();
    
    /**
     * Batch update intersection status.
     * 
     * @param uuids list of intersection UUIDs
     * @param newStatus the new status
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE Intersection i SET i.status = :newStatus WHERE i.uuid IN :uuids")
    int batchUpdateStatus(@Param("uuids") List<String> uuids, @Param("newStatus") IntersectionStatus newStatus);
    
    /**
     * Find intersections near a specific location.
     * Uses Haversine formula approximation for distance calculation.
     * 
     * @param latitude the reference latitude
     * @param longitude the reference longitude
     * @param radiusKm the search radius in kilometers
     * @return list of nearby intersections
     */
    @Query(value = "SELECT i.* " +
                   "FROM intersections i " +
                   "WHERE i.is_active = true " +
                   "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(i.latitude)) * " +
                   "cos(radians(i.longitude) - radians(:longitude)) + " +
                   "sin(radians(:latitude)) * sin(radians(i.latitude)))) < :radiusKm " +
                   "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(i.latitude)) * " +
                   "cos(radians(i.longitude) - radians(:longitude)) + " +
                   "sin(radians(:latitude)) * sin(radians(i.latitude))))",
           nativeQuery = true)
    List<Intersection> findNearbyIntersections(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm
    );
    
    /**
     * Check if intersection name exists (case-insensitive).
     * 
     * @param name the intersection name
     * @return true if exists, false otherwise
     */
    @Query("SELECT COUNT(i) > 0 FROM Intersection i WHERE LOWER(i.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);
    
    /**
     * Find intersections with no traffic lights.
     * 
     * @return list of intersections without traffic lights
     */
    @Query("SELECT i FROM Intersection i WHERE i.uuid NOT IN " +
           "(SELECT DISTINCT tl.intersectionId FROM TrafficLight tl WHERE tl.isActive = true)")
    List<Intersection> findIntersectionsWithoutTrafficLights();
    
    /**
     * Get intersection statistics.
     * 
     * @return array with [total, active, operational, maintenance, error]
     */
    @Query(value = "SELECT " +
                   "COUNT(*) as total, " +
                   "SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active, " +
                   "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as operational, " +
                   "SUM(CASE WHEN status = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance, " +
                   "SUM(CASE WHEN status = 'ERROR' THEN 1 ELSE 0 END) as error " +
                   "FROM intersections",
           nativeQuery = true)
    Object[] getIntersectionStatistics();
}