package com.trafficlight.utils;

import com.trafficlight.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Utility class for building consistent API responses.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class ResponseBuilder {
    
    private ResponseBuilder() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Build successful response with data.
     * 
     * @param data the response data
     * @param <T> the type of data
     * @return ResponseEntity with success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        ApiResponse<T> response = ApiResponse.success(data);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Build successful response with data and custom message.
     * 
     * @param data the response data
     * @param message the success message
     * @param <T> the type of data
     * @return ResponseEntity with success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        ApiResponse<T> response = ApiResponse.success(data, message);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Build successful response without data.
     * 
     * @param message the success message
     * @param <T> the type of data
     * @return ResponseEntity with success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        ApiResponse<T> response = ApiResponse.success();
        response.setMessage(message);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Build created response for new resources.
     * 
     * @param data the created resource data
     * @param <T> the type of data
     * @return ResponseEntity with created status
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        ApiResponse<T> response = ApiResponse.success(data, "Resource created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Build created response with custom message.
     * 
     * @param data the created resource data
     * @param message the creation message
     * @param <T> the type of data
     * @return ResponseEntity with created status
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        ApiResponse<T> response = ApiResponse.success(data, message);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Build paginated response from Spring Data Page.
     * 
     * @param page the Spring Data page
     * @param <T> the type of data
     * @return ResponseEntity with paginated response
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> paginated(Page<T> page) {
        ApiResponse<List<T>> response = ApiResponse.paginated(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Build paginated response with custom message.
     * 
     * @param page the Spring Data page
     * @param message the response message
     * @param <T> the type of data
     * @return ResponseEntity with paginated response
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> paginated(Page<T> page, String message) {
        ApiResponse<List<T>> response = ApiResponse.paginated(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
        response.setMessage(message);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Build error response with bad request status.
     * 
     * @param message the error message
     * @param <T> the type of data
     * @return ResponseEntity with bad request status
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        ApiResponse<T> response = ApiResponse.error(message);
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Build error response with validation errors.
     * 
     * @param message the error message
     * @param errors the list of validation errors
     * @param <T> the type of data
     * @return ResponseEntity with bad request status
     */
    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message, List<String> errors) {
        ApiResponse<T> response = ApiResponse.error(message, errors);
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Build not found response.
     * 
     * @param message the not found message
     * @param <T> the type of data
     * @return ResponseEntity with not found status
     */
    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        ApiResponse<T> response = ApiResponse.error(message);
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Build conflict response.
     * 
     * @param message the conflict message
     * @param <T> the type of data
     * @return ResponseEntity with conflict status
     */
    public static <T> ResponseEntity<ApiResponse<T>> conflict(String message) {
        ApiResponse<T> response = ApiResponse.error(message);
        response.setStatusCode(HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Build internal server error response.
     * 
     * @param message the error message
     * @param <T> the type of data
     * @return ResponseEntity with internal server error status
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalServerError(String message) {
        ApiResponse<T> response = ApiResponse.error(message);
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Build unauthorized response.
     * 
     * @param message the unauthorized message
     * @param <T> the type of data
     * @return ResponseEntity with unauthorized status
     */
    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String message) {
        ApiResponse<T> response = ApiResponse.error(message);
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Build forbidden response.
     * 
     * @param message the forbidden message
     * @param <T> the type of data
     * @return ResponseEntity with forbidden status
     */
    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String message) {
        ApiResponse<T> response = ApiResponse.error(message);
        response.setStatusCode(HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Build custom status response.
     * 
     * @param data the response data
     * @param message the response message
     * @param status the HTTP status
     * @param <T> the type of data
     * @return ResponseEntity with custom status
     */
    public static <T> ResponseEntity<ApiResponse<T>> custom(T data, String message, HttpStatus status) {
        ApiResponse<T> response;
        if (status.is2xxSuccessful()) {
            response = ApiResponse.success(data, message);
        } else {
            response = ApiResponse.error(message);
            response.setData(data);
        }
        response.setStatusCode(status.value());
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Add correlation ID to response.
     * 
     * @param responseEntity the response entity
     * @param correlationId the correlation ID
     * @param <T> the type of data
     * @return ResponseEntity with correlation ID
     */
    public static <T> ResponseEntity<ApiResponse<T>> withCorrelationId(
            ResponseEntity<ApiResponse<T>> responseEntity, 
            String correlationId) {
        if (responseEntity.getBody() != null) {
            responseEntity.getBody().setCorrelationId(correlationId);
        }
        return responseEntity;
    }
    
    /**
     * Add request path to response.
     * 
     * @param responseEntity the response entity
     * @param path the request path
     * @param <T> the type of data
     * @return ResponseEntity with request path
     */
    public static <T> ResponseEntity<ApiResponse<T>> withPath(
            ResponseEntity<ApiResponse<T>> responseEntity, 
            String path) {
        if (responseEntity.getBody() != null) {
            responseEntity.getBody().setPath(path);
        }
        return responseEntity;
    }
}