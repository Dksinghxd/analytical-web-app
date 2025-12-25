package com.devops.bfis.api.controller;

import com.devops.bfis.api.dto.GitHubWorkflowRunEvent;
import com.devops.bfis.api.config.GitHubAppConfig;
import com.devops.bfis.api.service.*;
import com.devops.bfis.core.domain.TrackedRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Controller for GitHub App integration
 * - OAuth/installation flow
 * - Webhook event handling
 * - Repository management
 */
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Slf4j
public class GitHubController {
    private final GitHubAppService gitHubAppService;
    private final GitHubWebhookProcessor webhookProcessor;
    private final GitHubInstallationStore installationStore;
    private final GitHubInstallationDiscoveryService installationDiscoveryService;
    private final GitHubRepositoryService repositoryService;
    private final GitHubActionsIngestService actionsIngestService;
    private final GitHubJwtService jwtService;
    private final ObjectMapper objectMapper;
    private final GitHubAppConfig gitHubAppConfig;
    
    /**
     * GET /api/github/connect
     * 
     * Returns GitHub App installation URL for users to connect repositories
     * Frontend redirects users to this URL
     */
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> getConnectionUrl() {
        String installUrl = gitHubAppService.generateInstallationUrl();
        log.info("Generated GitHub App installation URL");
        return ResponseEntity.ok(Map.of("installUrl", installUrl));
    }
    
