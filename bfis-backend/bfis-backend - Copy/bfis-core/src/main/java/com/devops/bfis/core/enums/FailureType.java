package com.devops.bfis.core.enums;

/**
 * Types of build failures
 * Must match frontend expectations: test | dependency | docker | infra
 */
public enum FailureType {
    TEST("test"),
    DEPENDENCY("dependency"),
    DOCKER("docker"),
    INFRA("infra");

    private final String value;

    FailureType(String value) {
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
