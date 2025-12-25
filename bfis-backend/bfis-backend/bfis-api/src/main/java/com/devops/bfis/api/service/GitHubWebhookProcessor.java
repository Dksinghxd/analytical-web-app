package com.devops.bfis.api.service;

import com.devops.bfis.api.dto.GitHubWorkflowRunEvent;
import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.domain.Failure;
import com.devops.bfis.core.enums.BuildStatus;
import com.devops.bfis.core.enums.FailureType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/**
 * Service to convert GitHub webhook events into BFIS domain models
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubWebhookProcessor {
    private final InMemoryDataStore dataStore;
    private final TrackedRepositoryStore repoStore;
    
    /**
     * Process workflow_run completed event
     * Converts GitHub event data into Build and optionally Failure
     */
    public void processWorkflowRunCompleted(GitHubWorkflowRunEvent event) {
        if (!"completed".equals(event.getAction())) {
            log.debug("Ignoring non-completed workflow_run event: {}", event.getAction());
            return;
        }
        
        GitHubWorkflowRunEvent.WorkflowRun run = event.getWorkflowRun();
        GitHubWorkflowRunEvent.Repository repo = event.getRepository();
        
        // Verify repository is tracked
        String[] parts = repo.getFullName().split("/");
        if (parts.length != 2 || !repoStore.isRegistered(parts[0], parts[1])) {
            log.warn("Received webhook for untracked repository: {}", repo.getFullName());
            return;
        }
        
        // Map GitHub conclusion to BFIS BuildStatus
        BuildStatus status = mapConclusion(run.getConclusion());
        
        // Calculate duration
        int durationSeconds = calculateDuration(run.getCreatedAt(), run.getUpdatedAt());
        
        // Parse timestamp
        Instant triggeredAt = parseTimestamp(run.getCreatedAt());
        
        // Create Build
        Build build = Build.builder()
                .id(String.valueOf(run.getId()))
                .repositoryName(repo.getFullName())
                .branch(run.getHeadBranch())
                .status(status)
                .durationSeconds(durationSeconds)
                .triggeredAt(triggeredAt)
                .commitHash(run.getHeadSha())
                .build();
        
        dataStore.addBuild(build);
        log.info("Ingested build from GitHub: repo={}, status={}, duration={}s", 
                repo.getFullName(), status, durationSeconds);
        
        // If failed, create Failure
        if (status == BuildStatus.FAILED) {
            Failure failure = Failure.builder()
                    .id(UUID.randomUUID().toString())
                    .buildId(build.getId())
                    .failureType(FailureType.TEST) // Default, could be enhanced
                    .errorMessage("GitHub workflow failed: " + run.getName())
                    .frequencyCount(1)
                    .firstSeenAt(triggeredAt)
                    .lastSeenAt(triggeredAt)
                    .build();
            dataStore.addFailure(failure);
        }
    }
    
    private BuildStatus mapConclusion(String conclusion) {
        if (conclusion == null) return BuildStatus.FAILED;
        switch (conclusion.toLowerCase()) {
            case "success":
                return BuildStatus.SUCCESS;
            case "failure":
                return BuildStatus.FAILED;
            case "cancelled":
            case "skipped":
                return BuildStatus.FAILED; // Treat as failed for metrics
            default:
                return BuildStatus.FAILED;
        }
    }
    
    private int calculateDuration(String createdAt, String updatedAt) {
        try {
            Instant start = Instant.parse(createdAt);
            Instant end = Instant.parse(updatedAt);
            return (int) Duration.between(start, end).getSeconds();
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse timestamps for duration calculation", e);
            return 0;
        }
    }
    
    private Instant parseTimestamp(String timestamp) {
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse timestamp: {}", timestamp);
            return Instant.now();
        }
    }
}
