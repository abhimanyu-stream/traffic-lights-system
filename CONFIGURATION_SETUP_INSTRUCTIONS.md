# Configuration Tables Setup Instructions

## Overview
Created database tables and JPA entities for system configuration management. The ConfigurationService now needs to be updated to use database-backed storage instead of in-memory ConcurrentHashMap.

## Files Created

### 1. Database Schema
- **File**: `traffic-light-system/create_configuration_tables.sql`
- **Purpose**: Creates MySQL tables for configuration management
- **Tables Created**:
  - `system_configurations` - Key-value configuration pairs
  - `feature_flags` - Feature flag management
  - `configuration_history` - Audit trail for configuration changes
  - `feature_flag_history` - Audit trail for feature flag changes

### 2. JPA Entities
- **File**: `traffic-light-system/src/main/java/com/trafficlight/domain/Auditor.java`
  - Abstract base class with audit fields (created_at, updated_at)
- **File**: `traffic-light-system/src/main/java/com/trafficlight/domain/SystemConfiguration.java`
  - Entity for system_configurations table
- **File**: `traffic-light-system/src/main/java/com/trafficlight/domain/FeatureFlag.java`
  - Entity for feature_flags table

### 3. Repository Interfaces
- **File**: `traffic-light-system/src/main/java/com/trafficlight/repository/SystemConfigurationRepository.java`
  - Repository for SystemConfiguration entity
- **File**: `traffic-light-system/src/main/java/com/trafficlight/repository/FeatureFlagRepository.java`
  - Repository for FeatureFlag entity

## Manual Setup Steps

### Step 1: Execute SQL Script
Run the SQL script manually in your MySQL client:

```bash
# Connect to MySQL
mysql -u root -proot -h localhost -P 3306

# Select database
USE traffic_light_dev;

# Execute the script content from create_configuration_tables.sql
```

Or copy and paste the SQL content from `traffic-light-system/create_configuration_tables.sql` into your MySQL client.

### Step 2: Update ConfigurationService
The `ConfigurationService.java` currently uses in-memory storage. It needs to be updated to:
1. Inject the new repositories
2. Replace ConcurrentHashMap operations with database operations
3. Add proper transaction management

### Step 3: Update ConfigurationController
The `ConfigurationController.java` needs to be updated to:
1. Inject the actual ConfigurationService (currently commented out)
2. Remove placeholder implementations
3. Use the service methods instead of mock responses

### Step 4: Rebuild and Test
1. Rebuild the application: `mvn clean package`
2. Restart the application
3. Test the configuration endpoints
4. Verify data is persisted in database tables

## Database Tables Structure

### system_configurations
- `id` - Primary key
- `config_key` - Unique configuration key
- `config_value` - Configuration value (TEXT)
- `value_type` - Data type (string, integer, boolean, double)
- `description` - Human-readable description
- `category` - Configuration category (timing, system, limits, thresholds)
- `is_active` - Soft delete flag
- `created_at`, `updated_at` - Audit timestamps
- `created_by`, `updated_by` - User tracking

### feature_flags
- `id` - Primary key
- `feature_name` - Unique feature name
- `is_enabled` - Feature enabled/disabled
- `description` - Feature description
- `category` - Feature category
- `environment` - Target environment (dev, test, prod, all)
- `rollout_percentage` - Gradual rollout percentage
- `is_active` - Soft delete flag
- `created_at`, `updated_at` - Audit timestamps
- `created_by`, `updated_by` - User tracking

## Default Data Inserted
The script inserts default configurations:
- Timing configurations (red: 30s, yellow: 5s, green: 25s)
- System settings (max requests: 100, timeout: 30s)
- System limits (max intersections: 1000, etc.)
- Thresholds (CPU: 80%, memory: 85%, etc.)
- Feature flags (analytics, metrics, audit logging, etc.)

## Next Steps
1. Execute the SQL script manually
2. Update ConfigurationService to use database
3. Update ConfigurationController to use the service
4. Test the configuration management functionality
5. Verify logging and audit trails work correctly