package com.trafficlight.filters;

import com.trafficlight.constants.ApiConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter for logging HTTP requests and responses.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
@Order(2)
public class RequestResponseLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private static final int MAX_PAYLOAD_LENGTH = 1000;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Skip logging for actuator endpoints
        if (httpRequest.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response for content caching
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log incoming request
            logRequest(wrappedRequest);
            
            // Continue with the filter chain
            chain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log outgoing response
            logResponse(wrappedRequest, wrappedResponse, duration);
            
            // Copy cached response content to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    /**
     * Log incoming HTTP request.
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        try {
            Map<String, Object> requestLog = new HashMap<>();
            requestLog.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            requestLog.put("type", "REQUEST");
            requestLog.put("method", request.getMethod());
            requestLog.put("uri", request.getRequestURI());
            requestLog.put("queryString", request.getQueryString());
            requestLog.put("remoteAddr", getClientIpAddress(request));
            requestLog.put("userAgent", request.getHeader("User-Agent"));
            requestLog.put("contentType", request.getContentType());
            requestLog.put("contentLength", request.getContentLength());
            
            // Log headers (excluding sensitive ones)
            Map<String, String> headers = getFilteredHeaders(request);
            requestLog.put("headers", headers);
            
            // Log request body for POST/PUT requests
            if (shouldLogRequestBody(request)) {
                String requestBody = getRequestBody(request);
                if (requestBody != null && !requestBody.isEmpty()) {
                    requestLog.put("body", truncatePayload(requestBody));
                }
            }
            
            logger.info("HTTP Request: {}", requestLog);
            auditLogger.info("REQUEST: {} {} from {} - {}", 
                           request.getMethod(), request.getRequestURI(), 
                           getClientIpAddress(request), headers.get(ApiConstants.CORRELATION_ID_HEADER));
            
        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }
    
    /**
     * Log outgoing HTTP response.
     */
    private void logResponse(ContentCachingRequestWrapper request, 
                           ContentCachingResponseWrapper response, 
                           long duration) {
        try {
            Map<String, Object> responseLog = new HashMap<>();
            responseLog.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            responseLog.put("type", "RESPONSE");
            responseLog.put("method", request.getMethod());
            responseLog.put("uri", request.getRequestURI());
            responseLog.put("status", response.getStatus());
            responseLog.put("durationMs", duration);
            responseLog.put("contentType", response.getContentType());
            responseLog.put("contentLength", response.getContentSize());
            
            // Log response headers
            Map<String, String> responseHeaders = new HashMap<>();
            for (String headerName : response.getHeaderNames()) {
                responseHeaders.put(headerName, response.getHeader(headerName));
            }
            responseLog.put("headers", responseHeaders);
            
            // Log response body for error responses or if explicitly enabled
            if (shouldLogResponseBody(response)) {
                String responseBody = getResponseBody(response);
                if (responseBody != null && !responseBody.isEmpty()) {
                    responseLog.put("body", truncatePayload(responseBody));
                }
            }
            
            // Determine log level based on response status
            if (response.getStatus() >= 400) {
                logger.warn("HTTP Response: {}", responseLog);
            } else {
                logger.info("HTTP Response: {}", responseLog);
            }
            
            auditLogger.info("RESPONSE: {} {} - {} ({} ms) - {}", 
                           request.getMethod(), request.getRequestURI(), 
                           response.getStatus(), duration,
                           responseHeaders.get(ApiConstants.CORRELATION_ID_HEADER));
            
        } catch (Exception e) {
            logger.error("Error logging response", e);
        }
    }
    
    /**
     * Get filtered headers (excluding sensitive information).
     */
    private Map<String, String> getFilteredHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Filter out sensitive headers
            if (isSensitiveHeader(headerName)) {
                headers.put(headerName, "[FILTERED]");
            } else {
                headers.put(headerName, headerValue);
            }
        }
        
        return headers;
    }
    
    /**
     * Check if header contains sensitive information.
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization") ||
               lowerCaseName.contains("password") ||
               lowerCaseName.contains("token") ||
               lowerCaseName.contains("secret") ||
               lowerCaseName.contains("key");
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
    
    /**
     * Check if request body should be logged.
     */
    private boolean shouldLogRequestBody(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType();
        
        // Log body for POST, PUT, PATCH requests with JSON content
        return ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) &&
               contentType != null && contentType.contains("application/json");
    }
    
    /**
     * Check if response body should be logged.
     */
    private boolean shouldLogResponseBody(HttpServletResponse response) {
        // Log response body for error responses (4xx, 5xx)
        return response.getStatus() >= 400;
    }
    
    /**
     * Get request body content.
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.debug("Could not read request body", e);
        }
        return null;
    }
    
    /**
     * Get response body content.
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            logger.debug("Could not read response body", e);
        }
        return null;
    }
    
    /**
     * Truncate payload if it exceeds maximum length.
     */
    private String truncatePayload(String payload) {
        if (payload != null && payload.length() > MAX_PAYLOAD_LENGTH) {
            return payload.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
        }
        return payload;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing RequestResponseLoggingFilter");
    }
    
    @Override
    public void destroy() {
        logger.info("Destroying RequestResponseLoggingFilter");
    }
}