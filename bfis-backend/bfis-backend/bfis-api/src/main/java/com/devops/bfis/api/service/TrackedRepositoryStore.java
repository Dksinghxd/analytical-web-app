package com.devops.bfis.api.service;

import com.devops.bfis.core.domain.TrackedRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrackedRepositoryStore {
    private final Map<String, TrackedRepository> repoById = new ConcurrentHashMap<>();
    private final Map<String, TrackedRepository> repoByName = new ConcurrentHashMap<>(); // key: owner/repoName

    private String key(String owner, String repoName) {
        String o = owner == null ? "" : owner.trim();
        String r = repoName == null ? "" : repoName.trim();
        return (o + "/" + r).toLowerCase(Locale.ROOT);
    }

    public TrackedRepository register(String owner, String repoName, String defaultBranch) {
        String key = key(owner, repoName);
        TrackedRepository existing = repoByName.get(key);
        if (existing != null) {
            return existing;
        }
        String id = UUID.randomUUID().toString();
        TrackedRepository repo = TrackedRepository.builder()
                .id(id)
                .owner(owner)
                .repoName(repoName)
                .defaultBranch(defaultBranch)
                .createdAt(Instant.now())
                .build();
        repoById.put(id, repo);
        repoByName.put(key, repo);
        return repo;
    }

    public List<TrackedRepository> getAll() {
        return new ArrayList<>(repoById.values());
    }

    public Optional<TrackedRepository> findByName(String owner, String repoName) {
        return Optional.ofNullable(repoByName.get(key(owner, repoName)));
    }

    public boolean isRegistered(String owner, String repoName) {
        return repoByName.containsKey(key(owner, repoName));
    }
}
