package com.trafficlight.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Input sanitization utility to prevent injection attacks.
 * Sanitizes user input before processing.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class InputSanitizer {
    
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('.+--)|(--)|(;)|(\\|\\|)|(\\*)|(<)|(>)|(\\^)|(\\[)|(\\])|(\\{)|(\\})", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>|javascript:|onerror=|onload=|<iframe|<object|<embed",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile(
        "[<>\"'%;()&+]"
    );
    
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String sanitized = input.trim();
        sanitized = removeXSS(sanitized);
        sanitized = removeSQLInjection(sanitized);
        
        return sanitized;
    }
    
    public String sanitizeStrict(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return SPECIAL_CHARS_PATTERN.matcher(input).replaceAll("");
    }
    
    private String removeXSS(String input) {
        return XSS_PATTERN.matcher(input).replaceAll("");
    }
    
    private String removeSQLInjection(String input) {
        return SQL_INJECTION_PATTERN.matcher(input).replaceAll("");
    }
    
    public boolean isSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        
        return !SQL_INJECTION_PATTERN.matcher(input).find() 
            && !XSS_PATTERN.matcher(input).find();
    }
}
