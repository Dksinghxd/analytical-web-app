package com.devops.bfis.api.controller;

import com.devops.bfis.analyzer.service.FailureAnalysisService;
import com.devops.bfis.api.dto.UIGenerateRequest;
import com.devops.bfis.api.dto.UIGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for UI generation and recommendations
 * 
 * Endpoint: POST /api/ui/generate
 * Purpose: Trigger backend analysis and return recommendations
 * 
 * Future enhancement: Could use AI to generate actual UI components
 * based on failure patterns and dashboard needs
 */
@RestController
@RequestMapping("/api/ui")
@RequiredArgsConstructor
@Slf4j
public class UIController {
    
    private final FailureAnalysisService failureAnalysisService;
    
    /**
     * POST /api/ui/generate
     * 
     * Triggers analysis logic and returns recommendations
     * 
     * Request body (optional):
     * {
     *   "analysisType": "failures",
     *   "timeRange": "30d"
     * }
     * 
     * Response format:
     * {
     *   "status": "success",
     *   "explanation": "Analyzed 45 failure patterns...",
     *   "recommendations": "Consider focusing on test stability..."
     * }
     * 
     * @param request Optional analysis parameters
     * @return UIGenerateResponse with status and insights
     */
    @PostMapping("/generate")
    public ResponseEntity<UIGenerateResponse> generateUI(@RequestBody(required = false) UIGenerateRequest request) {
        log.info("POST /api/ui/generate - Generating UI recommendations");
        
        try {
            // Run failure analysis
            String insights = failureAnalysisService.generateFailureInsights();
            
            // Build response
            UIGenerateResponse response = UIGenerateResponse.builder()
                    .status("success")
                    .explanation("Backend analysis completed successfully. Analyzed recent build failures and patterns.")
                    .recommendations(insights)
                    .build();
            
            log.info("Successfully generated recommendations");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating UI recommendations", e);
            
            UIGenerateResponse errorResponse = UIGenerateResponse.builder()
                    .status("failed")
                    .explanation("Failed to generate recommendations: " + e.getMessage())
                    .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
