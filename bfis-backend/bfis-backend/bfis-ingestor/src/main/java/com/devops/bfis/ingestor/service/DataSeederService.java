package com.devops.bfis.ingestor.service;

import com.devops.bfis.analyzer.repository.BuildRepository;
import com.devops.bfis.analyzer.repository.FailureRepository;
import com.devops.bfis.analyzer.service.FailureAnalysisService;
import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.domain.Failure;
import com.devops.bfis.core.enums.BuildStatus;
import com.devops.bfis.core.enums.FailureType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Seeds the system with realistic DevOps build data
 * 
 * This service generates mock data that resembles real CI/CD patterns:
 * - Multiple repositories (microservices architecture)
 * - Various failure types with realistic distributions
 * - Time-based patterns (recent builds more frequent)
 * - Flaky test simulation
 * 
 * In production, this would be replaced with:
 * - GitHub Actions webhook ingestion
 * - Real-time log parsing
 * - Historical data import from CI/CD system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService {
    
    private final BuildRepository buildRepository;
    private final FailureRepository failureRepository;
    private final FailureAnalysisService failureAnalysisService;
    
    private static final String[] REPOSITORIES = {
        "payment-service",
        "user-api",
        "notification-worker",
        "frontend-web",
        "analytics-pipeline",
        "auth-service",
        "inventory-service",
        "email-processor"
    };
    
    private static final String[] BRANCHES = {
        "main", "develop", "feature/payment-v2", "hotfix/auth-bug", "feature/new-ui"
    };
    
    private static final String[] ERROR_MESSAGES = {
        "AssertionError: Expected payment status to be 'completed', got 'pending'",
        "NullPointerException in UserService.validateEmail() at line 142",
        "Connection timeout to database after 30s",
        "Docker build failed: base image 'node:18-alpine' not found",
        "Test flaky: intermittent failure in PaymentIntegrationTest",
        "Maven dependency resolution failed: could not resolve com.stripe:stripe-java:22.0.0",
        "Kubernetes deployment timeout: pod 'api-server-xyz' not ready",
        "Memory limit exceeded: container killed (OOMKilled)",
        "npm install failed: network timeout downloading package",
        "JUnit test failed: UserRepositoryTest.testFindByEmail"
    };
    
    /**
     * Initialize data on application startup
     */
    @PostConstruct
    public void seedData() {
        if (buildRepository.count() > 0) {
            log.info("Data already seeded, skipping initialization");
            return;
        }
        
        log.info("Seeding system with realistic DevOps build data...");
        
        Random random = new Random(42); // Fixed seed for reproducibility
        Instant now = Instant.now();
        
        // Generate 150 builds over the past 30 days
        List<Build> builds = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            int daysAgo = random.nextInt(30);
            int hoursAgo = random.nextInt(24);
            Instant triggeredAt = now.minus(daysAgo, ChronoUnit.DAYS)
                                     .minus(hoursAgo, ChronoUnit.HOURS);
            
            BuildStatus status = determineStatus(random);
            
            Build build = Build.builder()
                    .id(UUID.randomUUID().toString())
                    .repositoryName(REPOSITORIES[random.nextInt(REPOSITORIES.length)])
                    .branch(BRANCHES[random.nextInt(BRANCHES.length)])
                    .status(status)
                    .durationSeconds(generateDuration(status, random))
                    .triggeredAt(triggeredAt)
                    .commitHash(generateCommitHash(random))
                    .build();
            
            builds.add(buildRepository.save(build));
            
            // Create failures for failed/flaky builds
            if (build.isFailed()) {
                createFailuresForBuild(build, random);
            }
        }
        
        log.info("Successfully seeded {} builds and {} failures", 
                buildRepository.count(), failureRepository.count());
        log.info("Sample repositories: {}", String.join(", ", REPOSITORIES));
    }
    
    /**
     * Determine build status with realistic distribution
     * 
     * Distribution:
     * - 70% success
     * - 20% failed
     * - 10% flaky
     */
    private BuildStatus determineStatus(Random random) {
        int roll = random.nextInt(100);
        if (roll < 70) return BuildStatus.SUCCESS;
        if (roll < 90) return BuildStatus.FAILED;
        return BuildStatus.FLAKY;
    }
    
    /**
     * Generate realistic build duration
     * 
     * - Success: 120-600 seconds (2-10 minutes)
     * - Failed: 60-300 seconds (fails earlier)
     * - Flaky: 150-650 seconds (similar to success but variable)
     */
    private int generateDuration(BuildStatus status, Random random) {
        return switch (status) {
            case SUCCESS -> 120 + random.nextInt(480);
            case FAILED -> 60 + random.nextInt(240);
            case FLAKY -> 150 + random.nextInt(500);
        };
    }
    
    /**
     * Generate realistic git commit hash
     */
    private String generateCommitHash(Random random) {
        String chars = "0123456789abcdef";
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }
        return hash.toString();
    }
    
    /**
     * Create failures for a failed build
     * 
     * Most builds have 1 failure, some have multiple
     */
    private void createFailuresForBuild(Build build, Random random) {
        int failureCount = random.nextInt(3) + 1; // 1-3 failures per build
        
        for (int i = 0; i < failureCount; i++) {
            String errorMessage = ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)];
            FailureType type = failureAnalysisService.categorizeFailure(errorMessage);
            
            // Simulate recurring failures
            int frequency = random.nextInt(10) + 1;
            Instant firstSeen = build.getTriggeredAt().minus(frequency * 2L, ChronoUnit.DAYS);
            
            Failure failure = Failure.builder()
                    .id(UUID.randomUUID().toString())
                    .buildId(build.getId())
                    .failureType(type)
                    .errorMessage(errorMessage)
                    .frequencyCount(frequency)
                    .firstSeenAt(firstSeen)
                    .lastSeenAt(build.getTriggeredAt())
                    .build();
            
            failureRepository.save(failure);
        }
    }
}
