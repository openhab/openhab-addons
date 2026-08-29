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
package org.openhab.io.yamlcomposer.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerIncludeTagTest} contains tests for the !include tag functionality in the {@link YamlComposer}
 * class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Include Tag Specification")
class YamlComposerIncludeTagTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Syntax & Argument Validation")
    class SyntaxValidation {

        @ParameterizedTest(name = "Input [{0}] should warn about missing file parameter")
        @ValueSource(strings = { "!include", "!include ''", "!include {}", "!include { file: null }" })
        void warnsOnMalformedInclude(String input) throws IOException {
            Path yamlFile = writeFixture("includeTest.yaml", "a: " + input);
            loadFixture(yamlFile);

            assertThat(logSession.getTrackedWarnings(),
                    hasItem(containsString("Failed to process !include: missing 'file' parameter")));
        }

        @Test
        @DisplayName("Supports simple scalar syntax (!include file.yaml)")
        void supportsSimpleScalarArgument() throws IOException {
            writeFixture("simple.inc.yaml", "key: value");
            Path main = writeFixture("main.yaml", "toplevel: !include simple.inc.yaml");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "toplevel", "key"), equalTo("value"));
        }

        @Test
        @DisplayName("Supports URL-style scalar syntax (!include file.yaml?arg=value&bool)")
        void supportsUrlStyleScalarArgument() throws IOException {
            writeFixture("simple.inc.yaml", """
                    key: ${arg1}
                    bool: ${bool}
                    """);
            Path main = writeFixture("main.yaml", "toplevel: !include simple.inc.yaml?arg1=value&bool");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "toplevel", "key"), equalTo("value"));
            assertThat(getNestedValue(data, "toplevel", "bool"), is(true));
        }

        @Test
        @DisplayName("Supports map syntax with 'file' key (!include { file: ... })")
        void supportsMapArgument() throws IOException {
            writeFixture("included.inc.yaml", "key: value");
            Path main = writeFixture("main.yaml", """
                    toplevel: !include
                      file: included.inc.yaml
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "toplevel", "key"), equalTo("value"));
        }
    }

    @Nested
    @DisplayName("File Resolution & Recursion")
    class Recursion {

        @Test
        @DisplayName("Warns and continues when the included file does not exist")
        void warnsWhenIncludeFileNotFound() throws IOException {
            Path main = writeFixture("main.yaml", """
                    data: !include missing.yaml
                    other: value
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(logSession.getTrackedWarnings(), hasItem(allOf(containsString("Failed to process !include"),
                    containsString("missing.yaml"), containsString("No such file"))));

            assertThat("The rest of the document should still be processed", getNestedValue(data, "other"),
                    is("value"));
        }

        @Test
        @DisplayName("Supports deeply nested inclusions (chained files)")
        void supportsDeeplyNestedIncludes() throws IOException {
            Path main = writeFixture("main.yaml", "toplevel: !include level1.inc.yaml");
            writeFixture("level1.inc.yaml", "level1: !include level2.inc.yaml");
            writeFixture("level2.inc.yaml", "level2: leaf_value");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "toplevel", "level1", "level2"), equalTo("leaf_value"));
        }

        @Test
        @DisplayName("Detects and warns about circular inclusion loops to prevent stack overflow")
        void preventsInfiniteLoopOnCircularInclusion() throws IOException {
            Path main = writeFixture("a.yaml", "data: !include b.yaml");
            writeFixture("b.yaml", "data: !include c.yaml");
            writeFixture("c.yaml", "data: !include a.yaml");
            loadFixture(main);

            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("Circular inclusion detected")));
        }
    }

    @Nested
    @DisplayName("Variable Scoping & Inheritance")
    class VariableScoping {

        @Test
        @DisplayName("Inherits variables defined in the parent context into the included file")
        void inheritsVariablesFromParentContext() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      parent_var: "visible"
                    data: !include included.inc.yaml
                    """);
            writeFixture("included.inc.yaml", "result: ${parent_var}");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "data", "result"), equalTo("visible"));
        }

        @Test
        @DisplayName("Propagates global variables through multiple nested include levels")
        void propagatesGlobalVariablesThroughMultipleIncludeLevels() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      top_var: "hello"
                    root: !include mid.inc.yaml
                    """);
            writeFixture("mid.inc.yaml", "mid_key: !include leaf.inc.yaml");
            writeFixture("leaf.inc.yaml", "leaf_key: ${top_var}");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "root", "mid_key", "leaf_key"), equalTo("hello"));
        }

        @Test
        @DisplayName("Global variables (main file) take precedence over variables in included files")
        void prefersGlobalVariablesOverLocalVariables() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      target: "global"
                    data: !include
                      file: included.inc.yaml
                    """);
            writeFixture("included.inc.yaml", """
                    variables:
                      target: "local"
                    result: ${target}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "data", "result"), equalTo("global"));
        }

        @Test
        @DisplayName("Allows overriding parent/global variables using the 'vars' argument in the !include tag")
        void overridesParentVariablesUsingVarsArgument() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      target: "original"
                    data: !include
                      file: included.inc.yaml
                      vars:
                        target: "overridden"
                    """);
            writeFixture("included.inc.yaml", "result: ${target}");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "data", "result"), equalTo("overridden"));
        }

        @Test
        @DisplayName("VARS exists in the included file context and can be used to reference the entire variable set passed to the include")
        void varsKeywordReferencesEntireVariableSet() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      var1: "value1"
                    data: !include
                      file: included.inc.yaml
                      vars:
                        var2: "value2"
                    """);
            writeFixture("included.inc.yaml", """
                    variables:
                      local: "local_value"

                    var1: ${VARS.var1}
                    var2: ${VARS.var2}
                    var3: ${VARS.local}
                    """);
            Map<Object, @Nullable Object> data = loadFixture(main);
            assertThat(getNestedValue(data, "data", "var1"), equalTo("value1"));
            assertThat(getNestedValue(data, "data", "var2"), equalTo("value2"));
            assertThat(getNestedValue(data, "data", "var3"), equalTo("local_value"));
        }

        @Test
        @DisplayName("ARGS exists in the included file context and contains ONLY the variables explicitly passed to the include")
        void argsKeywordReferencesOnlyInjectedVariableSet() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      global_var: "global"
                    data: !include
                      file: included.inc.yaml
                      vars:
                        injected_var: "injected"
                    """);

            writeFixture("included.inc.yaml", """
                    variables:
                      local_var: "local"

                    # Verify ARGS strictly isolates the injected variables
                    args_global: ${ARGS.global_var | default('missing')}
                    args_injected: ${ARGS.injected_var | default('missing')}
                    args_local: ${ARGS.local_var | default('missing')}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat("ARGS MUST contain explicitly injected variables", getNestedValue(data, "data", "args_injected"),
                    equalTo("injected"));

            assertThat("ARGS should NOT contain inherited global variables",
                    getNestedValue(data, "data", "args_global"), equalTo("missing"));

            assertThat("ARGS should NOT contain variables defined locally in the include",
                    getNestedValue(data, "data", "args_local"), equalTo("missing"));
        }

        @Test
        @DisplayName("ARGS in nested includes isolates variables to the immediate call site, excluding parent explicitly passed vars")
        void argsKeywordStrictlyIsolatesNestedIncludes() throws IOException {
            Path main = writeFixture("main.yaml", """
                    data: !include
                      file: level1.inc.yaml
                      vars:
                        level1_var: "val1"
                    """);

            writeFixture("level1.inc.yaml", """
                    level2_data: !include
                      file: level2.inc.yaml
                      vars:
                        level2_var: "val2"
                    """);

            writeFixture("level2.inc.yaml", """
                    args_l1: ${ARGS.level1_var | default('missing')}
                    args_l2: ${ARGS.level2_var | default('missing')}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat("ARGS MUST contain explicitly injected variables for this specific include",
                    getNestedValue(data, "data", "level2_data", "args_l2"), equalTo("val2"));

            assertThat("ARGS should NOT contain variables injected by the parent include",
                    getNestedValue(data, "data", "level2_data", "args_l1"), equalTo("missing"));
        }
    }

    @Nested
    @DisplayName("Path Resolution Strategy")
    class PathResolution {

        @Test
        @DisplayName("Resolves nested includes relative to the directory of the currently processing file")
        void resolvesRelativePathsCorrectly() throws IOException {
            Path main = writeFixture("main.yaml", "toplevel: !include scripts/level1.inc.yaml");

            // level1 is in /scripts/, so it should find 'utils/level2' relative to itself
            writeFixture("scripts/level1.inc.yaml", "data: !include utils/level2.inc.yaml");
            writeFixture("scripts/utils/level2.inc.yaml", "status: 'relative_success'");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "toplevel", "data", "status"), equalTo("relative_success"));
        }

        @Test
        @DisplayName("Resolves absolute path includes outside the base directory")
        void resolvesAbsolutePathsCorrectly(@TempDir Path tempDir) throws IOException {
            assertThat(tempDir, not(equalTo(sharedTempDir)));
            Path includeFile = tempDir.resolve("level1.inc.yaml");
            assertTrue(includeFile.isAbsolute());
            Path main = writeFixture("main.yaml", "toplevel: !include " + includeFile);
            writeFixture(includeFile.toString(), "data: absolute_include");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "toplevel", "data"), equalTo("absolute_include"));
        }

        @Test
        @DisplayName("Supports parent directory navigation using '..' in file paths")
        void supportsParentDirectoryNavigation() throws IOException {
            Path main = writeFixture("nested/main.yaml", "cfg: !include ../config.inc.yaml");
            writeFixture("config.inc.yaml", "version: 1.0");

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "cfg", "version"), equalTo(1.0));
        }

        @Test
        @DisplayName("Resolves '@' placeholder at start of include path to OPENHAB_CONF")
        void resolvesAtPlaceholderInIncludePath() throws IOException {
            writeFixture("test/include.inc.yaml", "key: value");
            Path main = writeFixture("main_at.yaml", "toplevel: !include '@test/include.inc.yaml'");

            Path tempDir = Objects.requireNonNull(sharedTempDir);
            try {
                ComposerConfig.setRootsForTesting(tempDir, tempDir);
                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "toplevel", "key"), is("value"));
            } finally {
                ComposerConfig.resetRootsForTesting();
            }
        }

        @Test
        @DisplayName("Resolves '$' placeholder to OPENHAB_CONF/yamlcomposer")
        void resolvesDollarPlaceholderInIncludePath() throws IOException {
            String topLevel = "yamlcomposer";
            writeFixture(topLevel + "/room/include.inc.yaml", "key: value");
            Path main = writeFixture(topLevel + "/room/main.yaml", "toplevel: !include '$room/include.inc.yaml'");

            Path tempDir = Objects.requireNonNull(sharedTempDir);
            try {
                ComposerConfig.setRootsForTesting(tempDir, tempDir);
                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "toplevel", "key"), is("value"));
            } finally {
                ComposerConfig.resetRootsForTesting();
            }
        }
    }
}
