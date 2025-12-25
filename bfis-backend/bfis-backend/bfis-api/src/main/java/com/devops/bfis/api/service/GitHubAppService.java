package com.devops.bfis.api.service;

import com.devops.bfis.api.config.GitHubAppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service for GitHub App operations
 * - Generate installation URLs
 * - Verify webhook signatures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubAppService {
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    private final GitHubAppConfig config;
    
    /**
     * Generate GitHub App installation URL
     * Users will be redirected here to install the app on their repositories
     * 
     * After installation, GitHub redirects back to: /api/github/callback?installation_id=...
     */
    public String generateInstallationUrl() {
        // GitHub App installation URL format:
        // https://github.com/apps/{app-slug}/installations/new
        // 
        // The app slug is derived from the GitHub App name
        // For "BFIS-CI-Tracker-Dksinghxd" the slug is "bfis-ci-tracker-dksinghxd"
        return "https://github.com/apps/bfis-ci-tracker-dksinghxd/installations/new";
    }
    
    /**
     * Verify GitHub webhook signature using HMAC SHA-256
     * 
     * GitHub sends signature in header: X-Hub-Signature-256: sha256=<hash>
     * 
     * @param payload Raw webhook payload (body)
     * @param signature Signature from X-Hub-Signature-256 header
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (signature == null || !signature.startsWith("sha256=")) {
            log.warn("Invalid signature format");
            return false;
        }
        
        String expectedSignature = signature.substring(7); // Remove "sha256=" prefix
        
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(
                config.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256
            );
            mac.init(secretKey);
            
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computedSignature = bytesToHex(hash);
            
            boolean valid = computedSignature.equalsIgnoreCase(expectedSignature);
            if (!valid) {
                log.warn("Webhook signature verification failed");
            }
            return valid;
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
