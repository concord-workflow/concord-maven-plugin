package com.walmartlabs.concord.maven.plugin;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DependencyVerifier {

    public List<Violation> verify(List<Dependency> projectDependencies, List<Dependency> runtimeDependencies) {
        List<Violation> violations = new ArrayList<>();

        var scopeViolations = findScopeViolations(projectDependencies, runtimeDependencies);
        violations.addAll(scopeViolations);

        var versionViolations = findVersionViolations(projectDependencies, runtimeDependencies);
        violations.addAll(versionViolations);

        return violations;
    }

    private static List<? extends Violation> findScopeViolations(List<Dependency> projectDependencies, List<Dependency> runtimeDependencies) {
        return projectDependencies.stream()
                .filter(dep -> !DependencyUtils.isTest(dep))
                .filter(dep -> {
                    Dependency runtimeDep = DependencyUtils.find(runtimeDependencies, dep.groupId(), dep.artifactId());
                    return runtimeDep != null && !DependencyUtils.isProvided(runtimeDep) && !DependencyUtils.isProvided(dep);
                })
                .map(dep -> new ScopeViolation(dep, "provided"))
                .toList();
    }

    private static List<? extends Violation> findVersionViolations(List<Dependency> projectDependencies, List<Dependency> runtimeDependencies) {
        return projectDependencies.stream()
                .filter(dep -> !DependencyUtils.isTest(dep))
                .map(dep -> {
                    Dependency runtimeDep = DependencyUtils.find(runtimeDependencies, dep.groupId(), dep.artifactId());
                    if (runtimeDep == null) {
                        return null;
                    }

                    DefaultArtifactVersion depVersion = new DefaultArtifactVersion(dep.version());
                    DefaultArtifactVersion runtimeDepVersion = new DefaultArtifactVersion(runtimeDep.version());
                    if (depVersion.compareTo(runtimeDepVersion) != 0) {
                        return new VersionViolation(dep, runtimeDep.version());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
