package com.devops.bfis.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated metrics for dashboard cards
 * Contract matched to frontend expectations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metrics {
    
    /**
     * Total number of builds executed
     */
    private Integer totalBuilds;
    
    /**
     * Percentage of builds that failed (0-100)
     */
    private Double failureRate;
    
    /**
     * Average build duration in seconds
     */
    private Double avgBuildTime;
    
    /**
     * Number of tests exhibiting flaky behavior
     */
    private Integer flakyTestCount;
    
    /**
     * Helper method to get failure rate as percentage string
     */
    public String getFailureRatePercentage() {
        return String.format("%.1f%%", failureRate);
    }
    
    /**
     * Helper method to format average build time
     */
    public String getFormattedAvgBuildTime() {
        if (avgBuildTime == null) return "N/A";
        int minutes = (int) (avgBuildTime / 60);
        int seconds = (int) (avgBuildTime % 60);
        return String.format("%dm %ds", minutes, seconds);
    }
}
