package com.trafficlight.exception;

import com.trafficlight.dto.response.ApiResponse;
import com.trafficlight.utils.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Traffic Light Controller API.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle traffic light not found exceptions.
     */
    @ExceptionHandler(TrafficLightNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTrafficLightNotFound(
            TrafficLightNotFoundException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.notFound(ex.getMessage());
    }
    
    /**
     * Handle intersection not found exceptions.
     */
    @ExceptionHandler(IntersectionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntersectionNotFound(
            IntersectionNotFoundException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.notFound(ex.getMessage());
    }
    
    /**
     * Handle light sequence not found exceptions.
     */
    @ExceptionHandler(LightSequenceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleLightSequenceNotFound(
            LightSequenceNotFoundException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.notFound(ex.getMessage());
    }
    
    /**
     * Handle conflicting directions exceptions.
     */
    @ExceptionHandler(ConflictingDirectionsException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictingDirections(
            ConflictingDirectionsException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.conflict(ex.getMessage());
    }
    
    /**
     * Handle invalid state transition exceptions.
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStateTransition(
            InvalidStateTransitionException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.badRequest(ex.getMessage());
    }
    
    /**
     * Handle concurrent modification exceptions.
     */
    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConcurrentModification(
            ConcurrentModificationException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.conflict(ex.getMessage());
    }
    
    /**
     * Handle validation exceptions.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.badRequest(ex.getMessage(), ex.getValidationErrors());
    }
    
    /**
     * Handle method argument validation exceptions.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());
        
        return ResponseBuilder.badRequest("Validation failed", errors);
    }
    
    /**
     * Handle bind exceptions.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            BindException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());
        
        return ResponseBuilder.badRequest("Binding failed", errors);
    }
    
    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(this::formatConstraintViolation)
            .collect(Collectors.toList());
        
        return ResponseBuilder.badRequest("Constraint validation failed", errors);
    }
    
    /**
     * Handle data integrity violation exceptions.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "Duplicate entry - resource already exists";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Referenced resource does not exist";
            } else if (ex.getMessage().contains("cannot be null")) {
                message = "Required field cannot be null";
            }
        }
        
        return ResponseBuilder.conflict(message);
    }
    
    /**
     * Handle optimistic locking failure exceptions.
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(
            OptimisticLockingFailureException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.conflict("Resource was modified by another user. Please refresh and try again.");
    }
    
    /**
     * Handle method argument type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        
        return ResponseBuilder.badRequest(message);
    }
    
    /**
     * Handle missing servlet request parameter exceptions.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        return ResponseBuilder.badRequest(message);
    }
    
    /**
     * Handle HTTP message not readable exceptions.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.badRequest("Invalid JSON format or malformed request body");
    }
    
    /**
     * Handle HTTP media type not supported exceptions.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = String.format("Media type '%s' is not supported. Supported types: %s",
            ex.getContentType(), ex.getSupportedMediaTypes());
        
        return ResponseBuilder.badRequest(message);
    }
    
    /**
     * Handle HTTP request method not supported exceptions.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = String.format("Method '%s' is not supported for this endpoint. Supported methods: %s",
            ex.getMethod(), ex.getSupportedMethods());
        
        return ResponseBuilder.custom(null, message, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    /**
     * Handle no handler found exceptions.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        String message = String.format("Endpoint '%s %s' not found", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseBuilder.notFound(message);
    }
    
    /**
     * Handle general traffic light exceptions.
     */
    @ExceptionHandler(TrafficLightException.class)
    public ResponseEntity<ApiResponse<Void>> handleTrafficLightException(
            TrafficLightException ex, HttpServletRequest request) {
        
        logException(ex, request);
        
        // Determine HTTP status based on error code
        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return ResponseBuilder.custom(null, ex.getMessage(), status);
    }
    
    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logException(ex, request);
        return ResponseBuilder.internalServerError("An unexpected error occurred. Please try again later.");
    }
    
    /**
     * Format field error for validation messages.
     */
    private String formatFieldError(FieldError fieldError) {
        return String.format("Field '%s': %s (rejected value: %s)",
            fieldError.getField(),
            fieldError.getDefaultMessage(),
            fieldError.getRejectedValue());
    }
    
    /**
     * Format constraint violation for validation messages.
     */
    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return String.format("Property '%s': %s (invalid value: %s)",
            violation.getPropertyPath(),
            violation.getMessage(),
            violation.getInvalidValue());
    }
    
    /**
     * Determine HTTP status based on error code.
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "TRAFFIC_LIGHT_NOT_FOUND", "INTERSECTION_NOT_FOUND", "LIGHT_SEQUENCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICTING_DIRECTIONS", "CONCURRENT_MODIFICATION" -> HttpStatus.CONFLICT;
            case "INVALID_STATE_TRANSITION", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    /**
     * Log exception with correlation ID and request details.
     */
    private void logException(Exception ex, HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        logger.error("Exception occurred - Method: {}, URI: {}, IP: {}, CorrelationId: {}, Exception: {}",
            method, uri, clientIp, correlationId, ex.getMessage(), ex);
    }
    
    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}