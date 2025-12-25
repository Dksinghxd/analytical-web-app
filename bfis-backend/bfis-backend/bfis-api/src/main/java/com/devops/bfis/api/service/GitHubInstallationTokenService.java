package com.devops.bfis.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

/**
 * Service for managing GitHub App installation tokens
 * 
 * Flow:
 * 1. Generate JWT using GitHubJwtService
 * 2. Exchange installation_id for access token
 * 3. Use access token for GitHub API calls
 * 
 * Installation tokens expire after 1 hour
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubInstallationTokenService {
    private final GitHubJwtService jwtService;
    private final GitHubInstallationStore installationStore;
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    /**
     * Get or create an access token for an installation
     * 
     * If token exists and is valid, returns cached token
     * Otherwise, requests new token from GitHub
     * 
     * @param installationId GitHub installation ID
     * @return Access token for API calls
     */
    public String getInstallationAccessToken(String installationId) {
        // Check if we have a valid cached token
        String cachedToken = installationStore.getAccessToken(installationId);
        if (cachedToken != null) {
            log.debug("Using cached installation token");
            return cachedToken;
        }
        
        // Request new token from GitHub
        log.info("Requesting new installation token for installation: {}", installationId);
        return requestNewAccessToken(installationId);
    }
    
    /**
     * Request a new installation access token from GitHub
     * 
     * POST /app/installations/{installation_id}/access_tokens
     * Authorization: Bearer <JWT>
     * 
     * Response:
     * {
     *   "token": "ghs_...",
     *   "expires_at": "2025-12-25T15:00:00Z",
     *   "permissions": {...},
     *   "repository_selection": "all"
     * }
     */
    private String requestNewAccessToken(String installationId) {
        try {
            // Generate JWT for authentication
            String jwt = jwtService.generateJwt();
            
            // Build request
            String url = String.format("%s/app/installations/%s/access_tokens", 
                    GITHUB_API_BASE, installationId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwt);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            
            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            
            // Make request
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> body = response.getBody();
                String accessToken = (String) body.get("token");
                String expiresAtStr = (String) body.get("expires_at");
                Instant expiresAt = Instant.parse(expiresAtStr);
                
                // Cache the token
                installationStore.storeInstallation(installationId, accessToken, expiresAt);
                
                log.info("Successfully obtained installation token (expires: {})", expiresAtStr);
                return accessToken;
            } else {
                throw new RuntimeException("Failed to get installation token: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Failed to get installation access token", e);
            throw new RuntimeException("Failed to get installation access token", e);
        }
    }
}
