package com.devops.bfis.api.controller;

import com.devops.bfis.api.service.IngestService;
import com.devops.bfis.api.dto.IngestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
public class IngestController {
    private final IngestService ingestService;

    @Autowired
    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping
    public ResponseEntity<String> ingestBuild(@RequestBody IngestRequest request) {
        boolean accepted = ingestService.ingestBuild(request);
        if (!accepted) {
            return ResponseEntity.badRequest().body("Repository not registered in BFIS. Register first via /api/repos.");
        }
        return ResponseEntity.ok("Build ingested");
    }
}
