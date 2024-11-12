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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedDeclaration"})
@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class VerifierMojo extends AbstractMojo {

    private final DependencyResolver resolver;

    private final DependencyVerifier dependencyVerifier;

    @Parameter(property = "concordVersion", required = true)
    private String concordVersion;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

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
                    .map(v -> " * " + v.dependency())
                    .collect(Collectors.joining("\n"));

            getLog().warn(header + "\n" + deps + "\n");
        }

        if (!violations.isEmpty()) {
            throw new MojoExecutionException("Some rules have failed. Look above for specific messages explaining why the rule failed");
        }
    }
}
