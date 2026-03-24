package com.trafficlight.constants;

/**
 * Kafka constants for Traffic Light Controller system.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
public final class KafkaConstants {
    
    private KafkaConstants() {
        // Utility class - prevent instantiation
    }
    
    // Consumer group IDs
    public static final String GROUP_ANALYTICS_PROCESSOR = "analytics-processor";
    public static final String GROUP_BATCH_PROCESSOR = "batch-processor";
    
    // Transaction configuration
    public static final int TRANSACTION_TIMEOUT_MS = 30000;
    
    // Producer configuration
    public static final int PRODUCER_BATCH_SIZE = 16384;
    public static final int PRODUCER_LINGER_MS = 5;
    public static final long PRODUCER_BUFFER_MEMORY = 33554432L;
    
    // Consumer configuration
    public static final int CONSUMER_MAX_POLL_RECORDS = 100;
    public static final int CONSUMER_SESSION_TIMEOUT_MS = 30000;
    public static final int CONSUMER_HEARTBEAT_INTERVAL_MS = 10000;
}