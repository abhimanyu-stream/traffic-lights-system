package com.trafficlight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Manager for critical sections using ReentrantLock.
 * Provides fine-grained locking for specific resources.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Component
public class CriticalSectionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CriticalSectionManager.class);
    
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ReentrantLock masterLock = new ReentrantLock(true); // Fair lock
    
    /**
     * Execute code in a critical section for a specific resource.
     */
    public <T> T executeInCriticalSection(String resourceId, Supplier<T> operation) {
        ReentrantLock lock = getLockForResource(resourceId);
        lock.lock();
        try {
            logger.debug("Entered critical section for resource: {}", resourceId);
            return operation.get();
        } finally {
            lock.unlock();
            logger.debug("Exited critical section for resource: {}", resourceId);
        }
    }
    
    /**
     * Execute code in a critical section with timeout.
     */
    public <T> T executeInCriticalSectionWithTimeout(String resourceId, Supplier<T> operation, 
                                                     long timeout, TimeUnit unit) throws InterruptedException {
        ReentrantLock lock = getLockForResource(resourceId);
        boolean acquired = lock.tryLock(timeout, unit);
        
        if (!acquired) {
            logger.warn("Failed to acquire lock for resource: {} within timeout", resourceId);
            throw new IllegalStateException("Could not acquire lock for resource: " + resourceId);
        }
        
        try {
            logger.debug("Entered critical section for resource: {} (with timeout)", resourceId);
            return operation.get();
        } finally {
            lock.unlock();
            logger.debug("Exited critical section for resource: {}", resourceId);
        }
    }
    
    /**
     * Execute code in a critical section without blocking (try lock).
     */
    public <T> T executeInCriticalSectionNonBlocking(String resourceId, Supplier<T> operation, 
                                                     Supplier<T> fallback) {
        ReentrantLock lock = getLockForResource(resourceId);
        boolean acquired = lock.tryLock();
        
        if (!acquired) {
            logger.warn("Could not acquire lock for resource: {}, using fallback", resourceId);
            return fallback.get();
        }
        
        try {
            logger.debug("Entered critical section for resource: {} (non-blocking)", resourceId);
            return operation.get();
        } finally {
            lock.unlock();
            logger.debug("Exited critical section for resource: {}", resourceId);
        }
    }
    
    /**
     * Execute runnable in a critical section.
     */
    public void executeInCriticalSection(String resourceId, Runnable operation) {
        executeInCriticalSection(resourceId, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * Check if a resource is currently locked.
     */
    public boolean isLocked(String resourceId) {
        ReentrantLock lock = locks.get(resourceId);
        return lock != null && lock.isLocked();
    }
    
    /**
     * Check if current thread holds the lock for a resource.
     */
    public boolean isHeldByCurrentThread(String resourceId) {
        ReentrantLock lock = locks.get(resourceId);
        return lock != null && lock.isHeldByCurrentThread();
    }
    
    /**
     * Get the number of threads waiting for a resource lock.
     */
    public int getQueueLength(String resourceId) {
        ReentrantLock lock = locks.get(resourceId);
        return lock != null ? lock.getQueueLength() : 0;
    }
    
    /**
     * Execute code in master critical section (global lock).
     */
    public <T> T executeInMasterCriticalSection(Supplier<T> operation) {
        masterLock.lock();
        try {
            logger.debug("Entered master critical section");
            return operation.get();
        } finally {
            masterLock.unlock();
            logger.debug("Exited master critical section");
        }
    }
    
    /**
     * Get or create a lock for a specific resource.
     */
    private ReentrantLock getLockForResource(String resourceId) {
        return locks.computeIfAbsent(resourceId, k -> {
            logger.debug("Creating new lock for resource: {}", resourceId);
            return new ReentrantLock(true); // Fair lock
        });
    }
    
    /**
     * Remove lock for a resource (cleanup).
     */
    public void removeLock(String resourceId) {
        ReentrantLock lock = locks.remove(resourceId);
        if (lock != null && lock.isLocked()) {
            logger.warn("Removing lock for resource: {} while it's still locked", resourceId);
        }
    }
    
    /**
     * Get statistics about locks.
     */
    public LockStatistics getStatistics() {
        int totalLocks = locks.size();
        long lockedCount = locks.values().stream().filter(ReentrantLock::isLocked).count();
        int maxQueueLength = locks.values().stream()
            .mapToInt(ReentrantLock::getQueueLength)
            .max()
            .orElse(0);
        
        return new LockStatistics(totalLocks, (int) lockedCount, maxQueueLength);
    }
    
    /**
     * Statistics about locks.
     */
    public static class LockStatistics {
        private final int totalLocks;
        private final int lockedCount;
        private final int maxQueueLength;
        
        public LockStatistics(int totalLocks, int lockedCount, int maxQueueLength) {
            this.totalLocks = totalLocks;
            this.lockedCount = lockedCount;
            this.maxQueueLength = maxQueueLength;
        }
        
        public int getTotalLocks() { return totalLocks; }
        public int getLockedCount() { return lockedCount; }
        public int getMaxQueueLength() { return maxQueueLength; }
        
        @Override
        public String toString() {
            return String.format("LockStatistics{total=%d, locked=%d, maxQueue=%d}",
                totalLocks, lockedCount, maxQueueLength);
        }
    }
}
