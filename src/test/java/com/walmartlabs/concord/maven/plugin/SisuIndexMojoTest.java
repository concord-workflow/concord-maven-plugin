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

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SisuIndexMojoTest {

    @TempDir
    private Path workDir;

    @Test
    public void testMainIndex() throws Exception {
        Path outputDirectory = workDir.resolve("classes");
        Path testOutputDirectory = workDir.resolve("test-classes");
        copyClass(NamedMainComponent.class, outputDirectory);
        copyClass(NamedTestComponent.class, testOutputDirectory);

        new SisuIndexMojo(new DefaultBuildContext(), project(outputDirectory, testOutputDirectory)).execute();

        String index = readIndex(outputDirectory);
        assertTrue(index.contains(NamedMainComponent.class.getName()));
        assertFalse(index.contains(NamedTestComponent.class.getName()));
    }

    @Test
    public void testTestIndex() throws Exception {
        Path outputDirectory = workDir.resolve("classes");
        Path testOutputDirectory = workDir.resolve("test-classes");
        copyClass(NamedMainComponent.class, outputDirectory);
        copyClass(NamedTestComponent.class, testOutputDirectory);

        new TestSisuIndexMojo(new DefaultBuildContext(), project(outputDirectory, testOutputDirectory)).execute();

        String index = readIndex(testOutputDirectory);
        assertTrue(index.contains(NamedTestComponent.class.getName()));
        assertFalse(index.contains(NamedMainComponent.class.getName()));
    }

    private static MavenProject project(Path outputDirectory, Path testOutputDirectory) {
        Build build = new Build();
        build.setOutputDirectory(outputDirectory.toString());
        build.setTestOutputDirectory(testOutputDirectory.toString());

        MavenProject project = new MavenProject();
        project.setBuild(build);
        project.setArtifacts(Set.of());
        return project;
    }

    private static void copyClass(Class<?> type, Path outputDirectory) throws IOException {
        String name = type.getName().replace('.', '/') + ".class";
        Path target = outputDirectory.resolve(name);
        Files.createDirectories(target.getParent());
        try (InputStream in = type.getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IOException("Class file not found: " + name);
            }
            Files.copy(in, target);
        }
    }

    private static String readIndex(Path outputDirectory) throws IOException {
        return Files.readString(outputDirectory.resolve("META-INF/sisu/javax.inject.Named"));
    }

    @Named("main")
    public static class NamedMainComponent {
    }

    @Named("test")
    public static class NamedTestComponent {
    }
}
