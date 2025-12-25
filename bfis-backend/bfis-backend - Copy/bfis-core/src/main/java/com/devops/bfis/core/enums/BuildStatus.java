package com.devops.bfis.core.enums;

/**
 * Build execution status
 * Must match frontend expectations exactly: success | failed | flaky
 */
public enum BuildStatus {
    SUCCESS("success"),
    FAILED("failed"),
    FLAKY("flaky");

    private final String value;

    BuildStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
