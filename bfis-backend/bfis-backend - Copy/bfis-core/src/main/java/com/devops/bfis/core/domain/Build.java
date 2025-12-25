package com.devops.bfis.core.domain;

import com.devops.bfis.core.enums.BuildStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing a CI/CD build execution
 * Contract matched to frontend expectations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Build {
    
    /**
     * Unique build identifier
     */
    private String id;
    
    /**
     * Repository name (e.g., "payment-service", "user-api")
     */
    private String repositoryName;
    
    /**
     * Git branch that triggered the build
     */
    private String branch;
    
    /**
     * Build execution status: success | failed | flaky
     */
    private BuildStatus status;
    
    /**
     * Build duration in seconds
     */
    private Integer durationSeconds;
    
    /**
     * When the build was triggered (ISO 8601 format)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant triggeredAt;
    
    /**
     * Git commit hash that triggered the build
     */
    private String commitHash;
    
    /**
     * Helper method to check if build failed
     */
    public boolean isFailed() {
        return status == BuildStatus.FAILED || status == BuildStatus.FLAKY;
    }
    
    /**
     * Helper method to check if build is flaky
     */
    public boolean isFlaky() {
        return status == BuildStatus.FLAKY;
    }
}
