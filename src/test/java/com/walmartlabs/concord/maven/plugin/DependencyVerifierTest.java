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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyVerifierTest {

    private DependencyVerifier verifier;

    @BeforeEach
    public void setUp() {
        verifier = new DependencyVerifier();
    }

    @Test
    public void testInvalidVersion() {
        var projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "provided");
        var runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.18.1-SNAPSHOT", "compile");

        var violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertEquals(1, violations.size());
        assertInstanceOf(VersionViolation.class, violations.get(0));

        VersionViolation violation = (VersionViolation) violations.get(0);
        assertEquals("2.17.0", violation.dependency().version());
        assertEquals("2.18.1-SNAPSHOT", violation.expectedVersion());
    }

    @Test
    public void testInvalidScope() {
        var projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "compile");
        var runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "compile");

        var violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertEquals(1, violations.size());
        assertInstanceOf(ScopeViolation.class, violations.get(0));
        ScopeViolation violation = (ScopeViolation) violations.get(0);
        assertEquals("compile", violation.dependency().scope());
    }

    @Test
    public void testNoViolations() {
        var projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.18.1-SNAPSHOT", "provided");
        var runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.18.1-SNAPSHOT", "compile");

        var violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testScopeAndVersion() {
        var projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.16.0", "compile");
        var runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "compile");

        var violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertEquals(2, violations.size());
    }
}
