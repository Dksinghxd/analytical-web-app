package com.devops.bfis.api.service;

import com.devops.bfis.api.dto.GitHubInstallationRepositoriesResponse;
import com.devops.bfis.api.dto.GitHubRepositoryResponse;
import com.devops.bfis.core.domain.TrackedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for fetching repositories from GitHub App installation
 * 
 * Uses installation access token to call:
 * GET /installation/repositories
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubRepositoryService {
    private final GitHubInstallationTokenService tokenService;
    private final GitHubInstallationStore installationStore;
    private final TrackedRepositoryStore repoStore;
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String GITHUB_API_BASE = "https://api.github.com";
    
    /**
     * Fetch all repositories accessible to the GitHub App installation
     * 
     * GET /installation/repositories
     * Authorization: Bearer <installation_token>
     * 
     * Returns list of repositories the user granted access to
     */
    public List<GitHubRepositoryResponse> fetchInstallationRepositories() {
        String installationId = installationStore.getLatestInstallationId();
        if (installationId == null) {
            log.warn("No GitHub App installation found");
            return new ArrayList<>();
        }
        
        try {
            // Get installation access token
            String accessToken = tokenService.getInstallationAccessToken(installationId);
            
            // Build request
            String url = GITHUB_API_BASE + "/installation/repositories";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            // Make request
            ResponseEntity<GitHubInstallationRepositoriesResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    GitHubInstallationRepositoriesResponse.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<GitHubRepositoryResponse> repos = response.getBody().getRepositories();
                log.info("Fetched {} repositories from GitHub App installation", repos.size());
                return repos;
            } else {
                log.error("Failed to fetch repositories: {}", response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Error fetching installation repositories", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch repositories and automatically register them in BFIS
     * 
     * This is called after user installs the GitHub App
     * All granted repositories are added to the tracked repository list
     */
    public List<TrackedRepository> syncRepositories() {
        List<GitHubRepositoryResponse> githubRepos = fetchInstallationRepositories();
        
        List<TrackedRepository> trackedRepos = new ArrayList<>();
        
        for (GitHubRepositoryResponse repo : githubRepos) {
            String owner = repo.getOwner().getLogin();
            String repoName = repo.getName();
            String defaultBranch = repo.getDefaultBranch() != null ? repo.getDefaultBranch() : "main";
            
            // Check if already registered
            if (!repoStore.isRegistered(owner, repoName)) {
                TrackedRepository tracked = repoStore.register(owner, repoName, defaultBranch);
                trackedRepos.add(tracked);
                log.info("Auto-registered repository: {}/{}", owner, repoName);
            } else {
                // Already registered, just fetch it
                repoStore.findByName(owner, repoName).ifPresent(trackedRepos::add);
            }
        }
        
        return trackedRepos;
    }
    
    /**
     * Convert GitHub API repository response to simplified format for frontend
     */
    public List<RepositoryInfo> getRepositoryList() {
        List<GitHubRepositoryResponse> repos = fetchInstallationRepositories();
        
        return repos.stream()
                .map(repo -> new RepositoryInfo(
                        repo.getFullName(),
                        repo.getOwner().getLogin(),
                        repo.getName(),
                        repo.getDefaultBranch(),
                        repo.getDescription(),
                        repo.getHtmlUrl()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Simplified repository info for frontend
     */
    public record RepositoryInfo(
            String fullName,
            String owner,
            String repoName,
            String defaultBranch,
            String description,
            String htmlUrl
    ) {}
}
