package com.devops.bfis.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackedRepository {
    private String id;
    private String owner;
    private String repoName;
    private String defaultBranch;
    private Instant createdAt;
}
