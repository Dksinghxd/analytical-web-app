package com.devops.bfis.api.dto;

import lombok.Data;

@Data
public class RegisterRepositoryRequest {
    private String owner;
    private String repoName;
    private String defaultBranch;
}
