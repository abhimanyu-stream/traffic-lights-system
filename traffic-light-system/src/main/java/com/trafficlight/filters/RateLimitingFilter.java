package com.trafficlight.filters;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter using Resilience4j.
 * Limits the number of requests per client based on IP address.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
@ConditionalOnProperty(name = "traffic-light.rate-limiting.enabled", havingValue = "true", matchIfMissing = false)
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final RateLimiterRegistry rateLimiterRegistry;
    
    public RateLimitingFilter(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String clientId = getClientIdentifier(request);
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(clientId, "apiEndpoints");
        
        try {
            rateLimiter.acquirePermission();
            filterChain.doFilter(request, response);
        } catch (RequestNotPermitted e) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
        }
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}