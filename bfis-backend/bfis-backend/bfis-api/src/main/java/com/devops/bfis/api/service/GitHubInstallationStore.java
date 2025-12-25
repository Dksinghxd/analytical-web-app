package com.devops.bfis.api.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for GitHub App installations
 * 
 * Stores:
 * - installation_id: The ID GitHub assigns when user installs the app
 * - access_token: Short-lived token for API calls (expires in 1 hour)
 * - expires_at: Token expiration time
 */
@Component
public class GitHubInstallationStore {
    private final Map<String, InstallationData> installations = new ConcurrentHashMap<>();
    private volatile String latestInstallationId;
    
    /**
     * Store installation data after user completes GitHub App installation
     */
    public void storeInstallation(String installationId, String accessToken, Instant expiresAt) {
        // If only the installation id is known (token not fetched yet), store an immediately-expired token.
        Instant safeExpiresAt = expiresAt != null ? expiresAt : Instant.EPOCH;
        InstallationData data = new InstallationData(installationId, accessToken, safeExpiresAt);
        installations.put(installationId, data);
        latestInstallationId = installationId;
    }
    
    /**
     * Get access token for an installation
     * Returns null if not found or expired
     */
    public String getAccessToken(String installationId) {
        InstallationData data = installations.get(installationId);
        if (data == null) {
            return null;
        }

        // Token may not be fetched yet
        if (data.accessToken == null || data.expiresAt == null) {
            return null;
        }
        
        // Check if token is expired
        if (Instant.now().isAfter(data.expiresAt)) {
            return null;
        }
        
        return data.accessToken;
    }
    
    /**
     * Get the most recent installation ID
     * For v1, we assume single-user installation
     */
    public String getLatestInstallationId() {
        if (latestInstallationId != null) {
            return latestInstallationId;
        }
        return installations.keySet().stream().findFirst().orElse(null);
    }

    /** Clear all stored installations (useful for local/dev when a bad installation id is cached). */
    public void clear() {
        installations.clear();
        latestInstallationId = null;
    }
    
    /**
     * Check if we have any installations
     */
    public boolean hasInstallations() {
        return !installations.isEmpty();
    }
    
    private static class InstallationData {
        final String installationId;
        final String accessToken;
        final Instant expiresAt;
        
        InstallationData(String installationId, String accessToken, Instant expiresAt) {
            this.installationId = installationId;
            this.accessToken = accessToken;
            this.expiresAt = expiresAt;
        }
    }
}
