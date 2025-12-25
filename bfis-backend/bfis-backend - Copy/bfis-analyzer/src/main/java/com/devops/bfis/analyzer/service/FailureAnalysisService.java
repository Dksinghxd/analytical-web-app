package com.devops.bfis.analyzer.service;

import com.devops.bfis.analyzer.repository.FailureRepository;
import com.devops.bfis.core.domain.Failure;
import com.devops.bfis.core.enums.FailureType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing failure patterns and providing insights
 * 
 * Responsibilities:
 * - Pattern matching in error messages
 * - Categorizing failures by type
 * - Tracking failure frequency
 * - Identifying recurring issues
 * 
 * Future enhancement: ML-based pattern detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FailureAnalysisService {
    
    private final FailureRepository failureRepository;
    
    /**
     * Get all failures ordered by frequency
     * 
     * Frontend expects this for failure analysis dashboard
     */
    public List<Failure> getAllFailures() {
        log.debug("Fetching all failures");
        return failureRepository.findAll();
    }
    
    /**
     * Get failures grouped by type with counts
     * 
     * Useful for pie charts and category analysis
     */
    public Map<FailureType, Long> getFailureDistribution() {
        return failureRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Failure::getFailureType,
                        Collectors.counting()
                ));
    }
    
    /**
     * Identify top recurring failures
     * 
     * Returns failures that occur most frequently,
     * sorted by frequency descending
     */
    public List<Failure> getTopRecurringFailures(int limit) {
        log.debug("Fetching top {} recurring failures", limit);
        return failureRepository.findRecurringFailures(2).stream()
                .limit(limit)
                .toList();
    }
    
    /**
     * Analyze error message to categorize failure type
     * 
     * This is a rule-based classifier. In production, consider:
     * - ML-based classification
     * - Configurable regex patterns
     * - Historical pattern learning
     * 
     * @param errorMessage The error message to analyze
     * @return Detected failure type
     */
    public FailureType categorizeFailure(String errorMessage) {
        String lowerMessage = errorMessage.toLowerCase();
        
        // Test-related failures
        if (lowerMessage.contains("test") || 
            lowerMessage.contains("assertion") ||
            lowerMessage.contains("expected") ||
            lowerMessage.contains("junit") ||
            lowerMessage.contains("mockito")) {
            return FailureType.TEST;
        }
        
        // Dependency failures
        if (lowerMessage.contains("dependency") ||
            lowerMessage.contains("package not found") ||
            lowerMessage.contains("maven") ||
            lowerMessage.contains("npm") ||
            lowerMessage.contains("module not found")) {
            return FailureType.DEPENDENCY;
        }
        
        // Docker/container failures
        if (lowerMessage.contains("docker") ||
            lowerMessage.contains("container") ||
            lowerMessage.contains("image") ||
            lowerMessage.contains("dockerfile")) {
            return FailureType.DOCKER;
        }
        
        // Infrastructure failures (default)
        return FailureType.INFRA;
    }
    
    /**
     * Generate human-readable insights about failure patterns
     * 
     * Used by the UI generation endpoint to provide recommendations
     */
    public String generateFailureInsights() {
        List<Failure> allFailures = failureRepository.findAll();
        
        if (allFailures.isEmpty()) {
            return "No failures detected. System is healthy.";
        }
        
        Map<FailureType, Long> distribution = getFailureDistribution();
        Failure topFailure = allFailures.stream()
                .max((f1, f2) -> f1.getFrequencyCount().compareTo(f2.getFrequencyCount()))
                .orElse(null);
        
        StringBuilder insights = new StringBuilder();
        insights.append(String.format("Analyzed %d unique failure patterns.\n", allFailures.size()));
        
        if (topFailure != null) {
            insights.append(String.format(
                    "Most frequent issue: %s failures (occurred %d times).\n",
                    topFailure.getFailureType(),
                    topFailure.getFrequencyCount()
            ));
        }
        
        // Provide actionable recommendations
        distribution.forEach((type, count) -> {
            if (count > 5) {
                insights.append(String.format(
                        "⚠️  High %s failure rate detected (%d occurrences). Consider reviewing:\n",
                        type, count
                ));
                
                switch (type) {
                    case TEST:
                        insights.append("   - Test stability and flakiness\n");
                        insights.append("   - Test data dependencies\n");
                        break;
                    case DEPENDENCY:
                        insights.append("   - Package versions and lock files\n");
                        insights.append("   - Dependency update strategy\n");
                        break;
                    case DOCKER:
                        insights.append("   - Base image versions\n");
                        insights.append("   - Build cache configuration\n");
                        break;
                    case INFRA:
                        insights.append("   - Resource allocation\n");
                        insights.append("   - Network connectivity\n");
                        break;
                }
            }
        });
        
        return insights.toString();
    }
}
