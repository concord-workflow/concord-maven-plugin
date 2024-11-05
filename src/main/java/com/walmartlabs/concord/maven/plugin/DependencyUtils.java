package com.walmartlabs.concord.maven.plugin;

import java.util.List;

import static org.apache.maven.artifact.Artifact.SCOPE_PROVIDED;
import static org.apache.maven.artifact.Artifact.SCOPE_TEST;

public final class DependencyUtils {

    public static boolean isTest(Dependency dependency) {
        return SCOPE_TEST.equalsIgnoreCase(dependency.scope());
    }

    public static boolean isProvided(Dependency dependency) {
        return SCOPE_PROVIDED.equalsIgnoreCase(dependency.scope());
    }

    public static Dependency find(List<Dependency> deps, String groupId, String artifactId) {
        return deps.stream()
                .filter(d -> d.groupId().equals(groupId) && d.artifactId().equals(artifactId))
                .findFirst()
                .orElse(null);
    }

    private DependencyUtils() {
    }
}
