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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Mojo(name = "test-sisu-index", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class TestSisuIndexMojo extends AbstractSisuIndexMojo {

    @Inject
    public TestSisuIndexMojo(BuildContext buildContext, MavenProject project) {
        super(buildContext, project, testOutputDirectory(project), List.of(testOutputDirectory(project), outputDirectory(project)));
    }

    private static File outputDirectory(MavenProject project) {
        return new File(project.getBuild().getOutputDirectory());
    }

    private static File testOutputDirectory(MavenProject project) {
        return new File(project.getBuild().getTestOutputDirectory());
    }
}