    /**
     * POST /api/github/webhook
     * 
     * Receives GitHub webhook events
     * - Verifies signature
     * - Processes workflow_run events
     * - Ignores other events
     * 
     * Headers:
     * - X-GitHub-Event: Event type (workflow_run, push, etc.)
     * - X-Hub-Signature-256: HMAC signature for verification
     * - X-GitHub-Delivery: Unique delivery ID
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestBody String payload
    ) {
        log.info("Received GitHub webhook: event={}, delivery={}", eventType, deliveryId);
        
        // Verify signature
        if (!gitHubAppService.verifyWebhookSignature(payload, signature)) {
            log.warn("Webhook signature verification failed for delivery: {}", deliveryId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        
        // Process workflow_run events
        if ("workflow_run".equals(eventType)) {
            try {
                GitHubWorkflowRunEvent event = objectMapper.readValue(payload, GitHubWorkflowRunEvent.class);
                webhookProcessor.processWorkflowRunCompleted(event);
                log.info("Successfully processed workflow_run event for delivery: {}", deliveryId);
            } catch (Exception e) {
                log.error("Failed to process workflow_run event", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to process event");
            }
        } else {
            log.debug("Ignoring non-workflow_run event: {}", eventType);
        }
        
        return ResponseEntity.ok("Webhook received");
    }
    
    /**
     * GET /api/github/status
     * 
     * Returns GitHub App connection status
     * For now, just returns basic info
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        ensureInstallationPresent();
        boolean hasInstallation = installationStore.hasInstallations();
        return ResponseEntity.ok(Map.of(
                "connected", hasInstallation,
                "webhookEndpoint", "/api/github/webhook",
                "hasInstallation", hasInstallation
        ));
    }
    
    /**
     * GET /api/github/callback
     * 
     * GitHub redirects here after user installs the GitHub App
     * 
     * Query params:
     * - installation_id: The ID GitHub assigns to this installation
     * - setup_action: "install" or "update"
     * 
     * Flow:
     * 1. Capture installation_id
     * 2. Store it
     * 3. Generate installation access token
     * 4. Redirect to frontend success page
     */
    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam("installation_id") String installationId,
            @RequestParam(value = "setup_action", required = false) String setupAction
    ) {
        log.info("GitHub App installation callback: installation_id={}, action={}", installationId, setupAction);
        
        try {
            // Store installation and get access token
            // This will cache the token for subsequent API calls
            log.info("Fetching installation token for installation: {}", installationId);
            // Store only the installation id; token is fetched lazily on first API call
            installationStore.storeInstallation(installationId, null, Instant.EPOCH);
            
            // Automatically sync repositories
            List<TrackedRepository> repos = repositoryService.syncRepositories();
            log.info("Auto-registered {} repositories from GitHub App", repos.size());
            
            // Redirect to frontend with success
            return new RedirectView("http://localhost:3000?github_connected=true");
            
        } catch (Exception e) {
            log.error("Failed to process GitHub App callback", e);
            return new RedirectView("http://localhost:3000?github_connected=false&error=" + e.getMessage());
        }
    }
    
    /**
     * GET /api/github/repos
     * 
     * Fetch all repositories accessible to the GitHub App
     * 
     * Returns:
     * - List of repositories the user granted access to
     * - Each repo includes: full_name, owner, repo_name, default_branch
     * 
     * This is called by frontend to display connected repositories
     */
    @GetMapping("/repos")
    public ResponseEntity<?> getRepositories() {
        ensureInstallationPresent();
        if (!installationStore.hasInstallations()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No GitHub App installation found. Please connect GitHub first."));
        }
        
        try {
            List<GitHubRepositoryService.RepositoryInfo> repos = repositoryService.getRepositoryList();
            log.info("Returning {} repositories", repos.size());
            return ResponseEntity.ok(repos);
            
        } catch (Exception e) {
            log.error("Failed to fetch repositories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch repositories: " + e.getMessage()));
        }
    }

    /**
     * POST /api/github/reset
     * Clears locally cached installation ids/tokens (dev helper).
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetInstallation() {
        installationStore.clear();
        return ResponseEntity.ok(Map.of("message", "GitHub installation cache cleared"));
    }

    /**
     * GET /api/github/debug
     * Returns minimal diagnostic info about current installation cache.
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug() {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("hasInstallation", installationStore.hasInstallations());
        body.put("latestInstallationId", installationStore.getLatestInstallationId());
        body.put("appIdConfigured", gitHubAppConfig.getAppId() != null && !gitHubAppConfig.getAppId().isBlank());
        boolean privateKeyConfigured = (gitHubAppConfig.getPrivateKey() != null && !gitHubAppConfig.getPrivateKey().isBlank())
                || (gitHubAppConfig.getPrivateKeyPath() != null && !gitHubAppConfig.getPrivateKeyPath().isBlank());
        body.put("privateKeyConfigured", privateKeyConfigured);
        String privateKeyPath = gitHubAppConfig.getPrivateKeyPath();
        boolean privateKeyPathConfigured = privateKeyPath != null && !privateKeyPath.isBlank();
        body.put("privateKeyPathConfigured", privateKeyPathConfigured);
        if (privateKeyPathConfigured) {
            String trimmed = privateKeyPath.trim();
            body.put("privateKeyPath", trimmed);
            try {
                Path p = Path.of(trimmed);
                body.put("privateKeyPathExists", Files.exists(p));
                body.put("privateKeyPathIsFile", Files.isRegularFile(p));
            } catch (Exception e) {
                body.put("privateKeyPathExists", false);
                body.put("privateKeyPathIsFile", false);
                body.put("privateKeyPathError", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        body.put("privateKeySource", privateKeyPathConfigured ? "path" : "env");
        body.put("webhookSecretConfigured", gitHubAppConfig.getWebhookSecret() != null && !gitHubAppConfig.getWebhookSecret().isBlank());
        body.put("installationDiscoveryLastError", installationDiscoveryService.getLastError());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/installations")
    public ResponseEntity<?> installations() {
        List<GitHubInstallationDiscoveryService.InstallationInfo> installs = installationDiscoveryService.listInstallations();
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("count", installs.size());
        body.put("installations", installs);
        body.put("lastError", installationDiscoveryService.getLastError());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/jwt-check")
    public ResponseEntity<?> jwtCheck() {
        try {
            jwtService.generateJwt();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String error = root.getClass().getSimpleName() + ": " + root.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "ok", false,
                    "error", error
            ));
        }
    }
    
    /**
     * POST /api/github/sync
     * 
     * Manually trigger repository sync
     * Fetches all repositories from GitHub and registers them in BFIS
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncRepositories() {
        ensureInstallationPresent();
        if (!installationStore.hasInstallations()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No GitHub App installation found"));
        }
        
        try {
            List<TrackedRepository> repos = repositoryService.syncRepositories();
            log.info("Synced {} repositories", repos.size());
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully synced repositories",
                    "count", repos.size(),
                    "repositories", repos
            ));
            
        } catch (Exception e) {
            log.error("Failed to sync repositories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to sync repositories: " + e.getMessage()));
        }
    }

    /**
     * POST /api/github/ingest-actions?owner=...&repo=...&perPage=20
     *
     * Pulls recent GitHub Actions workflow runs for the given repository and ingests them
     * into BFIS builds/failures (so the UI can show real failing reasons).
     */
    @PostMapping("/ingest-actions")
    public ResponseEntity<?> ingestActions(
            @RequestParam("owner") String owner,
            @RequestParam("repo") String repo,
            @RequestParam(value = "perPage", required = false, defaultValue = "20") int perPage
    ) {
        ensureInstallationPresent();
        if (!installationStore.hasInstallations()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No GitHub App installation found"));
        }

        try {
            Map<String, Object> summary = actionsIngestService.ingestRecentWorkflowRuns(owner, repo, perPage);
            if (summary.containsKey("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Failed to ingest GitHub Actions runs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to ingest GitHub Actions runs: " + e.getMessage()));
        }
    }

    /**
     * GET /api/github/actions/failure-reasons?owner=...&repo=...&perPage=20
     *
     * Returns the latest failed GitHub Actions runs with the failed job/step name.
     * This is used to answer: "why is my CI/CD pipeline failing?".
     */
    @GetMapping("/actions/failure-reasons")
    public ResponseEntity<?> getFailureReasons(
            @RequestParam("owner") String owner,
            @RequestParam("repo") String repo,
            @RequestParam(value = "perPage", required = false, defaultValue = "20") int perPage
    ) {
        ensureInstallationPresent();
        if (!installationStore.hasInstallations()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No GitHub App installation found"));
        }

        try {
            Map<String, Object> summary = actionsIngestService.getRecentFailureReasons(owner, repo, perPage);
            if (summary.containsKey("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(summary);
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Failed to fetch GitHub Actions failure reasons", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch GitHub Actions failure reasons: " + e.getMessage()));
        }
    }

    private void ensureInstallationPresent() {
        if (installationStore.hasInstallations()) {
            return;
        }
        List<GitHubInstallationDiscoveryService.InstallationInfo> installs = installationDiscoveryService.listInstallations();
        if (!installs.isEmpty()) {
            String installationId = installs.get(0).installationId();
            installationStore.storeInstallation(installationId, null, Instant.EPOCH);
        } else if (installationDiscoveryService.getLastError() != null) {
            log.warn("No installations discovered: {}", installationDiscoveryService.getLastError());
        }
    }
}

