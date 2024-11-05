package com.walmartlabs.concord.maven.plugin;

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
