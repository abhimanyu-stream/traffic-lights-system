package com.trafficlight.controller;

import com.trafficlight.constants.ApiConstants;
import com.trafficlight.dto.request.CreateTrafficLightRequest;
import com.trafficlight.dto.request.StateChangeRequest;
import com.trafficlight.dto.response.ApiResponse;
import com.trafficlight.dto.response.StateChangeResponse;
import com.trafficlight.dto.response.TrafficLightResponse;
import com.trafficlight.service.TrafficLightService;
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
 * REST Controller for Traffic Light operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@RestController
@RequestMapping(ApiConstants.TRAFFIC_SERVICE_BASE)
@Validated
public class TrafficLightController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrafficLightController.class);
    
    private final TrafficLightService trafficLightService;
    
    @Autowired
    public TrafficLightController(TrafficLightService trafficLightService) {
        this.trafficLightService = trafficLightService;
    }
    
    /**
     * Get all traffic lights with pagination.
     * 
     * @param pageable pagination parameters
     * @return paginated list of traffic lights
     */
    @GetMapping("/lights")
    public ResponseEntity<ApiResponse<List<TrafficLightResponse>>> getAllTrafficLights(
            @PageableDefault(size = ApiConstants.DEFAULT_SIZE) Pageable pageable) {
        
        logger.info("Getting all traffic lights with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        Page<TrafficLightResponse> trafficLights = trafficLightService.getAllTrafficLights(pageable);
        return ResponseBuilder.paginated(trafficLights);
    }
    
    /**
     * Get traffic light by ID.
     * 
     * @param lightId the traffic light UUID
     * @return traffic light details
     */
    @GetMapping("/lights/{lightId}")
    public ResponseEntity<ApiResponse<TrafficLightResponse>> getTrafficLightById(
            @PathVariable String lightId) {
        
        logger.info("Getting traffic light by ID: {}", lightId);
        
        TrafficLightResponse trafficLight = trafficLightService.getTrafficLightById(lightId);
        return ResponseBuilder.success(trafficLight);
    }
    
    /**
     * Create a new traffic light.
     * 
     * @param request the create traffic light request
     * @return created traffic light details
     */
    @PostMapping("/lights")
    public ResponseEntity<ApiResponse<TrafficLightResponse>> createTrafficLight(
            @Valid @RequestBody CreateTrafficLightRequest request) {
        
        logger.info("Creating new traffic light for intersection: {}, direction: {}", 
                   request.getIntersectionId(), request.getDirection());
        
        TrafficLightResponse trafficLight = trafficLightService.createTrafficLight(request);
        return ResponseBuilder.created(trafficLight, ApiConstants.TRAFFIC_LIGHT_CREATED);
    }
    
    /**
     * Update traffic light state.
     * 
     * @param lightId the traffic light UUID
     * @param request the state change request
     * @return state change result
     */
    @PutMapping("/lights/{lightId}/state")
    public ResponseEntity<ApiResponse<StateChangeResponse>> changeTrafficLightState(
            @PathVariable String lightId,
            @Valid @RequestBody StateChangeRequest request) {
        
        logger.info("Changing traffic light state: lightId={}, targetState={}, reason={}", 
                   lightId, request.getTargetState(), request.getReason());
        
        StateChangeResponse response = trafficLightService.changeState(lightId, request);
        return ResponseBuilder.success(response, ApiConstants.TRAFFIC_LIGHT_STATE_CHANGED);
    }
    
    /**
     * Get traffic lights for a specific intersection.
     * 
     * @param intersectionId the intersection UUID
     * @return list of traffic lights for the intersection
     */
    @GetMapping("/intersections/{intersectionId}/lights")
    public ResponseEntity<ApiResponse<List<TrafficLightResponse>>> getTrafficLightsByIntersection(
            @PathVariable String intersectionId) {
        
        logger.info("Getting traffic lights for intersection: {}", intersectionId);
        
        List<TrafficLightResponse> trafficLights = trafficLightService.getTrafficLightsByIntersection(intersectionId);
        return ResponseBuilder.success(trafficLights);
    }
    
    /**
     * Update traffic light configuration.
     * 
     * @param lightId the traffic light UUID
     * @param request the update request
     * @return updated traffic light details
     */
    @PutMapping("/lights/{lightId}")
    public ResponseEntity<ApiResponse<TrafficLightResponse>> updateTrafficLight(
            @PathVariable String lightId,
            @Valid @RequestBody CreateTrafficLightRequest request) {
        
        logger.info("Updating traffic light: {}", lightId);
        
        TrafficLightResponse trafficLight = trafficLightService.updateTrafficLight(lightId, request);
        return ResponseBuilder.success(trafficLight, ApiConstants.TRAFFIC_LIGHT_UPDATED);
    }
    
    /**
     * Delete traffic light.
     * 
     * @param lightId the traffic light UUID
     * @return deletion confirmation
     */
    @DeleteMapping("/lights/{lightId}")
    public ResponseEntity<ApiResponse<Void>> deleteTrafficLight(@PathVariable String lightId) {
        
        logger.info("Deleting traffic light: {}", lightId);
        
        trafficLightService.deleteTrafficLight(lightId);
        return ResponseBuilder.success(ApiConstants.DELETED_MESSAGE);
    }
    
    /**
     * Get traffic light state history.
     * 
     * @param lightId the traffic light UUID
     * @param pageable pagination parameters
     * @return paginated state history
     */
    @GetMapping("/lights/{lightId}/history")
    public ResponseEntity<ApiResponse<List<Object>>> getTrafficLightHistory(
            @PathVariable String lightId,
            @PageableDefault(size = ApiConstants.DEFAULT_SIZE) Pageable pageable) {
        
        logger.info("Getting state history for traffic light: {}", lightId);
        
        Page<Object> history = trafficLightService.getStateHistory(lightId, pageable);
        return ResponseBuilder.paginated(history);
    }
    
    /**
     * Get traffic lights that need state change.
     * 
     * @return list of traffic lights that have exceeded their duration
     */
    @GetMapping("/lights/expired")
    public ResponseEntity<ApiResponse<List<TrafficLightResponse>>> getExpiredTrafficLights() {
        
        logger.info("Getting traffic lights that need state change");
        
        List<TrafficLightResponse> expiredLights = trafficLightService.getExpiredTrafficLights();
        return ResponseBuilder.success(expiredLights);
    }
    
    /**
     * Bulk state change for multiple traffic lights.
     * 
     * @param intersectionId the intersection UUID
     * @param request the state change request
     * @return bulk state change results
     */
    @PutMapping("/intersections/{intersectionId}/lights/state")
    public ResponseEntity<ApiResponse<List<StateChangeResponse>>> bulkStateChange(
            @PathVariable String intersectionId,
            @Valid @RequestBody StateChangeRequest request) {
        
        logger.info("Bulk state change for intersection: {}, targetState: {}", 
                   intersectionId, request.getTargetState());
        
        List<StateChangeResponse> responses = trafficLightService.bulkStateChange(intersectionId, request);
        return ResponseBuilder.success(responses);
    }
    
    /**
     * Get traffic light metrics.
     * 
     * @param lightId the traffic light UUID
     * @return traffic light metrics
     */
    @GetMapping("/lights/{lightId}/metrics")
    public ResponseEntity<ApiResponse<Object>> getTrafficLightMetrics(@PathVariable String lightId) {
        
        logger.info("Getting metrics for traffic light: {}", lightId);
        
        Object metrics = trafficLightService.getMetrics(lightId);
        return ResponseBuilder.success(metrics);
    }
}