package com.devops.bfis.api.service;

import com.devops.bfis.api.dto.IngestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubActionsIngestService {
    private static final String GITHUB_API_BASE = "https://api.github.com";

    private final GitHubInstallationTokenService tokenService;
    private final GitHubInstallationStore installationStore;
    private final TrackedRepositoryStore repoStore;
    private final GitHubRepositoryService repositoryService;
    private final IngestService ingestService;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getRecentFailureReasons(String owner, String repo, int perPage) {
        Map<String, Object> result = new HashMap<>();
        result.put("owner", owner);
        result.put("repo", repo);
        result.put("requested", perPage);

        String installationId = installationStore.getLatestInstallationId();
        if (installationId == null) {
            result.put("error", "No GitHub installation id available.");
            return result;
        }
        String accessToken = tokenService.getInstallationAccessToken(installationId);

        String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/actions/runs?per_page=" + Math.max(1, Math.min(perPage, 100));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            result.put("error", "GitHub API request failed: " + response.getStatusCode());
            return result;
        }

        Object runsObj = response.getBody().get("workflow_runs");
        if (!(runsObj instanceof List<?> runs)) {
            result.put("error", "Unexpected GitHub API response: workflow_runs missing");
            return result;
        }

        List<Map<String, Object>> failures = new java.util.ArrayList<>();
        Map<String, Integer> reasonCounts = new HashMap<>();

        for (Object runObj : runs) {
            if (!(runObj instanceof Map<?, ?> run)) {
                continue;
            }

            String conclusion = asString(run.get("conclusion"));
            if (conclusion == null || conclusion.isBlank() || "success".equalsIgnoreCase(conclusion)) {
                continue;
            }

            String runId = run.get("id") != null ? String.valueOf(run.get("id")) : null;
            String runName = asString(run.get("name"));
            String htmlUrl = asString(run.get("html_url"));
            String headBranch = asString(run.get("head_branch"));
            String headSha = asString(run.get("head_sha"));
            String createdAt = asString(run.get("created_at"));

            Map<String, Object> item = new HashMap<>();
            item.put("runId", runId);
            item.put("workflowName", runName);
            item.put("conclusion", conclusion);
            item.put("htmlUrl", htmlUrl);
            item.put("headBranch", headBranch);
            item.put("headSha", headSha);
            item.put("createdAt", createdAt);

            if (runId != null) {
                FailureDetails details = fetchFailureDetailsFromJobs(owner, repo, accessToken, runId, conclusion);
                item.put("failureType", details.failureType);
                item.put("failedJob", details.failedJob);
                item.put("failedStep", details.failedStep);
                item.put("reason", details.reason);
                if (details.failureType != null) {
                    reasonCounts.put(details.failureType, reasonCounts.getOrDefault(details.failureType, 0) + 1);
                }
            }

            failures.add(item);
        }

        result.put("failedRuns", failures);
        result.put("reasons", reasonCounts);
        result.put("count", failures.size());
        return result;
    }

    private FailureDetails fetchFailureDetailsFromJobs(String owner, String repo, String accessToken, String runId, String conclusion) {
        try {
            String jobsUrl = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/actions/runs/" + runId + "/jobs?per_page=100";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            ResponseEntity<Map> jobsResp = restTemplate.exchange(jobsUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (jobsResp.getStatusCode() != HttpStatus.OK || jobsResp.getBody() == null) {
                return FailureDetails.fallback(conclusion);
            }

            Object jobsObj = jobsResp.getBody().get("jobs");
            if (!(jobsObj instanceof List<?> jobs)) {
                return FailureDetails.fallback(conclusion);
            }

            for (Object jobObj : jobs) {
                if (!(jobObj instanceof Map<?, ?> job)) continue;
                String jobName = asString(job.get("name"));
                String jobConclusion = asString(job.get("conclusion"));

                Object stepsObj = job.get("steps");
                if (stepsObj instanceof List<?> steps) {
                    for (Object stepObj : steps) {
                        if (!(stepObj instanceof Map<?, ?> step)) continue;
                        String stepConclusion = asString(step.get("conclusion"));
                        String stepStatus = asString(step.get("status"));

                        // Prefer any explicit non-success conclusion
                        if (stepConclusion == null || stepConclusion.isBlank()) {
                            continue;
                        }
                        if ("success".equalsIgnoreCase(stepConclusion) || "skipped".equalsIgnoreCase(stepConclusion) || "neutral".equalsIgnoreCase(stepConclusion)) {
                            continue;
                        }

                        String stepName = asString(step.get("name"));
                        String failureType = inferFailureTypeFromStepName(stepConclusion, stepName);
                        String reason;
                        if (stepName != null && !stepName.isBlank()) {
                            reason = jobName != null ? jobName + " :: " + stepName : stepName;
                        } else if (jobName != null) {
                            reason = jobName;
                        } else {
                            reason = "Workflow failed";
                        }
                        if (stepStatus != null && !stepStatus.isBlank() && stepConclusion != null && !stepConclusion.isBlank()) {
                            reason = reason + " (" + stepStatus + "/" + stepConclusion + ")";
                        }

                        return new FailureDetails(failureType, jobName, stepName, reason);
                    }
                }

                // No step-level conclusion captured; fall back to job-level failure
                if (jobConclusion != null && !jobConclusion.isBlank()
                        && !"success".equalsIgnoreCase(jobConclusion)
                        && !"skipped".equalsIgnoreCase(jobConclusion)
                        && !"neutral".equalsIgnoreCase(jobConclusion)) {
                    String failureType = inferFailureTypeFromStepName(jobConclusion, jobName);
                    String reason = jobName != null ? (jobName + " (job conclusion: " + jobConclusion + ")") : ("Job failed (" + jobConclusion + ")");
                    return new FailureDetails(failureType, jobName, null, reason);
                }
            }

            // No failed step found (can happen if job was cancelled/timeout)
            return FailureDetails.fallback(conclusion);
        } catch (Exception e) {
            log.debug("Failed to fetch failure details from jobs endpoint", e);
            return FailureDetails.fallback(conclusion);
        }
    }

    private String inferFailureTypeFromStepName(String conclusion, String stepName) {
        if (conclusion != null) {
            String c = conclusion.toLowerCase(Locale.ROOT);
            if (c.contains("timed_out") || c.contains("cancelled") || c.contains("startup_failure")) return "infra";
        }
        if (stepName == null) return "infra";
        String n = stepName.toLowerCase(Locale.ROOT);
        if (n.contains("docker")) return "docker";
        if (n.contains("depend") || n.contains("install") || n.contains("npm") || n.contains("pnpm") || n.contains("mvn") || n.contains("maven") || n.contains("gradle")) return "dependency";
        if (n.contains("test")) return "test";
        return "infra";
    }

    private record FailureDetails(String failureType, String failedJob, String failedStep, String reason) {
        static FailureDetails fallback(String conclusion) {
            String reason = conclusion != null ? ("Workflow conclusion: " + conclusion) : "Workflow failed";
            return new FailureDetails("infra", null, null, reason);
        }
    }

    public Map<String, Object> ingestRecentWorkflowRuns(String owner, String repo, int perPage) {
        Map<String, Object> result = new HashMap<>();
        result.put("owner", owner);
        result.put("repo", repo);
        result.put("requested", perPage);

        // Ensure repo is registered locally (sync if needed)
        if (!repoStore.isRegistered(owner, repo)) {
            repositoryService.syncRepositories();
        }
        if (!repoStore.isRegistered(owner, repo)) {
            result.put("ingested", 0);
            result.put("skipped", 0);
            result.put("error", "Repository not registered. Call /api/github/sync first (or install app for this repo)." );
            return result;
        }

        String installationId = installationStore.getLatestInstallationId();
        if (installationId == null) {
            result.put("ingested", 0);
            result.put("skipped", 0);
            result.put("error", "No GitHub installation id available." );
            return result;
        }

        String accessToken = tokenService.getInstallationAccessToken(installationId);

        String url = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/actions/runs?per_page=" + Math.max(1, Math.min(perPage, 100));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            result.put("ingested", 0);
            result.put("skipped", 0);
            result.put("error", "GitHub API request failed: " + response.getStatusCode());
            return result;
        }

        Object runsObj = response.getBody().get("workflow_runs");
        if (!(runsObj instanceof List<?> runs)) {
            result.put("ingested", 0);
            result.put("skipped", 0);
            result.put("error", "Unexpected GitHub API response: workflow_runs missing");
            return result;
        }

        int ingested = 0;
        int skipped = 0;
        Map<String, Integer> reasonCounts = new HashMap<>();

        for (Object runObj : runs) {
            if (!(runObj instanceof Map<?, ?> run)) {
                skipped++;
                continue;
            }

            String headBranch = asString(run.get("head_branch"));
            String headSha = asString(run.get("head_sha"));
            String runStartedAt = asString(run.get("run_started_at"));
            String createdAt = asString(run.get("created_at"));
            String updatedAt = asString(run.get("updated_at"));
            String conclusion = asString(run.get("conclusion"));

            // Only ingest completed runs (conclusion present)
            if (conclusion == null || conclusion.isBlank()) {
                skipped++;
                continue;
            }

            String status = "failed";
            if ("success".equalsIgnoreCase(conclusion) || "neutral".equalsIgnoreCase(conclusion) || "skipped".equalsIgnoreCase(conclusion)) {
                status = "success";
            }

            int durationSeconds = computeDurationSeconds(runStartedAt, createdAt, updatedAt);
            String triggeredAt = firstNonBlank(runStartedAt, createdAt);

            String failureType = null;
            if (!"success".equals(status)) {
                failureType = inferFailureType(owner, repo, accessToken, run);
                if (failureType != null) {
                    reasonCounts.put(failureType, reasonCounts.getOrDefault(failureType, 0) + 1);
                }
            }

            IngestRequest ingest = new IngestRequest();
            ingest.repositoryName = owner + "/" + repo;
            ingest.branch = headBranch != null ? headBranch : "main";
            ingest.status = status;
            ingest.durationSeconds = durationSeconds;
            ingest.commitHash = headSha;
            ingest.triggeredAt = triggeredAt;
            ingest.failureType = failureType;

            boolean accepted = ingestService.ingestBuild(ingest);
            if (accepted) {
                ingested++;
            } else {
                skipped++;
            }
        }

        result.put("ingested", ingested);
        result.put("skipped", skipped);
        result.put("reasons", reasonCounts);
        return result;
    }

    private String inferFailureType(String owner, String repo, String accessToken, Map<?, ?> run) {
        String conclusion = asString(run.get("conclusion"));
        if (conclusion != null) {
            String c = conclusion.toLowerCase(Locale.ROOT);
            if (c.contains("timed_out")) return "infra";
            if (c.contains("cancelled")) return "infra";
            if (c.contains("startup_failure")) return "infra";
        }

        Object idObj = run.get("id");
        String runId = idObj != null ? String.valueOf(idObj) : null;
        if (runId == null) {
            return "infra";
        }

        try {
            String jobsUrl = GITHUB_API_BASE + "/repos/" + owner + "/" + repo + "/actions/runs/" + runId + "/jobs?per_page=100";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Map> jobsResp = restTemplate.exchange(jobsUrl, HttpMethod.GET, request, Map.class);
            if (jobsResp.getStatusCode() != HttpStatus.OK || jobsResp.getBody() == null) {
                return "infra";
            }

            Object jobsObj = jobsResp.getBody().get("jobs");
            if (!(jobsObj instanceof List<?> jobs)) {
                return "infra";
            }

            for (Object jobObj : jobs) {
                if (!(jobObj instanceof Map<?, ?> job)) continue;

                Object stepsObj = job.get("steps");
                if (!(stepsObj instanceof List<?> steps)) continue;

                for (Object stepObj : steps) {
                    if (!(stepObj instanceof Map<?, ?> step)) continue;
                    String stepConclusion = asString(step.get("conclusion"));
                    if (!"failure".equalsIgnoreCase(stepConclusion)) continue;

                    String name = asString(step.get("name"));
                    if (name == null) continue;
                    String n = name.toLowerCase(Locale.ROOT);

                    if (n.contains("docker")) return "docker";
                    if (n.contains("depend") || n.contains("install") || n.contains("npm") || n.contains("pnpm") || n.contains("mvn") || n.contains("maven") || n.contains("gradle")) return "dependency";
                    if (n.contains("test")) return "test";

                    // Something else failed inside the workflow
                    return "infra";
                }
            }
        } catch (Exception e) {
            log.debug("Failed to infer failure type from jobs endpoint", e);
        }

        return "infra";
    }

    private int computeDurationSeconds(String runStartedAt, String createdAt, String updatedAt) {
        try {
            Instant start = Instant.parse(firstNonBlank(runStartedAt, createdAt));
            Instant end = updatedAt != null ? Instant.parse(updatedAt) : Instant.now();
            long seconds = Duration.between(start, end).getSeconds();
            if (seconds < 0) return 0;
            if (seconds > Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return (int) seconds;
        } catch (Exception e) {
            return 0;
        }
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return Instant.now().toString();
    }

    private String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
