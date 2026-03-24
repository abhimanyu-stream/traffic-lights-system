package com.trafficlight.controller;

import com.trafficlight.constants.ApiConstants;
import com.trafficlight.dto.request.CreateLightSequenceRequest;
import com.trafficlight.dto.response.ApiResponse;
import com.trafficlight.dto.response.LightSequenceResponse;
import com.trafficlight.enums.LightState;
import com.trafficlight.service.LightSequenceService;
import com.trafficlight.utils.ResponseBuilder;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Light Sequence operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@RestController
@RequestMapping(ApiConstants.TRAFFIC_SERVICE_BASE)
@Validated
public class LightSequenceController {
    
    private static final Logger logger = LoggerFactory.getLogger(LightSequenceController.class);
    
    private final LightSequenceService lightSequenceService;
    
    @Autowired
    public LightSequenceController(LightSequenceService lightSequenceService) {
        this.lightSequenceService = lightSequenceService;
    }
    
    /**
     * Get all light sequences with pagination.
     * 
     * @param pageable pagination parameters
     * @return paginated list of light sequences
     */
    @GetMapping("/sequences")
    public ResponseEntity<ApiResponse<List<LightSequenceResponse>>> getAllLightSequences(
            @PageableDefault(size = ApiConstants.DEFAULT_SIZE) Pageable pageable) {
        
        logger.info("Getting all light sequences with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<LightSequenceResponse> sequences = lightSequenceService.getAllLightSequences(pageable);
        return ResponseBuilder.paginated(sequences);
    }
    
    /**
     * Get light sequence by ID.
     * 
     * @param sequenceId the light sequence UUID
     * @return light sequence details
     */
    @GetMapping("/sequences/{sequenceId}")
    public ResponseEntity<ApiResponse<LightSequenceResponse>> getLightSequenceById(
            @PathVariable String sequenceId) {
        
        logger.info("Getting light sequence by ID: {}", sequenceId);
        
        LightSequenceResponse sequence = lightSequenceService.getLightSequenceById(sequenceId);
        return ResponseBuilder.success(sequence);
    }
    
    /**
     * Create a new light sequence.
     * 
     * @param request the create light sequence request
     * @return created light sequence details
     */
    @PostMapping("/sequences")
    public ResponseEntity<ApiResponse<LightSequenceResponse>> createLightSequence(
            @Valid @RequestBody CreateLightSequenceRequest request) {
        
        logger.info("Creating new light sequence for intersection: {}, state: {}, duration: {}s", 
                   request.getIntersectionId(), request.getState(), request.getDurationSeconds());
        
        LightSequenceResponse sequence = lightSequenceService.createLightSequence(request);
        return ResponseBuilder.created(sequence, "Light sequence created successfully");
    }
    
    /**
     * Update light sequence.
     * 
     * @param sequenceId the light sequence UUID
     * @param request the update request
     * @return updated light sequence details
     */
    @PutMapping("/sequences/{sequenceId}")
    public ResponseEntity<ApiResponse<LightSequenceResponse>> updateLightSequence(
            @PathVariable String sequenceId,
            @Valid @RequestBody CreateLightSequenceRequest request) {
        
        logger.info("Updating light sequence: {}", sequenceId);
        
        LightSequenceResponse sequence = lightSequenceService.updateLightSequence(sequenceId, request);
        return ResponseBuilder.success(sequence, "Light sequence updated successfully");
    }
    
    /**
     * Delete light sequence.
     * 
     * @param sequenceId the light sequence UUID
     * @return deletion confirmation
     */
    @DeleteMapping("/sequences/{sequenceId}")
    public ResponseEntity<ApiResponse<Void>> deleteLightSequence(@PathVariable String sequenceId) {
        
        logger.info("Deleting light sequence: {}", sequenceId);
        
        lightSequenceService.deleteLightSequence(sequenceId);
        return ResponseBuilder.success(ApiConstants.DELETED_MESSAGE);
    }
    
    /**
     * Get light sequences for a specific intersection.
     * 
     * @param intersectionId the intersection UUID
     * @return list of light sequences for the intersection
     */
    @GetMapping("/intersections/{intersectionId}/sequences")
    public ResponseEntity<ApiResponse<List<LightSequenceResponse>>> getLightSequencesByIntersection(
            @PathVariable String intersectionId) {
        
        logger.info("Getting light sequences for intersection: {}", intersectionId);
        
        List<LightSequenceResponse> sequences = lightSequenceService.getLightSequencesByIntersection(intersectionId);
        return ResponseBuilder.success(sequences);
    }
    
    /**
     * Get light sequences by state.
     * 
     * @param state the light state
     * @return list of light sequences with the specified state
     */
    @GetMapping("/sequences/state/{state}")
    public ResponseEntity<ApiResponse<List<LightSequenceResponse>>> getLightSequencesByState(
            @PathVariable LightState state) {
        
        logger.info("Getting light sequences by state: {}", state);
        
        List<LightSequenceResponse> sequences = lightSequenceService.getLightSequencesByState(state);
        return ResponseBuilder.success(sequences);
    }
    
    /**
     * Get next sequence in the chain.
     * 
     * @param sequenceId the current sequence UUID
     * @return next sequence in the chain
     */
    @GetMapping("/sequences/{sequenceId}/next")
    public ResponseEntity<ApiResponse<LightSequenceResponse>> getNextSequence(
            @PathVariable String sequenceId) {
        
        logger.info("Getting next sequence for: {}", sequenceId);
        
        LightSequenceResponse nextSequence = lightSequenceService.getNextSequence(sequenceId);
        return ResponseBuilder.success(nextSequence);
    }
    
    /**
     * Validate sequence chain for an intersection.
     * 
     * @param intersectionId the intersection UUID
     * @return validation result
     */
    @GetMapping("/intersections/{intersectionId}/sequences/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateSequenceChain(
            @PathVariable String intersectionId) {
        
        logger.info("Validating sequence chain for intersection: {}", intersectionId);
        
        boolean isValid = lightSequenceService.validateSequenceChain(intersectionId);
        String message = isValid ? "Sequence chain is valid" : "Sequence chain has validation errors";
        
        return ResponseBuilder.success(isValid, message);
    }
    
    /**
     * Get total cycle time for an intersection.
     * 
     * @param intersectionId the intersection UUID
     * @return total cycle time in seconds
     */
    @GetMapping("/intersections/{intersectionId}/sequences/cycle-time")
    public ResponseEntity<ApiResponse<Integer>> getTotalCycleTime(
            @PathVariable String intersectionId) {
        
        logger.info("Getting total cycle time for intersection: {}", intersectionId);
        
        int totalTime = lightSequenceService.getTotalCycleTime(intersectionId);
        return ResponseBuilder.success(totalTime, "Total cycle time calculated successfully");
    }
    
    /**
     * Reorder sequences for an intersection.
     * 
     * @param intersectionId the intersection UUID
     * @param sequenceUuids ordered list of sequence UUIDs
     * @return reordered sequences
     */
    @PutMapping("/intersections/{intersectionId}/sequences/reorder")
    public ResponseEntity<ApiResponse<List<LightSequenceResponse>>> reorderSequences(
            @PathVariable String intersectionId,
            @RequestBody List<String> sequenceUuids) {
        
        logger.info("Reordering sequences for intersection: {}", intersectionId);
        
        List<LightSequenceResponse> sequences = lightSequenceService.reorderSequences(intersectionId, sequenceUuids);
        return ResponseBuilder.success(sequences, "Sequences reordered successfully");
    }
    
    /**
     * Get sequence statistics for an intersection.
     * 
     * @param intersectionId the intersection UUID
     * @return sequence statistics
     */
    @GetMapping("/intersections/{intersectionId}/sequences/stats")
    public ResponseEntity<ApiResponse<Object>> getSequenceStatistics(
            @PathVariable String intersectionId) {
        
        logger.info("Getting sequence statistics for intersection: {}", intersectionId);
        
        // TODO: Implement detailed statistics
        int totalTime = lightSequenceService.getTotalCycleTime(intersectionId);
        boolean isValid = lightSequenceService.validateSequenceChain(intersectionId);
        
        Object stats = new Object() {
            public final int totalCycleTimeSeconds = totalTime;
            public final boolean isValidChain = isValid;
            public final String message = "Basic sequence statistics";
        };
        
        return ResponseBuilder.success(stats);
    }
}