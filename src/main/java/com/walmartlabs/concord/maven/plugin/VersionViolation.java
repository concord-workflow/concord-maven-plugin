package com.walmartlabs.concord.maven.plugin;

public record VersionViolation(Dependency dependency, String expectedVersion) implements Violation {
}
