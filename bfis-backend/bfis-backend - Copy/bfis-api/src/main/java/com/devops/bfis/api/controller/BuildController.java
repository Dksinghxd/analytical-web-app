package com.devops.bfis.api.controller;

import com.devops.bfis.analyzer.service.BuildAnalysisService;
import com.devops.bfis.core.domain.Build;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for build-related endpoints
 * 
 * Endpoint: GET /api/builds
 * Purpose: Provide recent build data for dashboard display
 * 
 * Contract: Must return build objects matching frontend expectations
 */
@RestController
@RequestMapping("/api/builds")
@RequiredArgsConstructor
@Slf4j
public class BuildController {
    
    private final BuildAnalysisService buildAnalysisService;
    
    /**
     * GET /api/builds
     * 
     * Returns list of recent CI/CD builds
     * 
     * Response format:
     * [
     *   {
     *     "id": "uuid",
     *     "repositoryName": "payment-service",
     *     "branch": "main",
     *     "status": "success" | "failed" | "flaky",
     *     "durationSeconds": 245,
     *     "triggeredAt": "2024-01-15T10:30:00.000Z",
     *     "commitHash": "a3f2c1d"
     *   }
     * ]
     * 
     * @return List of Build objects
     */
    @GetMapping
    public ResponseEntity<List<Build>> getBuilds() {
        log.info("GET /api/builds - Fetching build data");
        
        List<Build> builds = buildAnalysisService.getAllBuilds();
        
        log.info("Returning {} builds", builds.size());
        return ResponseEntity.ok(builds);
    }
}
