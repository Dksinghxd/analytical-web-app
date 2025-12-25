package com.devops.bfis.analyzer.repository;

import com.devops.bfis.core.domain.Failure;
import com.devops.bfis.core.enums.FailureType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Failure data access
 * 
 * Current: In-memory
 * Future: Database with indexing on failureType and frequencyCount
 */
public interface FailureRepository {
    
    /**
     * Retrieve all failures, ordered by frequency (highest first)
     */
    List<Failure> findAll();
    
    /**
     * Find failures by type
     */
    List<Failure> findByType(FailureType type);
    
    /**
     * Find failures for a specific build
     */
    List<Failure> findByBuildId(String buildId);
    
    /**
     * Find a specific failure by ID
     */
    Optional<Failure> findById(String id);
    
    /**
     * Find failures with frequency above threshold
     */
    List<Failure> findRecurringFailures(int minFrequency);
    
    /**
     * Save a new failure or update existing
     */
    Failure save(Failure failure);
    
    /**
     * Count total failures
     */
    long count();
}
