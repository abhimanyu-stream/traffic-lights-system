package com.trafficlight.service;

import com.trafficlight.domain.LightSequence;
import com.trafficlight.dto.request.CreateLightSequenceRequest;
import com.trafficlight.dto.response.LightSequenceResponse;
import com.trafficlight.enums.LightState;
import com.trafficlight.repository.LightSequenceRepository;
import com.trafficlight.exception.LightSequenceNotFoundException;
import com.trafficlight.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing light sequence operations for timing control.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
@Transactional
public class LightSequenceService {
    
    private static final Logger logger = LoggerFactory.getLogger(LightSequenceService.class);
    
    private final LightSequenceRepository lightSequenceRepository;
    
    @Autowired
    public LightSequenceService(LightSequenceRepository lightSequenceRepository) {
        this.lightSequenceRepository = lightSequenceRepository;
    }
    
    /**
     * Get all light sequences with pagination.
     */
    @Transactional(readOnly = true)
    public Page<LightSequenceResponse> getAllLightSequences(Pageable pageable) {
        logger.info("Getting all light sequences with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<LightSequence> sequences = lightSequenceRepository.findAll(pageable);
        return sequences.map(this::convertToResponse);
    }
    
    /**
     * Get light sequence by UUID.
     */
    @Transactional(readOnly = true)
    public LightSequenceResponse getLightSequenceById(String sequenceUuid) {
        logger.info("Getting light sequence by UUID: {}", sequenceUuid);
        
        LightSequence sequence = lightSequenceRepository.findByUuid(sequenceUuid)
            .orElseThrow(() -> new LightSequenceNotFoundException(sequenceUuid));
        
        return convertToResponse(sequence);
    }
    
    /**
     * Create a new light sequence.
     */
    public LightSequenceResponse createLightSequence(CreateLightSequenceRequest request) {
        logger.info("Creating new light sequence for intersection: {}, state: {}, duration: {}s", 
                   request.getIntersectionId(), request.getState(), request.getDurationSeconds());
        
        // Validate sequence order uniqueness for intersection
        if (lightSequenceRepository.existsByIntersectionIdAndSequenceOrder(
                request.getIntersectionId(), request.getSequenceOrder())) {
            throw new ValidationException("Sequence order " + request.getSequenceOrder() + 
                                     " already exists for intersection: " + request.getIntersectionId(),
                                     List.of("Sequence order must be unique within intersection"));
        }
        
        LightSequence sequence = LightSequence.builder()
            .uuid(UUID.randomUUID().toString())
            .intersectionId(request.getIntersectionId())
            .state(request.getState())
            .durationSeconds(request.getDurationSeconds())
            .sequenceOrder(request.getSequenceOrder())
            .description(request.getDescription())
            .nextSequenceId(request.getNextSequenceId())
            .isActive(true)
            .build();
        
        LightSequence saved = lightSequenceRepository.save(sequence);
        
        logger.info("Created light sequence with UUID: {}", saved.getUuid());
        return convertToResponse(saved);
    }
    
    /**
     * Update light sequence.
     */
    public LightSequenceResponse updateLightSequence(String sequenceUuid, CreateLightSequenceRequest request) {
        logger.info("Updating light sequence: {}", sequenceUuid);
        
        LightSequence sequence = lightSequenceRepository.findByUuid(sequenceUuid)
            .orElseThrow(() -> new RuntimeException("Light sequence not found: " + sequenceUuid));
        
        // Validate sequence order uniqueness if changed
        if (!sequence.getSequenceOrder().equals(request.getSequenceOrder()) &&
            lightSequenceRepository.existsByIntersectionIdAndSequenceOrder(
                request.getIntersectionId(), request.getSequenceOrder())) {
            throw new RuntimeException("Sequence order " + request.getSequenceOrder() + 
                                     " already exists for intersection: " + request.getIntersectionId());
        }
        
        // Update fields (note: intersectionId is immutable after creation)
        sequence.setState(request.getState());
        sequence.setDurationSeconds(request.getDurationSeconds());
        sequence.setSequenceOrder(request.getSequenceOrder());
        sequence.setDescription(request.getDescription());
        sequence.setNextSequenceId(request.getNextSequenceId());
        
        LightSequence updated = lightSequenceRepository.save(sequence);
        
        logger.info("Updated light sequence: {}", sequenceUuid);
        return convertToResponse(updated);
    }
    
    /**
     * Delete light sequence (soft delete).
     */
    public void deleteLightSequence(String sequenceUuid) {
        logger.info("Deleting light sequence: {}", sequenceUuid);
        
        LightSequence sequence = lightSequenceRepository.findByUuid(sequenceUuid)
            .orElseThrow(() -> new RuntimeException("Light sequence not found: " + sequenceUuid));
        
        sequence.setIsActive(false);
        
        lightSequenceRepository.save(sequence);
        
        logger.info("Deleted light sequence: {}", sequenceUuid);
    }
    
    /**
     * Get light sequences for a specific intersection.
     */
    @Transactional(readOnly = true)
    public List<LightSequenceResponse> getLightSequencesByIntersection(String intersectionId) {
        logger.info("Getting light sequences for intersection: {}", intersectionId);
        
        List<LightSequence> sequences = lightSequenceRepository.findByIntersectionIdAndIsActiveOrderBySequenceOrder(intersectionId, true);
        return sequences.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get light sequences by state.
     */
    @Transactional(readOnly = true)
    public List<LightSequenceResponse> getLightSequencesByState(LightState state) {
        logger.info("Getting light sequences by state: {}", state);
        
        List<LightSequence> sequences = lightSequenceRepository.findByState(state);
        return sequences.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get next sequence in the chain.
     */
    @Transactional(readOnly = true)
    public LightSequenceResponse getNextSequence(String currentSequenceUuid) {
        logger.info("Getting next sequence for: {}", currentSequenceUuid);
        
        LightSequence currentSequence = lightSequenceRepository.findByUuid(currentSequenceUuid)
            .orElseThrow(() -> new RuntimeException("Light sequence not found: " + currentSequenceUuid));
        
        if (currentSequence.getNextSequenceId() != null) {
            LightSequence nextSequence = lightSequenceRepository.findByUuid(currentSequence.getNextSequenceId())
                .orElseThrow(() -> new RuntimeException("Next sequence not found: " + currentSequence.getNextSequenceId()));
            return convertToResponse(nextSequence);
        }
        
        // If no explicit next sequence, find the next in order
        List<LightSequence> sequences = lightSequenceRepository
            .findByIntersectionIdAndIsActiveOrderBySequenceOrder(currentSequence.getIntersectionId(), true);
        
        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i).getUuid().equals(currentSequenceUuid)) {
                // Return next in sequence, or first if at end (cycle)
                int nextIndex = (i + 1) % sequences.size();
                return convertToResponse(sequences.get(nextIndex));
            }
        }
        
        throw new RuntimeException("Unable to determine next sequence for: " + currentSequenceUuid);
    }
    
