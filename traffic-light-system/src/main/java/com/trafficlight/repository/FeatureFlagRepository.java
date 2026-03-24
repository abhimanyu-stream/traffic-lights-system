package com.trafficlight.repository;

import com.trafficlight.domain.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FeatureFlag entity.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    
    /**
     * Find feature flag by name.
     */
    Optional<FeatureFlag> findByFeatureNameAndIsActiveTrue(String featureName);
    
    /**
     * Find all active feature flags.
     */
    List<FeatureFlag> findByIsActiveTrueOrderByCategory();
    
    /**
     * Find enabled feature flags.
     */
    List<FeatureFlag> findByIsEnabledTrueAndIsActiveTrueOrderByFeatureName();
    
    /**
     * Find feature flags by category.
     */
    List<FeatureFlag> findByCategoryAndIsActiveTrueOrderByFeatureName(String category);
    
    /**
     * Find feature flags by environment.
     */
    List<FeatureFlag> findByEnvironmentAndIsActiveTrueOrderByFeatureName(String environment);
    
    /**
     * Find feature flags for environment (including 'all').
     */
    @Query("SELECT f FROM FeatureFlag f WHERE f.isActive = true AND (f.environment = :env OR f.environment = 'all') ORDER BY f.featureName")
    List<FeatureFlag> findByEnvironmentOrAll(@Param("env") String environment);
    
    /**
     * Check if feature flag exists.
     */
    boolean existsByFeatureNameAndIsActiveTrue(String featureName);
    
    /**
     * Get all feature flag categories.
     */
    @Query("SELECT DISTINCT f.category FROM FeatureFlag f WHERE f.isActive = true ORDER BY f.category")
    List<String> findDistinctCategories();
    
    /**
     * Get feature flag count by category.
     */
    @Query("SELECT f.category, COUNT(f) FROM FeatureFlag f WHERE f.isActive = true GROUP BY f.category")
    List<Object[]> countByCategory();
    
    /**
     * Get enabled/disabled counts.
     */
    @Query("SELECT f.isEnabled, COUNT(f) FROM FeatureFlag f WHERE f.isActive = true GROUP BY f.isEnabled")
    List<Object[]> countByEnabledStatus();
    
    /**
     * Find feature flags updated after a specific date.
     */
    @Query("SELECT f FROM FeatureFlag f WHERE f.isActive = true AND f.updatedAt > :since ORDER BY f.updatedAt DESC")
    List<FeatureFlag> findRecentlyUpdated(@Param("since") java.time.LocalDateTime since);
    
    /**
     * Search feature flags by name pattern.
     */
    @Query("SELECT f FROM FeatureFlag f WHERE f.isActive = true AND f.featureName LIKE %:pattern% ORDER BY f.featureName")
    List<FeatureFlag> findByFeatureNameContaining(@Param("pattern") String pattern);
}