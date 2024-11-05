package com.walmartlabs.concord.maven.plugin;

public record ScopeViolation(Dependency dependency) implements Violation {
}
