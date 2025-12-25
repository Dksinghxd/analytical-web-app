package com.devops.bfis.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub API response for a single repository
 * 
 * Subset of fields from:
 * GET /installation/repositories
 */
@Data
public class GitHubRepositoryResponse {
    private Long id;
    
    @JsonProperty("full_name")
    private String fullName; // e.g., "owner/repo"
    
    private String name; // repo name
    
    private Owner owner;
    
    @JsonProperty("default_branch")
    private String defaultBranch; // e.g., "main", "master"
    
    private String description;
    
    @JsonProperty("private")
    private Boolean isPrivate;
    
    @JsonProperty("html_url")
    private String htmlUrl;
    
    @Data
    public static class Owner {
        private String login; // owner username
        private String type; // "User" or "Organization"
    }
}
