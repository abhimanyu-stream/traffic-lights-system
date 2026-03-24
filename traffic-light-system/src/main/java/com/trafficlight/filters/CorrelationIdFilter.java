package com.trafficlight.filters;

import com.trafficlight.constants.ApiConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to handle correlation ID for request tracing.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get or generate correlation ID
        String correlationId = httpRequest.getHeader(ApiConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Generate request ID
        String requestId = UUID.randomUUID().toString();
        
        try {
            // Set correlation ID and request ID in MDC for logging
            MDC.put("correlationId", correlationId);
            MDC.put("requestId", requestId);
            
            // Add correlation ID and request ID to response headers
            httpResponse.setHeader(ApiConstants.CORRELATION_ID_HEADER, correlationId);
            httpResponse.setHeader(ApiConstants.REQUEST_ID_HEADER, requestId);
            
            logger.debug("Processing request with correlation ID: {} and request ID: {}", 
                        correlationId, requestId);
            
            // Continue with the filter chain
            chain.doFilter(request, response);
            
        } finally {
            // Clean up MDC
            MDC.remove("correlationId");
            MDC.remove("requestId");
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing CorrelationIdFilter");
    }
    
    @Override
    public void destroy() {
        logger.info("Destroying CorrelationIdFilter");
    }
}