package com.devops.bfis.analyzer.repository;

import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.enums.BuildStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Build data access
 * 
 * Current implementation: In-memory storage
 * Future implementation: Real database (PostgreSQL/MongoDB)
 * 
 * This abstraction allows us to swap out the storage layer without
 * affecting the service logic.
 */
public interface BuildRepository {
    
    /**
     * Retrieve all builds, ordered by triggered time (newest first)
     */
    List<Build> findAll();
    
    /**
     * Retrieve builds within a time range
     */
    List<Build> findByTimeRange(Instant start, Instant end);
    
    /**
     * Retrieve builds by status
     */
    List<Build> findByStatus(BuildStatus status);
    
    /**
     * Retrieve builds for a specific repository
     */
    List<Build> findByRepository(String repositoryName);
    
    /**
     * Find a specific build by ID
     */
    Optional<Build> findById(String id);
    
    /**
     * Save a new build or update existing
     */
    Build save(Build build);
    
    /**
     * Count total builds
     */
    long count();
    
    /**
     * Count failed builds
     */
    long countByStatus(BuildStatus status);
}
