package com.devops.bfis.api.controller;

import com.devops.bfis.analyzer.service.BuildAnalysisService;
import com.devops.bfis.core.domain.Metrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for dashboard metrics
 * 
 * Endpoint: GET /api/metrics
 * Purpose: Provide aggregated metrics for dashboard cards
 * 
 * Contract: Must return metrics object matching frontend expectations
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {
    
    private final BuildAnalysisService buildAnalysisService;
    
    /**
     * GET /api/metrics
     * 
     * Returns aggregated build and failure metrics
     * 
     * Response format:
     * {
     *   "totalBuilds": 150,
     *   "failureRate": 25.3,
     *   "avgBuildTime": 245.7,
     *   "flakyTestCount": 8
     * }
     * 
     * These metrics populate the dashboard card KPIs
     * 
     * @return Metrics object with computed values
     */
    @GetMapping
    public ResponseEntity<Metrics> getMetrics() {
        log.info("GET /api/metrics - Computing dashboard metrics");
        
        Metrics metrics = buildAnalysisService.computeMetrics();
        
        log.info("Returning metrics: totalBuilds={}, failureRate={}%, avgBuildTime={}s, flakyTests={}",
                metrics.getTotalBuilds(),
                metrics.getFailureRate(),
                metrics.getAvgBuildTime(),
                metrics.getFlakyTestCount());
        
        return ResponseEntity.ok(metrics);
    }
}
