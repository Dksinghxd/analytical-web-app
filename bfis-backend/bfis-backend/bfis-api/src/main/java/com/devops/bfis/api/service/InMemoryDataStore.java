package com.devops.bfis.api.service;

import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.domain.Failure;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryDataStore {
    // Key: repositoryName (owner/repo), Value: List of builds/failures
    private final Map<String, List<Build>> buildsByRepo = new HashMap<>();
    private final Map<String, List<Failure>> failuresByRepo = new HashMap<>();

    public List<Build> getBuilds() {
        List<Build> all = new ArrayList<>();
        for (List<Build> builds : buildsByRepo.values()) {
            all.addAll(builds);
        }
        return all;
    }

    public List<Failure> getFailures() {
        List<Failure> all = new ArrayList<>();
        for (List<Failure> failures : failuresByRepo.values()) {
            all.addAll(failures);
        }
        return all;
    }

    public void addBuild(Build build) {
        String repo = build.getRepositoryName();
        buildsByRepo.computeIfAbsent(repo, k -> new ArrayList<>()).add(build);
    }

    public void addFailure(Failure failure) {
        // Find repo from buildId if needed, or require Failure to have repositoryName if you extend Failure
        // For now, assume buildId is unique and not used for repo lookup
        // This demo stores by a dummy repo key, but you can extend Failure to include repositoryName for real use
        failuresByRepo.computeIfAbsent("all", k -> new ArrayList<>()).add(failure);
    }

    public void clear() {
        buildsByRepo.clear();
        failuresByRepo.clear();
    }

    // Optionally, add per-repo accessors
    public List<Build> getBuildsForRepo(String repositoryName) {
        return buildsByRepo.getOrDefault(repositoryName, Collections.emptyList());
    }

    public List<Failure> getFailuresForRepo(String repositoryName) {
        return failuresByRepo.getOrDefault(repositoryName, Collections.emptyList());
    }
}
