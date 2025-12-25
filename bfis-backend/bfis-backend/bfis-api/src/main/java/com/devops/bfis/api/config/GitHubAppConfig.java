package com.devops.bfis.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * GitHub App configuration properties
 * 
 * Required environment variables:
 * - GITHUB_APP_ID: GitHub App ID (numeric)
 * - GITHUB_APP_CLIENT_ID: OAuth client ID
 * - GITHUB_APP_CLIENT_SECRET: OAuth client secret
 * - GITHUB_APP_WEBHOOK_SECRET: Webhook secret for signature verification
 * - GITHUB_APP_PRIVATE_KEY: Base64-encoded private key (PEM format)
 */
@Configuration
@ConfigurationProperties(prefix = "github.app")
@Data
public class GitHubAppConfig {
    private String appId;
    private String clientId;
    private String clientSecret;
    private String webhookSecret;
    private String privateKey;
    private String privateKeyPath;
    private String installBaseUrl = "https://github.com/apps";
}
