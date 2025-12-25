package com.devops.bfis.api.service;


import com.devops.bfis.api.dto.IngestRequest;
import com.devops.bfis.core.domain.Build;
import com.devops.bfis.core.domain.Failure;
import com.devops.bfis.core.enums.BuildStatus;
import com.devops.bfis.core.enums.FailureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.devops.bfis.api.service.TrackedRepositoryStore;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IngestService {
    private final InMemoryDataStore dataStore;
    private final TrackedRepositoryStore repoStore;
    private final AtomicInteger buildIdCounter = new AtomicInteger(1000);

    @Autowired
    public IngestService(InMemoryDataStore dataStore, TrackedRepositoryStore repoStore) {
        this.dataStore = dataStore;
        this.repoStore = repoStore;
    }

    public boolean ingestBuild(IngestRequest req) {
        // Parse owner/repo from repositoryName (format: owner/repo)
        String[] parts = req.repositoryName != null ? req.repositoryName.split("/") : new String[0];
        if (parts.length != 2 || !repoStore.isRegistered(parts[0], parts[1])) {
            return false;
        }
        String buildId = String.valueOf(buildIdCounter.incrementAndGet());
        BuildStatus status = parseStatus(req.status);
        FailureType failureType = parseFailureType(req.failureType);
        Instant triggeredAt = parseInstant(req.triggeredAt);

        Build build = Build.builder()
            .id(buildId)
            .repositoryName(req.repositoryName)
            .branch(req.branch)
            .status(status)
            .durationSeconds(req.durationSeconds)
            .triggeredAt(triggeredAt)
            .commitHash(req.commitHash)
            .build();
        dataStore.addBuild(build);

        if (status == BuildStatus.FAILED || status == BuildStatus.FLAKY) {
            Failure failure = Failure.builder()
                .id(UUID.randomUUID().toString())
                .buildId(buildId)
                .failureType(failureType)
                .errorMessage(status + ": " + failureType)
                .frequencyCount(1)
                .firstSeenAt(triggeredAt)
                .lastSeenAt(triggeredAt)
                .build();
            dataStore.addFailure(failure);
        }
        return true;
    }

    public List<Build> getBuilds() {
        return dataStore.getBuilds();
    }

    public List<Failure> getFailures() {
        return dataStore.getFailures();
    }

    public Map<String, Object> getMetrics() {
        List<Build> builds = dataStore.getBuilds();
        int totalBuilds = builds.size();
        int failed = (int) builds.stream().filter(b -> b.getStatus() == BuildStatus.FAILED).count();
        int flaky = (int) builds.stream().filter(b -> b.getStatus() == BuildStatus.FLAKY).count();
        double avgBuildTime = builds.stream().mapToInt(b -> b.getDurationSeconds() != null ? b.getDurationSeconds() : 0).average().orElse(0);
        double failureRate = totalBuilds > 0 ? ((double) (failed + flaky) / totalBuilds) * 100 : 0;
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalBuilds", totalBuilds);
        metrics.put("failureRate", failureRate);
        metrics.put("avgBuildTime", avgBuildTime);
        metrics.put("flakyTestCount", flaky);
        return metrics;
    }

    private BuildStatus parseStatus(String status) {
        if (status == null) return BuildStatus.FAILED;
        switch (status.toLowerCase()) {
            case "success": return BuildStatus.SUCCESS;
            case "flaky": return BuildStatus.FLAKY;
            default: return BuildStatus.FAILED;
        }
    }

    private FailureType parseFailureType(String type) {
        if (type == null) return FailureType.TEST;
        switch (type.toLowerCase()) {
            case "dependency": return FailureType.DEPENDENCY;
            case "docker": return FailureType.DOCKER;
            case "infra": return FailureType.INFRA;
            default: return FailureType.TEST;
        }
    }

    private Instant parseInstant(String iso) {
        try {
            return Instant.parse(iso);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
