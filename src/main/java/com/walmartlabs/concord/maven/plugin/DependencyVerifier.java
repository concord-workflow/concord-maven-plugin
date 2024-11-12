package com.walmartlabs.concord.maven.plugin;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2020 - 2024 Concord Authors
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

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
                .map(ScopeViolation::new)
                .toList();
    }

    private static List<? extends Violation> findVersionViolations(List<Dependency> projectDependencies, List<Dependency> runtimeDependencies) {
        return projectDependencies.stream()
                .filter(dep -> !DependencyUtils.isTest(dep))
                .map(dep -> {
                    var runtimeDep = DependencyUtils.find(runtimeDependencies, dep.groupId(), dep.artifactId());
                    if (runtimeDep == null) {
                        return null;
                    }

                    var depVersion = new DefaultArtifactVersion(dep.version());
                    var runtimeDepVersion = new DefaultArtifactVersion(runtimeDep.version());
                    if (depVersion.compareTo(runtimeDepVersion) != 0) {
                        return new VersionViolation(dep, runtimeDep.version());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
