package com.devops.bfis.api.controller;

import com.devops.bfis.analyzer.service.FailureAnalysisService;
import com.devops.bfis.core.domain.Failure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for failure analysis endpoints
 * 
 * Endpoint: GET /api/failures
 * Purpose: Provide failure pattern data with frequency analysis
 * 
 * Contract: Must return failure objects matching frontend expectations
 */
@RestController
@RequestMapping("/api/failures")
@RequiredArgsConstructor
@Slf4j
public class FailureController {
    
    private final FailureAnalysisService failureAnalysisService;
    
    /**
     * GET /api/failures
     * 
     * Returns analyzed failure data ordered by frequency
     * 
     * Response format:
     * [
     *   {
     *     "id": "uuid",
     *     "buildId": "build-uuid",
     *     "failureType": "test" | "dependency" | "docker" | "infra",
     *     "errorMessage": "AssertionError: ...",
     *     "frequencyCount": 12,
     *     "firstSeenAt": "2024-01-01T08:00:00.000Z",
     *     "lastSeenAt": "2024-01-15T14:30:00.000Z"
     *   }
     * ]
     * 
     * @return List of Failure objects sorted by frequency (descending)
     */
    @GetMapping
    public ResponseEntity<List<Failure>> getFailures() {
        log.info("GET /api/failures - Fetching failure analysis data");
        
        List<Failure> failures = failureAnalysisService.getAllFailures();
        
        log.info("Returning {} failure patterns", failures.size());
        return ResponseEntity.ok(failures);
    }
}
