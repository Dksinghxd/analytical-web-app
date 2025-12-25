package com.devops.bfis.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Discovers existing GitHub App installations using App JWT.
 * Useful after backend restarts because installation ids are currently stored in-memory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubInstallationDiscoveryService {
    private static final String GITHUB_API_BASE = "https://api.github.com";

    private final GitHubJwtService jwtService;
    private final RestTemplate restTemplate = new RestTemplate();

    private volatile String lastError;

    public List<InstallationInfo> listInstallations() {
        try {
            lastError = null;
            String jwt = jwtService.generateJwt();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    GITHUB_API_BASE + "/app/installations",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> body = response.getBody();
            if (body == null) {
                return Collections.emptyList();
            }

            List<InstallationInfo> result = new ArrayList<>();
            for (Map<String, Object> item : body) {
                Object idObj = item.get("id");
                Map<String, Object> account = safeMap(item.get("account"));
                String login = Objects.toString(account.get("login"), null);
                if (idObj == null) {
                    continue;
                }
                String id = Objects.toString(idObj, null);
                if (id != null) {
                    result.add(new InstallationInfo(id, login));
                }
            }
            return result;
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            lastError = "GitHub API " + e.getStatusCode() + " " + e.getStatusText() + (body == null || body.isBlank() ? "" : (": " + body));
            log.warn("Failed to list GitHub App installations: {}", lastError);
            return Collections.emptyList();
        } catch (Exception e) {
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("Failed to list GitHub App installations (JWT/config missing or API call failed): {}", lastError, e);
            return Collections.emptyList();
        }
    }

    public String getLastError() {
        return lastError;
    }

    private Map<String, Object> safeMap(Object value) {
        if (value instanceof Map<?, ?> m) {
            //noinspection unchecked
            return (Map<String, Object>) m;
        }
        return Collections.emptyMap();
    }

    public record InstallationInfo(String installationId, String accountLogin) {}
}
