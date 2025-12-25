package com.devops.bfis.core.domain;

import com.devops.bfis.core.enums.FailureType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain model representing a build failure with analysis data
 * Contract matched to frontend expectations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Failure {
    
    /**
     * Unique failure identifier
     */
    private String id;
    
    /**
     * Associated build ID
     */
    private String buildId;
    
    /**
     * Categorized failure type: test | dependency | docker | infra
     */
    private FailureType failureType;
    
    /**
     * Error message extracted from logs
     */
    private String errorMessage;
    
    /**
     * How many times this failure pattern has occurred
     */
    private Integer frequencyCount;
    
    /**
     * First time this failure pattern was observed
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant firstSeenAt;
    
    /**
     * Most recent occurrence of this failure pattern
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant lastSeenAt;
    
    /**
     * Helper to check if this is a recurring issue
     */
    public boolean isRecurring() {
        return frequencyCount != null && frequencyCount > 3;
    }
    
    /**
     * Calculate failure severity based on frequency
     */
    public String getSeverity() {
        if (frequencyCount == null) return "LOW";
        if (frequencyCount >= 10) return "CRITICAL";
        if (frequencyCount >= 5) return "HIGH";
        if (frequencyCount >= 3) return "MEDIUM";
        return "LOW";
    }
}
