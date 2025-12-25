package com.devops.bfis.api.controller;

import com.devops.bfis.api.dto.RegisterRepositoryRequest;
import com.devops.bfis.api.service.TrackedRepositoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.devops.bfis.core.domain.TrackedRepository;

@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepositoryController {
    private final TrackedRepositoryStore repoStore;

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody RegisterRepositoryRequest req) {
        repoStore.register(req.getOwner(), req.getRepoName(), req.getDefaultBranch());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TrackedRepository>> list() {
        return ResponseEntity.ok(repoStore.getAll());
    }
}
