package com.trafficlight.controller;

import com.trafficlight.constants.ApiConstants;
import com.trafficlight.dto.request.CreateIntersectionRequest;
import com.trafficlight.dto.request.UpdateIntersectionRequest;
import com.trafficlight.dto.response.ApiResponse;
import com.trafficlight.dto.response.IntersectionResponse;
import com.trafficlight.enums.IntersectionStatus;
import com.trafficlight.service.IntersectionService;
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
import java.util.Map;

/**
 * REST Controller for Intersection operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@RestController
@RequestMapping(ApiConstants.TRAFFIC_SERVICE_BASE)
@Validated
public class IntersectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(IntersectionController.class);
    
    private final IntersectionService intersectionService;
    
    @Autowired
    public IntersectionController(IntersectionService intersectionService) {
        this.intersectionService = intersectionService;
    }
    
    /**
     * Get all intersections with pagination.
     */
    @GetMapping("/intersections")
    public ResponseEntity<ApiResponse<List<IntersectionResponse>>> getAllIntersections(
            @PageableDefault(size = ApiConstants.DEFAULT_SIZE) Pageable pageable) {
        
        logger.info("Getting all intersections with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<IntersectionResponse> intersections = intersectionService.getAllIntersections(pageable);
        return ResponseBuilder.paginated(intersections);
    }
    
    /**
     * Get intersection by ID.
     */
    @GetMapping("/intersections/{intersectionId}")
    public ResponseEntity<ApiResponse<IntersectionResponse>> getIntersectionById(
            @PathVariable String intersectionId) {
        
        logger.info("Getting intersection by ID: {}", intersectionId);
        
        IntersectionResponse intersection = intersectionService.getIntersectionById(intersectionId);
        return ResponseBuilder.success(intersection);
    }
    
    /**
     * Create a new intersection.
     */
    @PostMapping("/intersections")
    public ResponseEntity<ApiResponse<IntersectionResponse>> createIntersection(
            @Valid @RequestBody CreateIntersectionRequest request) {
        
        logger.info("Creating new intersection: {}", request.getName());
        
        IntersectionResponse intersection = intersectionService.createIntersection(request);
        return ResponseBuilder.created(intersection, ApiConstants.INTERSECTION_CREATED);
    }
    
    /**
     * Update intersection.
     */
    @PutMapping("/intersections/{intersectionId}")
    public ResponseEntity<ApiResponse<IntersectionResponse>> updateIntersection(
            @PathVariable String intersectionId,
            @Valid @RequestBody UpdateIntersectionRequest request) {
        
        logger.info("Updating intersection: {}", intersectionId);
        
        IntersectionResponse intersection = intersectionService.updateIntersection(intersectionId, request);
        return ResponseBuilder.success(intersection, ApiConstants.INTERSECTION_UPDATED);
    }
    
    /**
     * Delete intersection.
     */
    @DeleteMapping("/intersections/{intersectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteIntersection(@PathVariable String intersectionId) {
        
        logger.info("Deleting intersection: {}", intersectionId);
        
        intersectionService.deleteIntersection(intersectionId);
        return ResponseBuilder.success(ApiConstants.DELETED_MESSAGE);
    }
    
    /**
     * Update intersection status.
     */
    @PutMapping("/intersections/{intersectionId}/status")
    public ResponseEntity<ApiResponse<IntersectionResponse>> updateIntersectionStatus(
            @PathVariable String intersectionId,
            @RequestParam IntersectionStatus status) {
        
        logger.info("Updating intersection status: {} to {}", intersectionId, status);
        
        IntersectionResponse intersection = intersectionService.updateIntersectionStatus(intersectionId, status);
        return ResponseBuilder.success(intersection, "Intersection status updated successfully");
    }
    
    /**
     * Find nearby intersections.
     */
    @GetMapping("/intersections/nearby")
    public ResponseEntity<ApiResponse<List<IntersectionResponse>>> findNearbyIntersections(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        
        logger.info("Finding intersections near coordinates: {}, {} within {} km", 
                   latitude, longitude, radiusKm);
        
        List<IntersectionResponse> intersections = intersectionService.findNearbyIntersections(
            latitude, longitude, radiusKm);
        return ResponseBuilder.success(intersections);
    }
    
    /**
     * Get intersections by status.
     */
    @GetMapping("/intersections/status/{status}")
    public ResponseEntity<ApiResponse<List<IntersectionResponse>>> getIntersectionsByStatus(
            @PathVariable IntersectionStatus status) {
        
        logger.info("Getting intersections by status: {}", status);
        
        List<IntersectionResponse> intersections = intersectionService.getIntersectionsByStatus(status);
        return ResponseBuilder.success(intersections);
    }
    
    /**
     * Search intersections by name.
     */
    @GetMapping("/intersections/search")
    public ResponseEntity<ApiResponse<List<IntersectionResponse>>> searchIntersectionsByName(
            @RequestParam String name) {
        
        logger.info("Searching intersections by name pattern: {}", name);
        
        List<IntersectionResponse> intersections = intersectionService.searchIntersectionsByName(name);
        return ResponseBuilder.success(intersections);
    }
    
    /**
     * Get intersection statistics.
     */
    @GetMapping("/intersections/{intersectionId}/statistics")
    public ResponseEntity<ApiResponse<Object>> getIntersectionStatistics(
            @PathVariable String intersectionId) {
        
        logger.info("Getting statistics for intersection: {}", intersectionId);
        
        Object statistics = intersectionService.getIntersectionStatistics(intersectionId);
        return ResponseBuilder.success(statistics);
    }
    
    /**
     * Pause intersection operation.
     * 
     * @param intersectionId the intersection UUID
     * @return pause confirmation
     */
    @PostMapping("/intersections/{intersectionId}/pause")
    public ResponseEntity<ApiResponse<IntersectionResponse>> pauseIntersection(
            @PathVariable String intersectionId) {
        
        logger.info("Pausing intersection: {}", intersectionId);
        
        IntersectionResponse intersection = intersectionService.pauseIntersection(intersectionId);
        return ResponseBuilder.success(intersection, "Intersection paused successfully");
    }
    
    /**
     * Resume intersection operation.
     * 
     * @param intersectionId the intersection UUID
     * @return resume confirmation
     */
    @PostMapping("/intersections/{intersectionId}/resume")
    public ResponseEntity<ApiResponse<IntersectionResponse>> resumeIntersection(
            @PathVariable String intersectionId) {
        
        logger.info("Resuming intersection: {}", intersectionId);
        
        IntersectionResponse intersection = intersectionService.resumeIntersection(intersectionId);
        return ResponseBuilder.success(intersection, "Intersection resumed successfully");
    }
    
    /**
     * Get intersection current status.
     * 
     * @param intersectionId the intersection UUID
     * @return current intersection status with all traffic lights
     */
    @GetMapping("/intersections/{intersectionId}/status")
    public ResponseEntity<ApiResponse<IntersectionResponse>> getIntersectionStatus(
            @PathVariable String intersectionId) {
        
        logger.info("Getting current status for intersection: {}", intersectionId);
        
        IntersectionResponse intersection = intersectionService.getIntersectionById(intersectionId);
        return ResponseBuilder.success(intersection);
    }
    
    /**
     * Get intersection timing history.
     * 
     * @param intersectionId the intersection UUID
     * @return timing history for the intersection
     */
    @GetMapping("/intersections/{intersectionId}/history")
    public ResponseEntity<ApiResponse<Object>> getIntersectionHistory(
            @PathVariable String intersectionId) {
        
        logger.info("Getting timing history for intersection: {}", intersectionId);
        
        Object history = intersectionService.getIntersectionHistory(intersectionId);
        return ResponseBuilder.success(history);
    }
    
    /**
     * Change light sequence for intersection.
     * 
     * @param intersectionId the intersection UUID
     * @param request the state change request
     * @return state change result
     */
    @PostMapping("/intersections/{intersectionId}/lights/change")
    public ResponseEntity<ApiResponse<Object>> changeIntersectionLights(
            @PathVariable String intersectionId,
            @RequestBody Map<String, Object> request) {
        
        logger.info("Changing light sequence for intersection: {}", intersectionId);
        
        Object result = intersectionService.changeIntersectionLights(intersectionId, request);
        return ResponseBuilder.success(result, "Light sequence changed successfully");
    }
}