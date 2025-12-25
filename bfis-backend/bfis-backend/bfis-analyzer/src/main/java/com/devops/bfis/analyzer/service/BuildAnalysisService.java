package com.devops.bfis.analyzer.service;

import com.devops.bfis.analyzer.repository.BuildRepository;
import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.domain.Metrics;
import com.devops.bfis.core.enums.BuildStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for analyzing build data and computing metrics
 * 
 * Business logic layer that orchestrates data access and analysis
 * Responsibilities:
 * - Compute dashboard metrics
 * - Analyze build patterns
 * - Calculate failure rates
 * - Identify flaky tests
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BuildAnalysisService {
    
        private final BuildRepository buildRepository;
    
    /**
     * Compute aggregated metrics for dashboard
     * 
     * Algorithm:
     * 1. Count total builds
     * 2. Count failed builds
     * 3. Calculate failure rate percentage
     * 4. Compute average build duration
     * 5. Count flaky tests (builds marked as flaky)
     * 
     * @return Metrics object matching frontend contract
     */
    public Metrics computeMetrics() {
        log.debug("Computing metrics from build data");
        
        List<Build> allBuilds = buildRepository.findAll();
        
        int totalBuilds = allBuilds.size();
        
        long failedCount = buildRepository.countByStatus(BuildStatus.FAILED);
        long flakyCount = buildRepository.countByStatus(BuildStatus.FLAKY);
        
        // Failure rate includes both failed and flaky builds
        double failureRate = totalBuilds > 0 
                ? ((double) (failedCount + flakyCount) / totalBuilds) * 100.0 
                : 0.0;
        
        // Average build time in seconds
        double avgBuildTime = allBuilds.stream()
                .filter(b -> b.getDurationSeconds() != null)
                .mapToInt(Build::getDurationSeconds)
                .average()
                .orElse(0.0);
        
        // Flaky test count (builds that intermittently fail)
        int flakyTestCount = (int) flakyCount;
        
        Metrics metrics = Metrics.builder()
                .totalBuilds(totalBuilds)
                .failureRate(Math.round(failureRate * 10.0) / 10.0) // Round to 1 decimal
                .avgBuildTime(Math.round(avgBuildTime * 10.0) / 10.0)
                .flakyTestCount(flakyTestCount)
                .build();
        
        log.info("Computed metrics: total={}, failureRate={}%, avgTime={}s, flaky={}", 
                totalBuilds, metrics.getFailureRate(), metrics.getAvgBuildTime(), flakyTestCount);
        
        return metrics;
    }
    
    /**
     * Retrieve recent builds for dashboard display
     * 
     * @param limit Maximum number of builds to return
     * @return List of most recent builds
     */
    public List<Build> getRecentBuilds(int limit) {
        log.debug("Fetching {} most recent builds", limit);
        return buildRepository.findAll().stream()
                .limit(limit)
                .toList();
    }
    
    /**
     * Get all builds (used by API endpoint)
     */
    public List<Build> getAllBuilds() {
        return buildRepository.findAll();
    }
}
