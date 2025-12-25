package com.devops.bfis.api.dto;

import lombok.Data;

/**
 * GitHub webhook event payload for workflow_run
 * 
 * Simplified structure focusing on fields needed for BFIS ingestion
 */
@Data
public class GitHubWorkflowRunEvent {
    private String action; // completed, requested, in_progress
    private WorkflowRun workflowRun;
    private Repository repository;
    
    @Data
    public static class WorkflowRun {
        private Long id;
        private String name;
        private String headBranch;
        private String headSha;
        private String status; // completed, in_progress, queued
        private String conclusion; // success, failure, cancelled, skipped, null
        private String createdAt;
        private String updatedAt;
        private String runStartedAt;
    }
    
    @Data
    public static class Repository {
        private String fullName; // owner/repo
        private String name;
        private Owner owner;
    }
    
    @Data
    public static class Owner {
        private String login;
    }
}
