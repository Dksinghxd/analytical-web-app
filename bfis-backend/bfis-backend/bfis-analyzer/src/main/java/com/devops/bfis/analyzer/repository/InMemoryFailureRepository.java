package com.devops.bfis.analyzer.repository;

import com.devops.bfis.core.domain.Failure;
import com.devops.bfis.core.enums.FailureType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of FailureRepository
 * 
 * Thread-safe storage for failure analysis data
 * Production would use indexed database queries
 */
@Repository
public class InMemoryFailureRepository implements FailureRepository {
    
    private final Map<String, Failure> failureStore = new ConcurrentHashMap<>();
    
    @Override
    public List<Failure> findAll() {
        return failureStore.values().stream()
                .sorted(Comparator.comparing(Failure::getFrequencyCount).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Failure> findByType(FailureType type) {
        return failureStore.values().stream()
                .filter(f -> f.getFailureType() == type)
                .sorted(Comparator.comparing(Failure::getFrequencyCount).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Failure> findByBuildId(String buildId) {
        return failureStore.values().stream()
                .filter(f -> f.getBuildId().equals(buildId))
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Failure> findById(String id) {
        return Optional.ofNullable(failureStore.get(id));
    }
    
    @Override
    public List<Failure> findRecurringFailures(int minFrequency) {
        return failureStore.values().stream()
                .filter(f -> f.getFrequencyCount() >= minFrequency)
                .sorted(Comparator.comparing(Failure::getFrequencyCount).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public Failure save(Failure failure) {
        if (failure.getId() == null) {
            failure.setId(UUID.randomUUID().toString());
        }
        failureStore.put(failure.getId(), failure);
        return failure;
    }
    
    @Override
    public long count() {
        return failureStore.size();
    }
}
