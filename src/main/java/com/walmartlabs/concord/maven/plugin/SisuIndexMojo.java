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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.io.CachingWriter;
import org.eclipse.sisu.space.SisuIndex;
import org.eclipse.sisu.space.URLClassSpace;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;

@SuppressWarnings({"UnusedDeclaration"})
@Mojo(name = "sisu-index", defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class SisuIndexMojo extends AbstractMojo {

    private static final String INDEX_FOLDER = "META-INF/sisu/";

    private final MavenProject project;
    private final BuildContext buildContext;
    private final File outputDirectory;

    @Inject
    public SisuIndexMojo(BuildContext buildContext, MavenProject project) {
        this.buildContext = buildContext;
        this.project = project;
        this.outputDirectory = new File(project.getBuild().getOutputDirectory());
    }

    @Override
    public void execute() {
        synchronized (project) {
            new SisuIndex(outputDirectory) {
                @Override
                protected Writer getWriter(String path) throws IOException {
                    Path p = outputDirectory.toPath().resolve(path);
                    Path d = p.getParent();
                    if (!Files.isDirectory(d)) {
                        Files.createDirectories(d);
                    }
                    return new CachingWriter(p, StandardCharsets.UTF_8);
                }

                @Override
                protected void info(String message) {
                    getLog().info(message);
                }

                @Override
                protected void warn(String message) {
                    getLog().warn(message);
                }
            }.index(new URLClassSpace(getProjectClassLoader(), getIndexPath()));
            buildContext.refresh(new File(outputDirectory, INDEX_FOLDER));
        }
    }

    private ClassLoader getProjectClassLoader() {
        List<URL> classPath = new ArrayList<>();
        appendDirectoryToClassPath(classPath, outputDirectory);
        for (Artifact artifact : project.getArtifacts()) {
            appendFileToClassPath(classPath, artifact.getFile());
        }
        return URLClassLoader.newInstance(classPath.toArray(new URL[0]));
    }

    private void appendDirectoryToClassPath(List<URL> urls, File directory) {
        if (!directory.isDirectory()) {
            getLog().debug("Path " + directory + " does not exist or is no directory");
            return;
        }

        Scanner scanner = buildContext.newScanner(directory);
        scanner.setIncludes(new String[]{"**/*.class"});
        scanner.scan();
        String[] includedFiles = scanner.getIncludedFiles();
        if (includedFiles != null && includedFiles.length > 0) {
            getLog().debug("Found at least one class file in " + directory);
            appendFileToClassPath(urls, directory);
        } else {
            getLog().debug("No class files found in " + directory);
        }
    }

    private void appendFileToClassPath(List<URL> urls, File file) {
        if (file == null) {
            return;
        }

        try {
            urls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
            getLog().warn(e.getLocalizedMessage());
        }
    }

    private URL[] getIndexPath() {
        List<URL> indexPath = new ArrayList<>();
        appendDirectoryToClassPath(indexPath, outputDirectory);
        return indexPath.toArray(new URL[0]);
    }
}
