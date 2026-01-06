/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.java223.internal.codegeneration;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class ClassGeneratorTest {

    @Test
    public void parseArgumentTypeFullClassNameTest() {
        Set<String> imports = new HashSet<>();
        String className = SourceGenerator.parseArgumentType(new FakeType("org.junit.jupiter.api.Test"), imports);
        assertEquals("Test", className);
        assertTrue(imports.contains("org.junit.jupiter.api.Test"), "Missing import statement");
    }

    @Test
    public void parseArgumentOneWordTypeTest() {
        Set<String> imports = new HashSet<>();
        String className = SourceGenerator.parseArgumentType(new FakeType("oneword"), imports);
        assertEquals("oneword", className);
        assertTrue(imports.isEmpty(), "Too many import statement");
    }

    @Test
    public void parseArgumentTypeFullGenericClassNameTest() {
        Set<String> imports = new HashSet<>();
        String className = SourceGenerator.parseArgumentType(
                new FakeType("java.fake.Generic<org.openhab.core.automation.annotation.RuleAction>"), imports);
        assertEquals("Generic<RuleAction>", className);
        assertTrue(imports.contains("org.openhab.core.automation.annotation.RuleAction"), "Missing import statement");
        assertTrue(imports.contains("java.fake.Generic"), "Missing import statement");
        assertEquals(2, imports.size());
    }

    @Test
    public void parseArgumentTypeComplexGenericClassNameTest() {
        Set<String> imports = new HashSet<>();
        String className = SourceGenerator.parseArgumentType(new FakeType(
                "java.fake.DoubleGeneric<? super org.fake.First, fake.Set<org.openhab.core.automation.annotation.RuleAction extends Another >>"),
                imports);
        assertEquals("DoubleGeneric<? super First, Set<RuleAction extends Another >>", className);
        assertTrue(imports.contains("java.fake.DoubleGeneric"), "Missing import statement");
        assertTrue(imports.contains("org.fake.First"), "Missing import statement");
        assertTrue(imports.contains("fake.Set"), "Missing import statement");
        assertTrue(imports.contains("org.openhab.core.automation.annotation.RuleAction"), "Missing import statement");
        assertEquals(4, imports.size());
    }

    public static class FakeType implements Type {

        private final String type;

        public FakeType(String type) {
            this.type = type;
        }

        @Override
        public String getTypeName() {
            return type;
        }
    }
}
