package com.walmartlabs.concord.maven.plugin;

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
        Dependency projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "provided");
        Dependency runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.18.1-SNAPSHOT", "compile");

        List<Violation> violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertEquals(1, violations.size());
        assertInstanceOf(VersionViolation.class, violations.get(0));

        VersionViolation violation = (VersionViolation) violations.get(0);
        assertEquals("2.17.0", violation.dependency().version());
        assertEquals("2.18.1-SNAPSHOT", violation.expectedVersion());
    }

    @Test
    public void testInvalidScope() {
        Dependency projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "compile");
        Dependency runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "compile");

        List<Violation> violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertEquals(1, violations.size());
        assertInstanceOf(ScopeViolation.class, violations.get(0));
        ScopeViolation violation = (ScopeViolation) violations.get(0);
        assertEquals("compile", violation.dependency().scope());
        assertEquals("provided", violation.expectedScope());
    }

    @Test
    public void testNoViolations() {
        Dependency projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.18.1-SNAPSHOT", "provided");
        Dependency runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.18.1-SNAPSHOT", "compile");

        List<Violation> violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertTrue(violations.isEmpty());
    }

    @Test
    public void testScopeAndVersion() {
        Dependency projectDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.16.0", "compile");
        Dependency runtimeDependency = new Dependency("com.walmartlabs.concord", "concord-sdk", "2.17.0", "compile");

        List<Violation> violations = verifier.verify(List.of(projectDependency), List.of(runtimeDependency));

        assertEquals(2, violations.size());
    }
}
