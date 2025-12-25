package com.devops.bfis.analyzer.repository;

import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.enums.BuildStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of BuildRepository
 * 
 * Uses ConcurrentHashMap for thread-safety
 * In production, this would be replaced with JPA/MongoDB repository
 */
@Repository
public class InMemoryBuildRepository implements BuildRepository {
    
    private final Map<String, Build> buildStore = new ConcurrentHashMap<>();
    
    @Override
    public List<Build> findAll() {
        return buildStore.values().stream()
                .sorted(Comparator.comparing(Build::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Build> findByTimeRange(Instant start, Instant end) {
        return buildStore.values().stream()
                .filter(b -> !b.getTriggeredAt().isBefore(start) && !b.getTriggeredAt().isAfter(end))
                .sorted(Comparator.comparing(Build::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Build> findByStatus(BuildStatus status) {
        return buildStore.values().stream()
                .filter(b -> b.getStatus() == status)
                .sorted(Comparator.comparing(Build::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Build> findByRepository(String repositoryName) {
        return buildStore.values().stream()
                .filter(b -> b.getRepositoryName().equalsIgnoreCase(repositoryName))
                .sorted(Comparator.comparing(Build::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Build> findById(String id) {
        return Optional.ofNullable(buildStore.get(id));
    }
    
    @Override
    public Build save(Build build) {
        if (build.getId() == null) {
            build.setId(UUID.randomUUID().toString());
        }
        buildStore.put(build.getId(), build);
        return build;
    }
    
    @Override
    public long count() {
        return buildStore.size();
    }
    
    @Override
    public long countByStatus(BuildStatus status) {
        return buildStore.values().stream()
                .filter(b -> b.getStatus() == status)
                .count();
    }
}
