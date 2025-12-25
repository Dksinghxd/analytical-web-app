package com.devops.bfis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for POST /api/ui/generate endpoint
 * 
 * Returns status and AI-generated recommendations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UIGenerateResponse {
    
    /**
     * Status of the generation request: "success" | "processing" | "failed"
     */
    private String status;
    
    /**
     * Human-readable explanation of analysis and recommendations
     */
    private String explanation;
    
    /**
     * Optional: Generated recommendations or insights
     */
    private String recommendations;
}
