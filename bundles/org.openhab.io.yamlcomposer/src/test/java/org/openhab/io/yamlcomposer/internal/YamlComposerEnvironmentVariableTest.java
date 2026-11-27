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
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.OpenHAB;
import org.openhab.io.yamlcomposer.internal.core.EvaluationContext;

/**
 * The {@link YamlComposerEnvironmentVariableTest} contains tests for the {@code packages} functionality in the
 * {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Packaging Specification")
class YamlComposerEnvironmentVariableTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Environment Variable Tracking")
    class EnvTracking {

        @Test
        @DisplayName("Tracks environment variables using dot notation and single-quote bracket notation")
        void tracksReferencedEnvironmentVariables() throws IOException {
            Path main = writeFixture("env_main.yaml", """
                    setting: ${ENV.APP_CONFIG}
                    nested: ${ENV['CUSTOM_PATH']}
                    """);

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            loadWithTracking(main, trackedEnv);

            assertThat(trackedEnv, containsInAnyOrder(equalTo("APP_CONFIG"), equalTo("CUSTOM_PATH")));
        }

        @Test
        @DisplayName("Tracks only direct environment variable access, ignoring method calls")
        void tracksOnlyDirectEnvironmentVariableAccess() throws IOException {
            Path main = writeFixture("env_main.yaml", """
                    tracked_access:
                      dot_syntax: ${ENV.APP_CONFIG}
                      bracket_syntax: ${ENV['APP_NAME']}

                    untracked_access:
                      containsKey: ${ENV.containsKey('IGNORE_KEY_1')}
                      in_operator: ${'IGNORE_KEY_2' in ENV}
                      containsValue: ${ENV.containsValue('ignore_value')}
                      size: ${ENV.size}
                      keySet: ${ENV.keySet}
                      iteration:
                        !for env, value in ENV:
                          - ${env}: ${value}
                    """);

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            loadWithTracking(main, trackedEnv);

            assertThat(trackedEnv, containsInAnyOrder(equalTo("APP_CONFIG"), equalTo("APP_NAME")));
        }

        @Test
        @DisplayName("Tracks environment variables referenced inside included files")
        void tracksEnvironmentVariablesInIncludedFiles() throws IOException {
            writeFixture("env_include.yaml", "nested_setting: ${ENV.INCLUDED_SETTING}");
            Path main = writeFixture("env_main_with_include.yaml", """
                    primary_setting: ${ENV.PRIMARY_SETTING}
                    include_block: !include env_include.yaml
                    """);

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            loadWithTracking(main, trackedEnv);

            assertThat(trackedEnv, containsInAnyOrder(equalTo("PRIMARY_SETTING"), equalTo("INCLUDED_SETTING")));
        }

        @Test
        @DisplayName("Tracks environment variables referenced from inserted templates")
        void tracksEnvironmentVariablesInInsertedTemplates() throws IOException {
            Path main = writeFixture("env_template_main.yaml", """
                    templates:
                      env_tpl:
                        nested: ${ENV.TEMPLATE_SETTING}
                    result: !insert env_tpl
                    """);

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            loadWithTracking(main, trackedEnv);

            assertThat(trackedEnv, containsInAnyOrder(equalTo("TEMPLATE_SETTING")));
        }

        @Test
        @DisplayName("Tracks environment variables referenced through merge-key substitutions")
        void tracksEnvironmentVariablesInMergeKeys() throws IOException {
            Path main = writeFixture("env_merge_main.yaml", """
                    variables:
                      base:
                        setting: ${ENV.MERGED_SETTING}
                    target:
                      <<: ${base}
                    """);

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            loadWithTracking(main, trackedEnv);

            assertThat(trackedEnv, containsInAnyOrder(equalTo("MERGED_SETTING")));
        }

        @Test
        @DisplayName("Does not track any environment variables when none are referenced")
        void doesNotTrackEnvironmentVariablesWhenNoneReferenced() throws IOException {
            Path main = writeFixture("env_unreferenced.yaml", "setting: static_value");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            loadWithTracking(main, trackedEnv);

            assertThat(trackedEnv, empty());
        }
    }

    @Nested
    @DisplayName("ComposerUtils Header & Formatting Tests")
    class HeaderAndUtilsTests {

        @Test
        @DisplayName("Writes compiled output with tracked environment variables in the header")
        void writesCompiledOutputWithTrackedEnvironmentVariables() throws IOException {
            Path main = writeFixture("header_main.yaml", "value: ${ENV.MY_TEST_ENV}");
            Path output = Objects.requireNonNull(sharedTempDir).resolve("output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv);
            }

            assertThat(Files.exists(output), is(true));
            assertThat(Files.readString(output), containsString("MY_TEST_ENV"));
        }

        @Test
        @DisplayName("Env-Deps header round-trips environment variables containing commas accurately")
        void envDepsRoundTripsCommasInVarNames() throws IOException {
            Path main = writeFixture("env_comma_main.yaml", """
                    setting: ${ENV['YAML_COMPOSER,REVIEW']}
                    another: ${ENV['VAR_WITH_BACKSLASH\\,AND_COMMA']}
                    """);
            Path output = Objects.requireNonNull(sharedTempDir).resolve("compiled_comma.yaml");

            Map<String, String> envMap = Map.of("YAML_COMPOSER,REVIEW", "value1", "VAR_WITH_BACKSLASH\\,AND_COMMA",
                    "value2");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv, envMap);
            }

            // Environment should register as UNCHANGED when checked against the same environment map using file content
            String content = Files.readString(output);
            boolean changed = ComposerUtils.isEnvironmentChanged(content, trackedEnv, envMap);
            assertThat(changed, is(false));
        }

        @Test
        @DisplayName("Returns false initially and true when a tracked environment variable value changes")
        void returnsFalseInitiallyAndTrueWhenEnvironmentValueChanged() throws Exception {
            String envName = "DYNAMIC_TEST_VAR";
            Map<String, String> envMap = new HashMap<>();
            envMap.put(envName, "initial_value");

            Path main = writeFixture("env_change_main.yaml", "setting: ${ENV.DYNAMIC_TEST_VAR}");
            Path output = Objects.requireNonNull(sharedTempDir).resolve("env_change_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv, envMap);
            }

            // Verify it returns false when the environment value hasn't changed yet
            String content = Files.readString(output);
            assertThat(ComposerUtils.isEnvironmentChanged(content, trackedEnv, envMap), is(false));

            // Mutate the environment map value to simulate a change
            envMap.put(envName, "changed_value");

            // Verify it returns true after the change
            assertThat(ComposerUtils.isEnvironmentChanged(content, trackedEnv, envMap), is(true));
        }

        @Test
        @DisplayName("Detects change when a tracked environment variable transitions from absent to empty string")
        void detectsChangeWhenTrackedVariableChangesFromAbsentToEmpty() throws Exception {
            String envName = "ABSENT_TO_EMPTY_VAR";
            Map<String, String> envMap = new HashMap<>(); // Variable is absent (not present in envMap)

            Path main = writeFixture("env_absent_main.yaml", "setting: ${ENV.ABSENT_TO_EMPTY_VAR}");
            Path output = Objects.requireNonNull(sharedTempDir).resolve("env_absent_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            // Write compiled output header while the variable is absent (un-set)
            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv, envMap);
            }

            // Verify isEnvironmentChanged() returns false while the variable remains absent
            String content = Files.readString(output);
            assertThat(ComposerUtils.isEnvironmentChanged(content, trackedEnv, envMap), is(false));

            // Add the variable to the map with an empty string value ("")
            envMap.put(envName, "");

            // Verify that the transition from absent (null) to empty string ("") returns true
            assertThat(ComposerUtils.isEnvironmentChanged(content, trackedEnv, envMap), is(true));
        }

        @Test
        @DisplayName("Treats file without Env headers as legacy and returns true to trigger regeneration")
        void treatsFileWithoutEnvHeadersAsLegacy() throws IOException {
            Path main = writeFixture("legacy_main.yaml", "setting: static_value");
            Path output = Objects.requireNonNull(sharedTempDir).resolve("legacy_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();

            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv);
            }

            // Strip Env-Deps / Env-Hash headers to simulate a legacy file
            removeHeaderFromFile(output, "Env-Deps");
            removeHeaderFromFile(output, "Env-Hash");

            // Legacy files without Env headers must trigger regeneration (return true)
            String content = Files.readString(output);
            boolean changed = ComposerUtils.isEnvironmentChanged(content, trackedEnv);
            assertThat(changed, is(true));
        }

        @Test
        @DisplayName("Does not treat output as legacy when header exists with zero tracked environment variables")
        void doesNotRegenerateWhenHeaderExistsWithZeroTrackedVars() throws Exception {
            Map<String, String> envMap = Map.of("SOME_VAR", "some_val");

            Path main = writeFixture("zero_tracked_main.yaml", "setting: static_value");
            Path output = Objects.requireNonNull(sharedTempDir).resolve("zero_tracked_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            // Write output with zero tracked variables in trackedEnv
            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv, envMap);
            }

            // File has empty/zero-variable Env header -> NOT legacy -> returns false (no regeneration)
            String content = Files.readString(output);
            assertThat(ComposerUtils.isEnvironmentChanged(content, trackedEnv, envMap), is(false));
        }

        @Test
        @DisplayName("Handles line wrapping correctly when there are many environment variables")
        void handlesWrappingWithManyEnvs() throws IOException {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 30; i++) {
                sb.append("var").append(i).append(": ${ENV.BULK_ENV_VAR_").append(i).append("}\n");
            }

            Path main = writeFixture("wrapping_main.yaml", sb.toString());
            Path output = Objects.requireNonNull(sharedTempDir).resolve("wrapping_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv);
            }

            String written = Files.readString(output);
            assertThat(written, containsString("BULK_ENV_VAR_1"));
            assertThat(written, containsString("BULK_ENV_VAR_30"));
        }

        @Test
        @DisplayName("Ensures long environment variable names do not break apart across lines")
        void ensuresLongEnvNamesDoNotBreak() throws IOException {
            String longEnvName = "VERY_LONG_ENVIRONMENT_VARIABLE_NAME_FOR_TESTING_PURPOSES_1234567890_ABCDEFGHIJ";
            Path main = writeFixture("long_env_main.yaml", """
                    setting: ${ENV.%s}
                    """.formatted(longEnvName));
            Path output = Objects.requireNonNull(sharedTempDir).resolve("long_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv);
            }

            assertThat(Files.readString(output), containsString(longEnvName));
        }

        @Test
        @DisplayName("Handles missing header gracefully by checking environment change behavior")
        void handlesMissingHeader() throws IOException {
            Path main = writeFixture("no_header_main.yaml", """
                    setting: ${ENV.FALLBACK_ENV}
                    """);
            Path output = Objects.requireNonNull(sharedTempDir).resolve("no_header_output.yaml");

            Set<String> trackedEnv = ConcurrentHashMap.newKeySet();
            Object yamlObject = loadWithTracking(main, trackedEnv);

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                ComposerUtils.writeCompiledOutput(yamlObject, main, output, trackedEnv);
            }
            removeHeaderFromFile(output);

            String content = Files.readString(output);
            boolean changed = ComposerUtils.isEnvironmentChanged(content, trackedEnv);
            assertThat(changed, is(true));
        }

        @Test
        @DisplayName("Rewrites output when tracked dependency list changes even if compiled body is identical")
        void rewritesOutputWhenTrackedDependenciesChange() throws IOException {
            Map<String, String> envMap = Map.of("FOO", "same_value", "BAR", "same_value");

            Path main = writeFixture("dep_change_main.yaml", "setting: ${ENV.FOO}");
            Path output = Objects.requireNonNull(sharedTempDir).resolve("dep_change_output.yaml");

            // First compile pass: tracks FOO
            Set<String> trackedFoo = Set.of("FOO");
            Object yamlObjectFoo = loadWithTracking(main, ConcurrentHashMap.newKeySet());

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                boolean written = ComposerUtils.writeCompiledOutput(yamlObjectFoo, main, output, trackedFoo, envMap);
                assertThat(written, is(true));
            }

            String initialContent = Files.readString(output);
            assertThat(initialContent, containsString("FOO"));

            // Second compile pass: source changed to reference BAR instead of FOO.
            // Both produce "setting: same_value", so the compiled YAML body is identical.
            Set<String> trackedBar = Set.of("BAR");
            Object yamlObjectBar = loadWithTracking(main, ConcurrentHashMap.newKeySet());

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                boolean rewritten = ComposerUtils.writeCompiledOutput(yamlObjectBar, main, output, trackedBar, envMap);
                assertThat("Must not skip write when tracked env vars change from FOO to BAR", rewritten, is(true));
            }

            String updatedContent = Files.readString(output);
            assertThat(updatedContent, containsString("BAR"));
            assertThat(updatedContent, not(containsString("FOO")));
        }

        @Test
        @DisplayName("Skips second write when tracked variable iteration order differs between compilation passes")
        void skipsSecondWriteWhenTrackedVariableIterationOrderDiffers() throws IOException {
            Map<String, String> envMap = Map.of("B", "valB", "Q", "valQ");

            Path main = writeFixture("unordered_env_main.yaml", """
                    b_val: ${ENV.B}
                    q_val: ${ENV.Q}
                    """);
            Path output = Objects.requireNonNull(sharedTempDir).resolve("unordered_env_output.yaml");

            Object yamlObject = loadWithTracking(main, ConcurrentHashMap.newKeySet());

            try (MockedStatic<OpenHAB> openHABMock = mockOpenHabMetadata()) {
                Set<String> pass1Envs = new LinkedHashSet<>(List.of("B", "Q"));
                boolean firstWrite = ComposerUtils.writeCompiledOutput(yamlObject, main, output, pass1Envs, envMap);
                assertThat("First compile pass must write file", firstWrite, is(true));

                Set<String> pass2Envs = new LinkedHashSet<>(List.of("Q", "B"));
                boolean secondWrite = ComposerUtils.writeCompiledOutput(yamlObject, main, output, pass2Envs, envMap);
                assertThat("Second write must be skipped despite different set iteration order", secondWrite,
                        is(false));
            }
        }

        private void removeHeaderFromFile(Path file) throws IOException {
            List<String> lines = Files.readAllLines(file);
            List<String> cleaned = lines.stream().filter(line -> !line.startsWith("#")).toList();
            Files.write(file, cleaned);
        }

        private void removeHeaderFromFile(Path file, String header) throws IOException {
            List<String> lines = Files.readAllLines(file);
            String headerPrefix = "# " + header;
            List<String> cleaned = lines.stream().filter(line -> !line.startsWith(headerPrefix)).toList();
            Files.write(file, cleaned);
        }
    }

    private Object loadWithTracking(Path main, Set<String> trackedEnv) throws IOException {
        EvaluationContext context = new EvaluationContext(trackedEnv::add, sourceName -> null);
        return Objects.requireNonNull(YamlComposer.load(main, context, p -> {
        }, logSession, new ConcurrentHashMap<>()));
    }
}
