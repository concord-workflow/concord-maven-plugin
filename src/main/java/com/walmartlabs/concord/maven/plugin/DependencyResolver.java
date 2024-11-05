package com.walmartlabs.concord.maven.plugin;

import org.apache.maven.execution.MavenSession;

import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.*;

import javax.inject.Inject;
import java.util.List;

public class DependencyResolver {

    private final MavenProject project;
    private final RepositorySystem repositorySystem;
    private final MavenSession session;

    @Inject
    public DependencyResolver(MavenProject project, RepositorySystem repositorySystem, MavenSession session) {
        this.project = project;
        this.repositorySystem = repositorySystem;
        this.session = session;
    }

    public List<Dependency> resolve(String groupId, String artifactId, String version) {
        MavenProject project = session.getCurrentProject();

        Artifact externalArtifact = new DefaultArtifact(
                groupId, artifactId, "", "jar", version);

        ArtifactDescriptorRequest request = new ArtifactDescriptorRequest();
        request.setArtifact(externalArtifact);
        request.setRepositories(project.getRemoteProjectRepositories());
        try {
            ArtifactDescriptorResult result = repositorySystem.readArtifactDescriptor(session.getRepositorySession(), request);
            return result.getDependencies().stream()
                    .map(DependencyResolver::toDependency)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Dependency> resolveCurrent() {
        return project.getDependencies().stream()
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
