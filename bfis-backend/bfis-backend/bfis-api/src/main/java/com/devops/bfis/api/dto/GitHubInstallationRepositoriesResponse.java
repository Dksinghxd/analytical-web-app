package com.devops.bfis.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * GitHub API response for installation repositories
 * 
 * Response from:
 * GET /installation/repositories
 * 
 * {
 *   "total_count": 2,
 *   "repositories": [...]
 * }
 */
@Data
public class GitHubInstallationRepositoriesResponse {
    @JsonProperty("total_count")
    private Integer totalCount;
    
    private List<GitHubRepositoryResponse> repositories;
}
