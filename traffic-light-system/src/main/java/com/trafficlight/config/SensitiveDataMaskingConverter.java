package com.trafficlight.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Custom Logback converter for masking sensitive data in log messages.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {
    
    // Patterns for sensitive data detection
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|pwd|pass)\\s*[:=]\\s*[\"']?([^\\s\"',}]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(token|jwt|bearer)\\s*[:=]\\s*[\"']?([^\\s\"',}]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api[_-]?key|apikey)\\s*[:=]\\s*[\"']?([^\\s\"',}]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:\\d{4}[\\s-]?){3}\\d{4}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b");
    
    private static final String MASK = "***MASKED***";
    
    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        
        if (message == null) {
            return "";
        }
        
        // Apply masking patterns
        message = maskSensitiveData(message);
        
        return message;
    }
    
    /**
     * Apply all masking patterns to the message.
     * 
     * @param message the original message
     * @return the masked message
     */
    private String maskSensitiveData(String message) {
        // Mask passwords
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1=" + MASK);
        
        // Mask tokens
        message = TOKEN_PATTERN.matcher(message).replaceAll("$1=" + MASK);
        
        // Mask API keys
        message = API_KEY_PATTERN.matcher(message).replaceAll("$1=" + MASK);
        
        // Mask credit card numbers (keep first 4 and last 4 digits)
        message = CREDIT_CARD_PATTERN.matcher(message).replaceAll(match -> {
            String cardNumber = match.group();
            String digitsOnly = cardNumber.replaceAll("[\\s-]", "");
            if (digitsOnly.length() >= 8) {
                return digitsOnly.substring(0, 4) + "****" + digitsOnly.substring(digitsOnly.length() - 4);
            }
            return MASK;
        });
        
        // Mask email addresses (keep domain)
        message = EMAIL_PATTERN.matcher(message).replaceAll(match -> {
            String email = match.group();
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                return "***@" + email.substring(atIndex + 1);
            }
            return MASK;
        });
        
        // Mask phone numbers (keep area code)
        message = PHONE_PATTERN.matcher(message).replaceAll("($1) ***-****");
        
        return message;
    }
}