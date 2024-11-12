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
