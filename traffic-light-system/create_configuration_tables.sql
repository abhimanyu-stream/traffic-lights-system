-- MySQL script to create configuration tables for Traffic Light System
-- Database: traffic_light_dev

USE traffic_light_dev;

-- Table for system configurations (key-value pairs)
CREATE TABLE IF NOT EXISTS system_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    value_type VARCHAR(50) NOT NULL DEFAULT 'string', -- string, integer, boolean, double
    description TEXT,
    category VARCHAR(100), -- timing, system, limits, thresholds
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    
    INDEX idx_config_key (config_key),
    INDEX idx_category (category),
    INDEX idx_is_active (is_active),
    INDEX idx_updated_at (updated_at)
);

-- Table for feature flags
CREATE TABLE IF NOT EXISTS feature_flags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_name VARCHAR(255) NOT NULL UNIQUE,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    category VARCHAR(100), -- analytics, metrics, security, performance
    environment VARCHAR(50) DEFAULT 'all', -- dev, test, prod, all
    rollout_percentage INT DEFAULT 100, -- for gradual rollouts
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'system',
    updated_by VARCHAR(100) DEFAULT 'system',
    
    INDEX idx_feature_name (feature_name),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_category (category),
    INDEX idx_environment (environment),
    INDEX idx_is_active (is_active),
    INDEX idx_updated_at (updated_at)
);

-- Table for configuration history (audit trail)
CREATE TABLE IF NOT EXISTS configuration_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    value_type VARCHAR(50) NOT NULL,
    change_type VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE, RESET
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) NOT NULL,
    reason TEXT,
    session_id VARCHAR(100),
    ip_address VARCHAR(45),
    
    INDEX idx_config_key (config_key),
    INDEX idx_change_type (change_type),
    INDEX idx_changed_at (changed_at),
    INDEX idx_changed_by (changed_by)
);

-- Table for feature flag history (audit trail)
CREATE TABLE IF NOT EXISTS feature_flag_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_name VARCHAR(255) NOT NULL,
    old_enabled BOOLEAN,
    new_enabled BOOLEAN NOT NULL,
    change_type VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE, RESET
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) NOT NULL,
    reason TEXT,
    session_id VARCHAR(100),
    ip_address VARCHAR(45),
    
    INDEX idx_feature_name (feature_name),
    INDEX idx_change_type (change_type),
    INDEX idx_changed_at (changed_at),
    INDEX idx_changed_by (changed_by)
);

-- Insert default system configurations
INSERT INTO system_configurations (config_key, config_value, value_type, description, category) VALUES
-- Timing configurations
('redLightDuration', '30', 'integer', 'Default duration for red light in seconds', 'timing'),
('yellowLightDuration', '5', 'integer', 'Default duration for yellow light in seconds', 'timing'),
('greenLightDuration', '25', 'integer', 'Default duration for green light in seconds', 'timing'),
('minimumCycleDuration', '60', 'integer', 'Minimum cycle duration in seconds', 'timing'),

-- System settings
('maxConcurrentRequests', '100', 'integer', 'Maximum concurrent API requests allowed', 'system'),
('requestTimeoutSeconds', '30', 'integer', 'Request timeout in seconds', 'system'),
('enableMetrics', 'true', 'boolean', 'Enable system metrics collection', 'system'),
('enableAuditLogging', 'true', 'boolean', 'Enable audit logging', 'system'),

-- System limits
('maxIntersections', '1000', 'integer', 'Maximum number of intersections allowed', 'limits'),
('maxTrafficLightsPerIntersection', '8', 'integer', 'Maximum traffic lights per intersection', 'limits'),
('maxSequencesPerIntersection', '10', 'integer', 'Maximum sequences per intersection', 'limits'),
('maxEventHistoryDays', '90', 'integer', 'Maximum days to keep event history', 'limits'),

-- Thresholds
('highCpuUsage', '80', 'integer', 'High CPU usage threshold percentage', 'thresholds'),
('highMemoryUsage', '85', 'integer', 'High memory usage threshold percentage', 'thresholds'),
('slowResponseTime', '1000', 'integer', 'Slow response time threshold in milliseconds', 'thresholds'),
('errorRateThreshold', '5', 'integer', 'Error rate threshold percentage', 'thresholds')

ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Insert default feature flags
INSERT INTO feature_flags (feature_name, is_enabled, description, category) VALUES
('enableAnalytics', TRUE, 'Enable traffic analytics and reporting', 'analytics'),
('enableMetrics', TRUE, 'Enable system performance metrics', 'metrics'),
('enableAuditLogging', TRUE, 'Enable audit logging for security', 'security'),
('enableRealTimeUpdates', TRUE, 'Enable real-time state updates', 'performance'),
('enableAdvancedSequencing', FALSE, 'Enable advanced traffic light sequencing', 'features'),
('enableEmergencyOverride', TRUE, 'Enable emergency override functionality', 'safety')

ON DUPLICATE KEY UPDATE 
    is_enabled = VALUES(is_enabled),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Show created tables
SHOW TABLES LIKE '%configuration%';
SHOW TABLES LIKE '%feature%';

-- Show sample data
SELECT 'System Configurations:' as info;
SELECT config_key, config_value, value_type, category FROM system_configurations ORDER BY category, config_key;

SELECT 'Feature Flags:' as info;
SELECT feature_name, is_enabled, category FROM feature_flags ORDER BY category, feature_name;