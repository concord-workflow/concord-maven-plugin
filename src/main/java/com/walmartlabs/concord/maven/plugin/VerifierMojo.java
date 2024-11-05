package com.walmartlabs.concord.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mojo to list dependencies with versions and scopes, including external artifacts.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.COMPILE)
public class VerifierMojo extends AbstractMojo {

    private static final Logger log = LoggerFactory.getLogger(VerifierMojo.class);

    private final DependencyResolver resolver;

    private final DependencyVerifier dependencyVerifier;

    @Parameter(property = "concordVersion", required = true)
    private String concordVersion;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip = false;

    @Inject
    public VerifierMojo(DependencyResolver resolver, DependencyVerifier dependencyVerifier) {
        this.resolver = resolver;
        this.dependencyVerifier = dependencyVerifier;
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping...");
            return;
        }

        getLog().info("Resolving runtime-v2 dependencies for version '" + concordVersion + "'");

        var runtimeDeps = resolver.resolve("com.walmartlabs.concord.runtime.v2", "concord-runner-v2", concordVersion);
        var currentProjectDeps = resolver.resolveCurrent();

        List<ScopeViolation> scopeViolations = new ArrayList<>();
        var violations = dependencyVerifier.verify(currentProjectDeps, runtimeDeps);
        for (var v : violations) {
            if (v instanceof VersionViolation vv) {
                getLog().warn(
                        String.format("The dependency '%s' has an invalid version.\nExpected version: '%s'. Please update your POM.\n", vv.dependency(), vv.expectedVersion()));
            } else if (v instanceof ScopeViolation sv) {
                scopeViolations.add(sv);
            }
        }

        if (!scopeViolations.isEmpty()) {
            String header = """
            Some dependencies of Concord Plugins are expected to be declared with a 'provided' scope.
            Please check your POM file and ensure the following dependencies are correctly set with '<scope>provided</scope>':
            """;

            String deps = scopeViolations.stream()
                    .map(d -> " * " + d)
                    .collect(Collectors.joining("\n"));

            getLog().warn(header + "\n" + deps + "\n");
        }

        if (!violations.isEmpty()) {
            throw new MojoExecutionException("Some rules have failed. Look above for specific messages explaining why the rule failed");
        }
    }
}
