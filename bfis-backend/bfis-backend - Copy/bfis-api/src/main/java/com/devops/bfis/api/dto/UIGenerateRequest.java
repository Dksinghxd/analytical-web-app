package com.devops.bfis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for POST /api/ui/generate endpoint
 * 
 * This endpoint triggers backend analysis and returns recommendations.
 * Future enhancement: Could trigger actual UI code generation based on failure patterns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UIGenerateRequest {
    
    /**
     * Optional: specific analysis type to run
     */
    private String analysisType;
    
    /**
     * Optional: time range for analysis
     */
    private String timeRange;
}
