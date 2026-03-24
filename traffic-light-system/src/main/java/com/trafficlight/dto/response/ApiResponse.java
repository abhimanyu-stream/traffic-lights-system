package com.trafficlight.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic API response wrapper for consistent response format.
 * 
 * @param <T> the type of data being returned
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private String correlationId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private String path;
    private Integer statusCode;
    
    // Pagination fields
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    
    // Default constructor
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor for successful response
    public ApiResponse(T data) {
        this();
        this.success = true;
        this.data = data;
        this.message = "Operation completed successfully";
    }
    
    // Constructor for successful response with message
    public ApiResponse(T data, String message) {
        this();
        this.success = true;
        this.data = data;
        this.message = message;
    }
    
    // Constructor for error response
    public ApiResponse(String message, List<String> errors) {
        this();
        this.success = false;
        this.message = message;
        this.errors = errors;
    }
    
    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message);
    }
    
    public static <T> ApiResponse<T> success() {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "Operation completed successfully";
        return response;
    }
    
    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>(message, errors);
    }
    
    // Static factory method for paginated response
    public static <T> ApiResponse<T> paginated(T data, int page, int size, long totalElements, int totalPages) {
        ApiResponse<T> response = new ApiResponse<>(data);
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = totalPages;
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    
    public Long getTotalElements() { return totalElements; }
    public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
    
    public Integer getTotalPages() { return totalPages; }
    public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }
    
    // Convenience methods
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public boolean isPaginated() {
        return page != null && size != null && totalElements != null;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", correlationId='" + correlationId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}