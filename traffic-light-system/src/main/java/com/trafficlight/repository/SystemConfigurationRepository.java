package com.trafficlight.repository;

import com.trafficlight.domain.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SystemConfiguration entity.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    
    /**
     * Find configuration by key.
     */
    Optional<SystemConfiguration> findByConfigKeyAndIsActiveTrue(String configKey);
    
    /**
     * Find all active configurations.
     */
    List<SystemConfiguration> findByIsActiveTrueOrderByCategory();
    
    /**
     * Find configurations by category.
     */
    List<SystemConfiguration> findByCategoryAndIsActiveTrueOrderByConfigKey(String category);
    
    /**
     * Find configurations by value type.
     */
    List<SystemConfiguration> findByValueTypeAndIsActiveTrueOrderByConfigKey(String valueType);
    
    /**
     * Check if configuration key exists.
     */
    boolean existsByConfigKeyAndIsActiveTrue(String configKey);
    
    /**
     * Get all configuration categories.
     */
    @Query("SELECT DISTINCT c.category FROM SystemConfiguration c WHERE c.isActive = true ORDER BY c.category")
    List<String> findDistinctCategories();
    
    /**
     * Get configuration count by category.
     */
    @Query("SELECT c.category, COUNT(c) FROM SystemConfiguration c WHERE c.isActive = true GROUP BY c.category")
    List<Object[]> countByCategory();
    
    /**
     * Find configurations updated after a specific date.
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isActive = true AND c.updatedAt > :since ORDER BY c.updatedAt DESC")
    List<SystemConfiguration> findRecentlyUpdated(@Param("since") java.time.LocalDateTime since);
    
    /**
     * Search configurations by key pattern.
     */
    @Query("SELECT c FROM SystemConfiguration c WHERE c.isActive = true AND c.configKey LIKE %:pattern% ORDER BY c.configKey")
    List<SystemConfiguration> findByConfigKeyContaining(@Param("pattern") String pattern);
}