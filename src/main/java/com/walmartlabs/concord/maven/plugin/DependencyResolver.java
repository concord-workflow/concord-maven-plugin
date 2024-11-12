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

import org.apache.maven.execution.MavenSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;

import javax.inject.Inject;
import java.util.List;

public class DependencyResolver {

    private final RepositorySystem repositorySystem;
    private final MavenSession session;

    @Inject
    public DependencyResolver(RepositorySystem repositorySystem, MavenSession session) {
        this.repositorySystem = repositorySystem;
        this.session = session;
    }

    public List<Dependency> resolve(String groupId, String artifactId, String version) {
        var request = new ArtifactDescriptorRequest()
                .setArtifact(new DefaultArtifact(groupId, artifactId, "", "jar", version))
                .setRepositories(session.getCurrentProject().getRemoteProjectRepositories());

        try {
            var result = repositorySystem.readArtifactDescriptor(session.getRepositorySession(), request);
            return result.getDependencies().stream()
                    .map(DependencyResolver::toDependency)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dependency> resolveCurrent() {
        return session.getCurrentProject().getDependencies().stream()
                .map(DependencyResolver::toDependency)
                .toList();
    }

    private static Dependency toDependency(org.apache.maven.model.Dependency dependency) {
        return new Dependency(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), dependency.getScope());
    }

    private static Dependency toDependency(org.eclipse.aether.graph.Dependency dependency) {
        var artifact = dependency.getArtifact();
        return new Dependency(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), dependency.getScope());
    }
}
