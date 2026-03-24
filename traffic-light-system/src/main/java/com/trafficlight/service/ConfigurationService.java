package com.trafficlight.service;

import com.trafficlight.domain.SystemConfiguration;
import com.trafficlight.domain.FeatureFlag;
import com.trafficlight.domain.TimingConfiguration;
import com.trafficlight.repository.SystemConfigurationRepository;
import com.trafficlight.repository.FeatureFlagRepository;
import com.trafficlight.utils.PerformanceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service for system configuration operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Service
@Transactional
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    
    private final PerformanceLogger performanceLogger;
    private final SystemConfigurationRepository configurationRepository;
    private final FeatureFlagRepository featureFlagRepository;
    
    @Autowired
    public ConfigurationService(PerformanceLogger performanceLogger,
                               SystemConfigurationRepository configurationRepository,
                               FeatureFlagRepository featureFlagRepository) {
        this.performanceLogger = performanceLogger;
        this.configurationRepository = configurationRepository;
        this.featureFlagRepository = featureFlagRepository;
    }
    
    /**
     * Get all system configurations.
     */
    @Transactional(readOnly = true)
    public Object getAllConfigurations() {
        return performanceLogger.logExecutionTime("getAllConfigurations", () -> {
            logger.info("Getting all system configurations");
            
            List<SystemConfiguration> allConfigs = configurationRepository.findByIsActiveTrueOrderByCategory();
            
            Map<String, Object> timingConfigsMap = allConfigs.stream()
                .filter(config -> "timing".equals(config.getCategory()))
                .collect(Collectors.toMap(
                    SystemConfiguration::getConfigKey,
                    SystemConfiguration::getTypedValue
                ));
            
            Map<String, Object> systemSettingsMap = allConfigs.stream()
                .filter(config -> "system".equals(config.getCategory()))
                .collect(Collectors.toMap(
                    SystemConfiguration::getConfigKey,
                    SystemConfiguration::getTypedValue
                ));
            
            Map<String, Object> systemLimitsMap = allConfigs.stream()
                .filter(config -> "limits".equals(config.getCategory()))
                .collect(Collectors.toMap(
                    SystemConfiguration::getConfigKey,
                    SystemConfiguration::getTypedValue
                ));
            
            Map<String, Object> thresholdsMap = allConfigs.stream()
                .filter(config -> "thresholds".equals(config.getCategory()))
                .collect(Collectors.toMap(
                    SystemConfiguration::getConfigKey,
                    SystemConfiguration::getTypedValue
                ));
            
            return new Object() {
                public final String message = "System configurations retrieved successfully";
                public final Map<String, Object> defaultTimings = timingConfigsMap;
                public final Map<String, Object> systemSettings = systemSettingsMap;
                public final Map<String, Object> systemLimits = systemLimitsMap;
                public final Map<String, Object> thresholds = thresholdsMap;
                public final LocalDateTime lastUpdated = LocalDateTime.now();
                public final int totalConfigurations = allConfigs.size();
            };
        });
    }
    
    /**
     * Get configuration by key.
     */
    @Transactional(readOnly = true)
    public Object getConfiguration(String configKey) {
        return performanceLogger.logExecutionTime("getConfiguration", () -> {
            logger.info("Getting configuration for key: {}", configKey);
            
            SystemConfiguration config = configurationRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .orElse(null);
            
            Object configValue = config != null ? config.getTypedValue() : null;
            String configType = config != null ? config.getValueType() : "unknown";
            
            return new Object() {
                public final String key = configKey;
                public final String message = config != null ? "Configuration retrieved successfully" : "Configuration not found";
                public final Object value = configValue != null ? configValue : "Not found";
                public final String type = configType;
                public final boolean exists = config != null;
                public final String description = config != null ? config.getDescription() : null;
                public final String category = config != null ? config.getCategory() : null;
                public final LocalDateTime lastUpdated = config != null ? config.getUpdatedAt() : LocalDateTime.now();
            };
        });
    }
    
    /**
     * Update configuration value.
     */
    public Object updateConfiguration(String configKey, Object configValue) {
        return performanceLogger.logExecutionTime("updateConfiguration", () -> {
            logger.info("Updating configuration: {} = {}", configKey, configValue);
            
            SystemConfiguration config = configurationRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .orElse(null);
            
            final Object oldConfigValue = config != null ? config.getTypedValue() : null;
            final boolean wasNewConfig = config == null;
            
            if (config == null) {
                // Create new configuration
                config = SystemConfiguration.builder()
                    .configKey(configKey)
                    .isActive(true)
                    .updatedBy("system")
                    .build();
            }
            
            config.setTypedValue(configValue);
            config.touch(); // Update timestamp
            
            SystemConfiguration saved = configurationRepository.save(config);
            
            return new Object() {
                public final String key = configKey;
                public final Object value = configValue;
                public final Object previousValue = oldConfigValue;
                public final String message = "Configuration updated successfully";
                public final LocalDateTime updatedAt = saved.getUpdatedAt();
                public final String updatedBy = "system";
                public final boolean wasNew = wasNewConfig;
                public final String valueType = saved.getValueType();
            };
        });
    }
    
    /**
     * Get timing configurations.
     */
    @Transactional(readOnly = true)
    public Object getTimingConfigurations() {
        return performanceLogger.logExecutionTime("getTimingConfigurations", () -> {
            logger.info("Getting timing configurations");
            
            List<SystemConfiguration> timingConfigs = configurationRepository.findByCategoryAndIsActiveTrueOrderByConfigKey("timing");
            
            // Extract timing values
            Integer redDuration = timingConfigs.stream()
                .filter(config -> "redLightDuration".equals(config.getConfigKey()))
                .findFirst()
                .map(config -> (Integer) config.getTypedValue())
                .orElse(30);
            
            Integer yellowDuration = timingConfigs.stream()
                .filter(config -> "yellowLightDuration".equals(config.getConfigKey()))
                .findFirst()
                .map(config -> (Integer) config.getTypedValue())
                .orElse(5);
            
            Integer greenDuration = timingConfigs.stream()
                .filter(config -> "greenLightDuration".equals(config.getConfigKey()))
                .findFirst()
                .map(config -> (Integer) config.getTypedValue())
                .orElse(25);
            
            // Create TimingConfiguration object
            TimingConfiguration timingConfig = TimingConfiguration.of(redDuration, yellowDuration, greenDuration);
            
            return new Object() {
                public final String message = "Timing configurations retrieved successfully";
                public final TimingConfiguration configuration = timingConfig;
                public final Map<String, Integer> defaultDurations = Map.of(
                    "RED", timingConfig.getRedDurationSeconds(),
                    "YELLOW", timingConfig.getYellowDurationSeconds(),
                    "GREEN", timingConfig.getGreenDurationSeconds()
                );
                public final Map<String, Integer> minimumDurations = Map.of(
                    "RED", 10,
                    "YELLOW", 3,
                    "GREEN", 10
                );
                public final Map<String, Integer> maximumDurations = Map.of(
                    "RED", 120,
                    "YELLOW", 10,
                    "GREEN", 120
                );
                public final int totalCycleDuration = timingConfig.getTotalCycleDuration();
                public final LocalDateTime lastUpdated = LocalDateTime.now();
            };
        });
    }
    
    /**
     * Update timing configurations.
     */
    public Object updateTimingConfigurations(Map<String, Object> timingConfig) {
        return performanceLogger.logExecutionTime("updateTimingConfigurations", () -> {
            logger.info("Updating timing configurations: {}", timingConfig);
            
            int configurationsUpdated = 0;
            
            // Extract timing values from request
            Integer redDuration = extractIntegerValue(timingConfig, "redLightDuration");
            Integer yellowDuration = extractIntegerValue(timingConfig, "yellowLightDuration");
            Integer greenDuration = extractIntegerValue(timingConfig, "greenLightDuration");
            
            // Create TimingConfiguration object for validation
            TimingConfiguration newTimingConfig = null;
            if (redDuration != null && yellowDuration != null && greenDuration != null) {
                newTimingConfig = TimingConfiguration.of(redDuration, yellowDuration, greenDuration);
            }
            
            // Update timing configurations
            for (Map.Entry<String, Object> entry : timingConfig.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (key.equals("redLightDuration") || key.equals("yellowLightDuration") || 
                    key.equals("greenLightDuration") || key.equals("minimumCycleDuration")) {
                    
                    SystemConfiguration config = configurationRepository.findByConfigKeyAndIsActiveTrue(key)
                        .orElse(SystemConfiguration.builder()
                            .configKey(key)
                            .category("timing")
                            .isActive(true)
                            .updatedBy("system")
                            .build());
                    
                    config.setTypedValue(value);
                    config.touch();
                    configurationRepository.save(config);
                    configurationsUpdated++;
                }
            }
            
            // Make variables effectively final for anonymous object
            final int finalConfigurationsUpdated = configurationsUpdated;
            final TimingConfiguration finalNewTimingConfig = newTimingConfig;
            
            return new Object() {
                public final Map<String, Object> updatedConfigurations = timingConfig;
                public final TimingConfiguration validatedConfiguration = finalNewTimingConfig;
                public final String message = "Timing configurations updated successfully";
                public final LocalDateTime updatedAt = LocalDateTime.now();
                public final String updatedBy = "system";
                public final int configurationsUpdatedCount = finalConfigurationsUpdated;
                public final int totalCycleDuration = finalNewTimingConfig != null ? finalNewTimingConfig.getTotalCycleDuration() : 0;
            };
        });
    }
    
    /**
     * Helper method to extract integer values from configuration map.
     */
    private Integer extractIntegerValue(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key {}: {}", key, value);
                return null;
            }
        }
        return null;
    }
    
    /**
     * Get feature flags.
     */
    @Transactional(readOnly = true)
    public Object getFeatureFlags() {
        return performanceLogger.logExecutionTime("getFeatureFlags", () -> {
            logger.info("Getting system feature flags");
            
            List<FeatureFlag> allFlags = featureFlagRepository.findByIsActiveTrueOrderByCategory();
            
            Map<String, Boolean> flags = allFlags.stream()
                .collect(Collectors.toMap(
                    FeatureFlag::getFeatureName,
                    FeatureFlag::getIsEnabled
                ));
            
            long enabledFlags = allFlags.stream().mapToLong(f -> f.getIsEnabled() ? 1 : 0).sum();
            long disabledFlags = allFlags.size() - enabledFlags;
            
            return new Object() {
                public final String message = "Feature flags retrieved successfully";
                public final Map<String, Boolean> flagsMap = flags;
                public final LocalDateTime lastUpdated = LocalDateTime.now();
                public final int totalFlags = allFlags.size();
                public final long enabledFlagsCount = enabledFlags;
                public final long disabledFlagsCount = disabledFlags;
            };
        });
    }
    
    /**
     * Update feature flag.
     */
    public Object updateFeatureFlag(String featureName, boolean enabled) {
        return performanceLogger.logExecutionTime("updateFeatureFlag", () -> {
            logger.info("Updating feature flag: {} = {}", featureName, enabled);
            
            FeatureFlag flag = featureFlagRepository.findByFeatureNameAndIsActiveTrue(featureName)
                .orElse(null);
            
            final Boolean oldValue = flag != null ? flag.getIsEnabled() : null;
            final boolean wasNew = flag == null;
            
            if (flag == null) {
                // Create new feature flag
                flag = FeatureFlag.builder()
                    .featureName(featureName)
                    .isActive(true)
                    .updatedBy("system")
                    .build();
            }
            
            flag.setIsEnabled(enabled);
            flag.touch(); // Update timestamp
            
            FeatureFlag saved = featureFlagRepository.save(flag);
            
            return new Object() {
                public final String name = featureName;
                public final boolean isEnabled = enabled;
                public final Boolean previousValue = oldValue;
                public final String message = "Feature flag updated successfully";
                public final LocalDateTime updatedAt = saved.getUpdatedAt();
                public final String updatedBy = "system";
                public final boolean wasNewFlag = wasNew;
                public final boolean valueChanged = oldValue == null || !oldValue.equals(enabled);
            };
        });
    }
    
    /**
     * Get system limits.
     */
    @Transactional(readOnly = true)
    public Object getSystemLimits() {
        return performanceLogger.logExecutionTime("getSystemLimits", () -> {
            logger.info("Getting system limits and thresholds");
            
            List<SystemConfiguration> limitConfigs = configurationRepository.findByCategoryAndIsActiveTrueOrderByConfigKey("limits");
            List<SystemConfiguration> thresholdConfigs = configurationRepository.findByCategoryAndIsActiveTrueOrderByConfigKey("thresholds");
            
            Map<String, Integer> limits = limitConfigs.stream()
                .collect(Collectors.toMap(
                    SystemConfiguration::getConfigKey,
                    config -> (Integer) config.getTypedValue()
                ));
            
            Map<String, Integer> thresholds = thresholdConfigs.stream()
                .collect(Collectors.toMap(
                    SystemConfiguration::getConfigKey,
                    config -> (Integer) config.getTypedValue()
                ));
            
            return new Object() {
                public final String message = "System limits retrieved successfully";
                public final Map<String, Integer> limitsMap = limits;
                public final Map<String, Integer> thresholdsMap = thresholds;
                public final LocalDateTime lastUpdated = LocalDateTime.now();
            };
        });
    }
    
    /**
     * Reset configuration to defaults.
     */
    public Object resetConfiguration(String configKey) {
        return performanceLogger.logExecutionTime("resetConfiguration", () -> {
            if (configKey != null) {
                logger.info("Resetting configuration key to default: {}", configKey);
                
                SystemConfiguration config = configurationRepository.findByConfigKeyAndIsActiveTrue(configKey)
                    .orElse(null);
                
                Object oldValue = config != null ? config.getTypedValue() : null;
                
                if (config != null) {
                    // For demo, we'll deactivate the current config and let default data take over
                    config.setIsActive(false);
                    configurationRepository.save(config);
                }
                
                return new Object() {
                    public final String key = configKey;
                    public final String message = "Configuration reset to default successfully";
                    public final String scope = "single";
                    public final Object previousValue = oldValue;
                    public final LocalDateTime resetAt = LocalDateTime.now();
                    public final String resetBy = "system";
                };
            } else {
                logger.info("Resetting all configurations to defaults");
                
                List<SystemConfiguration> allConfigs = configurationRepository.findByIsActiveTrueOrderByCategory();
                List<FeatureFlag> allFlags = featureFlagRepository.findByIsActiveTrueOrderByCategory();
                
                int previousConfigCount = allConfigs.size();
                int previousFlagCount = allFlags.size();
                
                // Deactivate all current configurations
                allConfigs.forEach(config -> config.setIsActive(false));
                allFlags.forEach(flag -> flag.setIsActive(false));
                
                configurationRepository.saveAll(allConfigs);
                featureFlagRepository.saveAll(allFlags);
                
                // Note: Default data will be available from the SQL script initialization
                
                return new Object() {
                    public final String key = null;
                    public final String message = "All configurations reset to defaults successfully";
                    public final String scope = "all";
                    public final int previousConfigurationCount = previousConfigCount;
                    public final int previousFeatureFlagCount = previousFlagCount;
                    public final LocalDateTime resetAt = LocalDateTime.now();
                    public final String resetBy = "system";
                };
            }
        });
    }
    
    /**
     * Create TimingConfiguration from current system settings.
     */
    @Transactional(readOnly = true)
    public TimingConfiguration createTimingConfiguration() {
        return performanceLogger.logExecutionTime("createTimingConfiguration", () -> {
            logger.info("Creating TimingConfiguration from current system settings");
            
            List<SystemConfiguration> timingConfigs = configurationRepository.findByCategoryAndIsActiveTrueOrderByConfigKey("timing");
            
            // Extract timing values with defaults
            Integer redDuration = timingConfigs.stream()
                .filter(config -> "redLightDuration".equals(config.getConfigKey()))
                .findFirst()
                .map(config -> (Integer) config.getTypedValue())
                .orElse(30);
            
            Integer yellowDuration = timingConfigs.stream()
                .filter(config -> "yellowLightDuration".equals(config.getConfigKey()))
                .findFirst()
                .map(config -> (Integer) config.getTypedValue())
                .orElse(5);
            
            Integer greenDuration = timingConfigs.stream()
                .filter(config -> "greenLightDuration".equals(config.getConfigKey()))
                .findFirst()
                .map(config -> (Integer) config.getTypedValue())
                .orElse(25);
            
            return TimingConfiguration.of(redDuration, yellowDuration, greenDuration);
        });
    }
    
    /**
     * Update system configurations from TimingConfiguration object.
     */
    public Object updateFromTimingConfiguration(TimingConfiguration timingConfig) {
        return performanceLogger.logExecutionTime("updateFromTimingConfiguration", () -> {
            logger.info("Updating system configurations from TimingConfiguration: {}", timingConfig);
            
            int configurationsUpdated = 0;
            
            // Update red light duration
            SystemConfiguration redConfig = configurationRepository.findByConfigKeyAndIsActiveTrue("redLightDuration")
                .orElse(SystemConfiguration.builder()
                    .configKey("redLightDuration")
                    .category("timing")
                    .isActive(true)
                    .updatedBy("system")
                    .build());
            redConfig.setTypedValue(timingConfig.getRedDurationSeconds());
            redConfig.touch();
            configurationRepository.save(redConfig);
            configurationsUpdated++;
            
            // Update yellow light duration
            SystemConfiguration yellowConfig = configurationRepository.findByConfigKeyAndIsActiveTrue("yellowLightDuration")
                .orElse(SystemConfiguration.builder()
                    .configKey("yellowLightDuration")
                    .category("timing")
                    .isActive(true)
                    .updatedBy("system")
                    .build());
            yellowConfig.setTypedValue(timingConfig.getYellowDurationSeconds());
            yellowConfig.touch();
            configurationRepository.save(yellowConfig);
            configurationsUpdated++;
            
            // Update green light duration
            SystemConfiguration greenConfig = configurationRepository.findByConfigKeyAndIsActiveTrue("greenLightDuration")
                .orElse(SystemConfiguration.builder()
                    .configKey("greenLightDuration")
                    .category("timing")
                    .isActive(true)
                    .updatedBy("system")
                    .build());
            greenConfig.setTypedValue(timingConfig.getGreenDurationSeconds());
            greenConfig.touch();
            configurationRepository.save(greenConfig);
            configurationsUpdated++;
            
            // Make variable effectively final for anonymous object
            final int finalConfigurationsUpdated = configurationsUpdated;
            
            return new Object() {
                public final TimingConfiguration configuration = timingConfig;
                public final String message = "System configurations updated from TimingConfiguration successfully";
                public final LocalDateTime updatedAt = LocalDateTime.now();
                public final String updatedBy = "system";
                public final int configurationsUpdatedCount = finalConfigurationsUpdated;
                public final int totalCycleDuration = timingConfig.getTotalCycleDuration();
            };
        });
    }
    
    /**
     * Validate timing configuration against system constraints.
     */
    public Object validateTimingConfiguration(TimingConfiguration timingConfig) {
        return performanceLogger.logExecutionTime("validateTimingConfiguration", () -> {
            logger.info("Validating TimingConfiguration: {}", timingConfig);
            
            List<String> validationErrors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            try {
                // TimingConfiguration constructor already validates ranges
                // Additional business rule validations can be added here
                
                int totalCycle = timingConfig.getTotalCycleDuration();
                if (totalCycle > 180) {
                    warnings.add("Total cycle duration (" + totalCycle + "s) is quite long, consider optimizing");
                }
                
                if (totalCycle < 30) {
                    warnings.add("Total cycle duration (" + totalCycle + "s) is very short, may cause safety issues");
                }
                
                // Check yellow light duration relative to others
                if (timingConfig.getYellowDurationSeconds() > timingConfig.getGreenDurationSeconds()) {
                    warnings.add("Yellow duration is longer than green duration, which is unusual");
                }
                
            } catch (IllegalArgumentException e) {
                validationErrors.add(e.getMessage());
            }
            
            boolean isValid = validationErrors.isEmpty();
            
            return new Object() {
                public final TimingConfiguration configuration = timingConfig;
                public final boolean validationResult = isValid;
                public final List<String> errors = validationErrors;
                public final List<String> warningsList = warnings;
                public final String message = isValid ? "TimingConfiguration is valid" : "TimingConfiguration has validation errors";
                public final int totalCycleDuration = timingConfig.getTotalCycleDuration();
                public final LocalDateTime validatedAt = LocalDateTime.now();
            };
        });
    }
}