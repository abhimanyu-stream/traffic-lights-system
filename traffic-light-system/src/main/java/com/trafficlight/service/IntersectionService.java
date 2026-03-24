package com.trafficlight.service;

import com.trafficlight.domain.Intersection;
import com.trafficlight.domain.Coordinates;
import com.trafficlight.dto.request.CreateIntersectionRequest;
import com.trafficlight.dto.request.UpdateIntersectionRequest;
import com.trafficlight.dto.response.IntersectionResponse;
import com.trafficlight.enums.IntersectionStatus;
import com.trafficlight.events.IntersectionUpdatedEvent;
import com.trafficlight.repository.IntersectionRepository;
import com.trafficlight.exception.IntersectionNotFoundException;
import com.trafficlight.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing intersection operations with validation logic.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
@Transactional
public class IntersectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(IntersectionService.class);
    
    private final IntersectionRepository intersectionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    public IntersectionService(IntersectionRepository intersectionRepository,
                              KafkaTemplate<String, Object> kafkaTemplate) {
        this.intersectionRepository = intersectionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Get all intersections with pagination.
     */
    @Transactional(readOnly = true)
    public Page<IntersectionResponse> getAllIntersections(Pageable pageable) {
        logger.info("Getting all intersections with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Intersection> intersections = intersectionRepository.findAll(pageable);
        return intersections.map(this::convertToResponse);
    }
    
    /**
     * Get intersection by UUID.
     */
    @Transactional(readOnly = true)
    public IntersectionResponse getIntersectionById(String intersectionUuid) {
        logger.info("Getting intersection by UUID: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        return convertToResponse(intersection);
    }
    
    /**
     * Create a new intersection with validation.
     */
    public IntersectionResponse createIntersection(CreateIntersectionRequest request) {
        logger.info("Creating new intersection: {}", request.getName());
        
        // Validate intersection name uniqueness
        if (intersectionRepository.existsByName(request.getName())) {
            throw new ValidationException("Intersection name already exists: " + request.getName(), 
                List.of("Name '" + request.getName() + "' is already in use"));
        }
        
        // Create coordinates with built-in validation
        Coordinates coordinates = null;
        if (request.getLatitude() != null && request.getLongitude() != null) {
            coordinates = Coordinates.of(request.getLatitude(), request.getLongitude());
        }
        
        Intersection intersection = Intersection.builder()
            .uuid(UUID.randomUUID().toString())
            .name(request.getName())
            .description(request.getDescription())
            .coordinates(coordinates)
            .status(IntersectionStatus.ACTIVE)
            .isActive(true)
            .build();
        
        Intersection saved = intersectionRepository.save(intersection);
        
        // Publish intersection created event
        publishIntersectionEvent(saved, "CREATED");
        
        logger.info("Created intersection with UUID: {}", saved.getUuid());
        return convertToResponse(saved);
    }
    
    /**
     * Update intersection with validation.
     */
    public IntersectionResponse updateIntersection(String intersectionUuid, UpdateIntersectionRequest request) {
        logger.info("Updating intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        // Validate name uniqueness if changed
        if (!intersection.getName().equals(request.getName()) && 
            intersectionRepository.existsByName(request.getName())) {
            throw new ValidationException("Intersection name already exists: " + request.getName(),
                List.of("Name '" + request.getName() + "' is already in use"));
        }
        
        // Update fields
        intersection.setName(request.getName());
        intersection.setDescription(request.getDescription());
        
        // Update coordinates if provided (with built-in validation)
        if (request.getLatitude() != null && request.getLongitude() != null) {
            intersection.setCoordinates(request.getLatitude(), request.getLongitude());
        }
        intersection.setUpdatedAt(LocalDateTime.now());
        
        Intersection updated = intersectionRepository.save(intersection);
        
        // Publish intersection updated event
        publishIntersectionEvent(updated, "UPDATED");
        
        logger.info("Updated intersection: {}", intersectionUuid);
        return convertToResponse(updated);
    }
    
    /**
     * Delete intersection (soft delete).
     */
    public void deleteIntersection(String intersectionUuid) {
        logger.info("Deleting intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        intersection.setActive(false);
        intersection.setStatus(IntersectionStatus.INACTIVE);
        intersection.setUpdatedAt(LocalDateTime.now());
        
        intersectionRepository.save(intersection);
        
        // Publish intersection deleted event
        publishIntersectionEvent(intersection, "DELETED");
        
        logger.info("Deleted intersection: {}", intersectionUuid);
    }
    
    /**
     * Update intersection status.
     */
    public IntersectionResponse updateIntersectionStatus(String intersectionUuid, IntersectionStatus status) {
        logger.info("Updating intersection status: {} to {}", intersectionUuid, status);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        IntersectionStatus oldStatus = intersection.getStatus();
        intersection.setStatus(status);
        intersection.setUpdatedAt(LocalDateTime.now());
        
        // Update active flag based on status
        intersection.setActive(status == IntersectionStatus.ACTIVE);
        
        Intersection updated = intersectionRepository.save(intersection);
        
        // Publish status change event
        publishIntersectionStatusEvent(updated, oldStatus, status);
        
        logger.info("Updated intersection status: {} from {} to {}", intersectionUuid, oldStatus, status);
        return convertToResponse(updated);
    }
    
    /**
     * Find nearby intersections within a radius.
     */
    @Transactional(readOnly = true)
    public List<IntersectionResponse> findNearbyIntersections(Double latitude, Double longitude, Double radiusKm) {
        logger.info("Finding intersections near coordinates: {}, {} within {} km", latitude, longitude, radiusKm);
        
        // Validate and set default radius if needed
        final double finalRadiusKm = (radiusKm == null || radiusKm <= 0) ? 5.0 : radiusKm;
        
        // Create coordinates with built-in validation
        final Coordinates centerCoordinates = Coordinates.of(latitude, longitude);
        
        // Get all active intersections and filter by distance
        List<Intersection> allIntersections = intersectionRepository.findByIsActive(true);
        
        List<Intersection> nearbyIntersections = allIntersections.stream()
            .filter(intersection -> intersection.isWithinRadius(centerCoordinates, finalRadiusKm))
            .collect(Collectors.toList());
        
        return nearbyIntersections.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get intersections by status.
     */
    @Transactional(readOnly = true)
    public List<IntersectionResponse> getIntersectionsByStatus(IntersectionStatus status) {
        logger.info("Getting intersections by status: {}", status);
        
        List<Intersection> intersections = intersectionRepository.findByStatusAndIsActiveTrue(status);
        return intersections.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Search intersections by name pattern.
     */
    @Transactional(readOnly = true)
    public List<IntersectionResponse> searchIntersectionsByName(String namePattern) {
        logger.info("Searching intersections by name pattern: {}", namePattern);
        
        List<Intersection> intersections = intersectionRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(namePattern);
        return intersections.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get intersection statistics.
     */
    @Transactional(readOnly = true)
    public Object getIntersectionStatistics(String intersectionUuid) {
        logger.info("Getting statistics for intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        // TODO: Implement statistics calculation
        // This would typically include traffic light counts, state change frequencies, etc.
        
        return "Statistics for intersection: " + intersection.getName();
    }
    
    /**
     * Publish intersection event to Kafka.
     */
    private void publishIntersectionEvent(Intersection intersection, String eventType) {
        try {
            IntersectionUpdatedEvent event = IntersectionUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .intersectionId(intersection.getUuid())
                .name(intersection.getName())
                .updateType(eventType)
                .status(intersection.getStatus())
                .latitude(intersection.getLatitude())
                .longitude(intersection.getLongitude())
                .updatedAt(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .build();
            
            kafkaTemplate.send("intersection-updated", intersection.getUuid(), event);
            logger.debug("Published intersection event: {} for {}", eventType, intersection.getUuid());
        } catch (Exception e) {
            logger.error("Failed to publish intersection event: {} for {}", eventType, intersection.getUuid(), e);
        }
    }
    
    /**
     * Publish intersection status change event.
     */
    private void publishIntersectionStatusEvent(Intersection intersection, IntersectionStatus oldStatus, IntersectionStatus newStatus) {
        try {
            IntersectionUpdatedEvent event = IntersectionUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .intersectionId(intersection.getUuid())
                .name(intersection.getName())
                .updateType("STATUS_CHANGED")
                .status(newStatus)
                .previousStatus(oldStatus)
                .latitude(intersection.getLatitude())
                .longitude(intersection.getLongitude())
                .updatedAt(LocalDateTime.now())
                .correlationId(UUID.randomUUID().toString())
                .build();
            
            kafkaTemplate.send("intersection-updated", intersection.getUuid(), event);
            logger.debug("Published intersection status change event: {} -> {} for {}", 
                        oldStatus, newStatus, intersection.getUuid());
        } catch (Exception e) {
            logger.error("Failed to publish intersection status change event for {}", intersection.getUuid(), e);
        }
    }
    
    /**
     * Convert Intersection entity to response DTO.
     */
    private IntersectionResponse convertToResponse(Intersection intersection) {
        return IntersectionResponse.builder()
            .id(intersection.getId())
            .uuid(intersection.getUuid())
            .name(intersection.getName())
            .description(intersection.getDescription())
            .latitude(intersection.getLatitude())
            .longitude(intersection.getLongitude())
            .status(intersection.getStatus())
            .isActive(intersection.isActive())
            .createdAt(intersection.getCreatedAt())
            .updatedAt(intersection.getUpdatedAt())
            .build();
    }
    
    /**
     * Pause intersection operation.
     */
    public IntersectionResponse pauseIntersection(String intersectionUuid) {
        logger.info("Pausing intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        IntersectionStatus oldStatus = intersection.getStatus();
        intersection.setStatus(IntersectionStatus.MAINTENANCE);
        intersection.setUpdatedAt(LocalDateTime.now());
        
        Intersection updated = intersectionRepository.save(intersection);
        
        // Publish pause event
        publishIntersectionStatusEvent(updated, oldStatus, IntersectionStatus.MAINTENANCE);
        
        logger.info("Paused intersection: {}", intersectionUuid);
        return convertToResponse(updated);
    }
    
    /**
     * Resume intersection operation.
     */
    public IntersectionResponse resumeIntersection(String intersectionUuid) {
        logger.info("Resuming intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        IntersectionStatus oldStatus = intersection.getStatus();
        intersection.setStatus(IntersectionStatus.ACTIVE);
        intersection.setActive(true);
        intersection.setUpdatedAt(LocalDateTime.now());
        
        Intersection updated = intersectionRepository.save(intersection);
        
        // Publish resume event
        publishIntersectionStatusEvent(updated, oldStatus, IntersectionStatus.ACTIVE);
        
        logger.info("Resumed intersection: {}", intersectionUuid);
        return convertToResponse(updated);
    }
    
    /**
     * Get intersection timing history.
     */
    @Transactional(readOnly = true)
    public Object getIntersectionHistory(String intersectionUuid) {
        logger.info("Getting timing history for intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        // TODO: Implement actual history retrieval from StateHistory repository
        // This would query state_history table for all traffic lights in this intersection
        
        return "Timing history for intersection: " + intersection.getName();
    }
    
    /**
     * Change light sequence for intersection.
     */
    public Object changeIntersectionLights(String intersectionUuid, java.util.Map<String, Object> request) {
        logger.info("Changing light sequence for intersection: {}", intersectionUuid);
        
        Intersection intersection = intersectionRepository.findByUuid(intersectionUuid)
            .orElseThrow(() -> new IntersectionNotFoundException(intersectionUuid));
        
        // TODO: Implement light sequence change logic
        // This would coordinate state changes across multiple traffic lights
        // ensuring safety constraints are maintained
        
        return "Light sequence change initiated for intersection: " + intersection.getName();
    }
}