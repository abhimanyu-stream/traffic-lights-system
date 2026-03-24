package com.trafficlight.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for asynchronous task execution with custom thread pools.
 * Implements custom ThreadPoolExecutor configurations for different types of operations.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);
    
    /**
     * Default async executor for general asynchronous operations.
     * Used by @Async annotation without explicit executor name.
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        logger.info("Creating default async task executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Rejection policy: CallerRunsPolicy - caller thread executes task if pool is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom thread pool for traffic light state management operations.
     * Optimized for high-priority, time-sensitive state changes.
     */
    @Bean(name = "trafficLightExecutor")
    public Executor trafficLightExecutor() {
        logger.info("Creating traffic light executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("traffic-light-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Rejection policy: AbortPolicy - throw exception if pool is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom thread pool for Kafka event publishing operations.
     * Configured for high throughput event processing.
     */
    @Bean(name = "kafkaEventExecutor")
    public Executor kafkaEventExecutor() {
        logger.info("Creating Kafka event executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("kafka-event-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        // Rejection policy: CallerRunsPolicy - ensures events are not lost
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom thread pool for intersection management operations.
     * Handles intersection-level coordination and updates.
     */
    @Bean(name = "intersectionExecutor")
    public Executor intersectionExecutor() {
        logger.info("Creating intersection executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(150);
        executor.setThreadNamePrefix("intersection-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Rejection policy: CallerRunsPolicy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom thread pool for analytics and reporting operations.
     * Lower priority, can handle longer processing times.
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        logger.info("Creating analytics executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("analytics-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(30);
        
        // Rejection policy: DiscardOldestPolicy - discard oldest task if pool is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom thread pool for batch processing operations.
     * Optimized for bulk operations like state history cleanup.
     */
    @Bean(name = "batchProcessingExecutor")
    public Executor batchProcessingExecutor() {
        logger.info("Creating batch processing executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180);
        
        // Rejection policy: CallerRunsPolicy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Custom thread pool for scheduled task operations.
     * Used by @Scheduled methods with @Async annotation.
     */
    @Bean(name = "scheduledExecutor")
    public Executor scheduledExecutor() {
        logger.info("Creating scheduled task executor");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("scheduled-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Rejection policy: CallerRunsPolicy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
    
    /**
     * Exception handler for uncaught exceptions in async methods.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            logger.error("Uncaught exception in async method: {}.{}", 
                        method.getDeclaringClass().getSimpleName(), 
                        method.getName(), 
                        throwable);
            logger.error("Method parameters: {}", (Object[]) params);
        };
    }
}
