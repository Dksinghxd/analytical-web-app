package com.devops.bfis.api.dto;

public class IngestRequest {
    public String repositoryName;
    public String branch;
    public String status;
    public int durationSeconds;
    public String failureType;
    public String commitHash;
    public String triggeredAt;
}
