package com.walmartlabs.concord.maven.plugin;

public record Dependency(String groupId, String artifactId, String version, String scope) {

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version + " (" + scope + ")";
    }
}