    /**
     * Validate sequence chain for an intersection.
     */
    @Transactional(readOnly = true)
    public boolean validateSequenceChain(String intersectionId) {
        logger.info("Validating sequence chain for intersection: {}", intersectionId);
        
        List<LightSequence> sequences = lightSequenceRepository
            .findByIntersectionIdAndIsActiveOrderBySequenceOrder(intersectionId, true);
        
        if (sequences.isEmpty()) {
            return false;
        }
        
        // Check for gaps in sequence order
        for (int i = 0; i < sequences.size(); i++) {
            if (!sequences.get(i).getSequenceOrder().equals(i + 1)) {
                logger.warn("Gap in sequence order at position {} for intersection: {}", i + 1, intersectionId);
                return false;
            }
        }
        
        // Check for valid state transitions
        for (int i = 0; i < sequences.size(); i++) {
            LightSequence current = sequences.get(i);
            LightSequence next = sequences.get((i + 1) % sequences.size());
            
            if (!current.getState().canTransitionTo(next.getState())) {
                logger.warn("Invalid state transition from {} to {} in intersection: {}", 
                           current.getState(), next.getState(), intersectionId);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get total cycle time for an intersection.
     */
    @Transactional(readOnly = true)
    public int getTotalCycleTime(String intersectionId) {
        logger.info("Calculating total cycle time for intersection: {}", intersectionId);
        
        List<LightSequence> sequences = lightSequenceRepository
            .findByIntersectionIdAndIsActiveOrderBySequenceOrder(intersectionId, true);
        
        return sequences.stream()
            .mapToInt(LightSequence::getDurationSeconds)
            .sum();
    }
    
    /**
     * Reorder sequences for an intersection.
     */
    public List<LightSequenceResponse> reorderSequences(String intersectionId, List<String> sequenceUuids) {
        logger.info("Reordering sequences for intersection: {}", intersectionId);
        
        List<LightSequence> sequences = sequenceUuids.stream()
            .map(uuid -> lightSequenceRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Light sequence not found: " + uuid)))
            .collect(Collectors.toList());
        
        // Update sequence orders
        for (int i = 0; i < sequences.size(); i++) {
            sequences.get(i).setSequenceOrder(i + 1);
        }
        
        List<LightSequence> updated = lightSequenceRepository.saveAll(sequences);
        
        logger.info("Reordered {} sequences for intersection: {}", updated.size(), intersectionId);
        return updated.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert LightSequence entity to response DTO.
     */
    private LightSequenceResponse convertToResponse(LightSequence sequence) {
        LightSequenceResponse response = new LightSequenceResponse();
        response.setId(sequence.getId());
        response.setUuid(sequence.getUuid());
        response.setIntersectionId(sequence.getIntersectionId());
        response.setState(sequence.getState());
        response.setDurationSeconds(sequence.getDurationSeconds());
        response.setSequenceOrder(sequence.getSequenceOrder());
        response.setDescription(sequence.getDescription());
        response.setNextSequenceId(sequence.getNextSequenceId());
        response.setIsActive(sequence.getIsActive());
        response.setCreatedAt(sequence.getCreatedAt());
        response.setUpdatedAt(sequence.getUpdatedAt());
        response.setVersion(sequence.getVersion());
        response.setHasNextSequence(sequence.hasNextSequence());
        response.setIsLastInCycle(sequence.isLastInCycle());
        return response;
    }
}