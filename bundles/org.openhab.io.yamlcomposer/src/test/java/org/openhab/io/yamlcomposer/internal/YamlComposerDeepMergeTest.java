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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the recursive semantics and precedence rules of the {@code !deep} directive.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@DisplayName("YAML Composer Deep Merge Directive Tests")
class YamlComposerDeepMergeTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Default Deep Merge Semantics")
    class DefaultDeepMerge {

        @Test
        @DisplayName("Preserves local value when local key is defined before the directive")
        void preservesLocalValueWhenDefinedBeforeDirective() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Light
                      !deep <<:
                        metadata:
                          alexa: Switch
                          autoupdate: true
                        type: Switch
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
            assertThat(getNestedValue(result, "target", "type"), equalTo("Switch"));
        }

        @Test
        @DisplayName("Preserves local value when local key is defined after the directive")
        void preservesLocalValueWhenDefinedAfterDirective() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      !deep <<:
                        metadata:
                          alexa: Switch
                          autoupdate: true
                      metadata:
                        alexa: Light
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
        }

        @Test
        @DisplayName("Folds missing defaults in a sequence without overwriting earlier entries")
        void sequenceFoldsMissingDefaultsWithoutOverwritingEarlierEntries() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Light
                      !deep <<:
                        - metadata: { alexa: Switch, autoupdate: true }
                        - metadata: { autoupdate: false, category: "Lighting" }
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
            assertThat(getNestedValue(result, "target", "metadata", "category"), equalTo("Lighting"));
        }
    }

    @Nested
    @DisplayName("List Merging Behaviors")
    class ListMerging {

        @Test
        @DisplayName("Appends source elements to an unmarked target list")
        void appendsElementsToList() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      tags:
                        - "light"
                      !deep "<< # first":
                        tags:
                          - "light"
                          - "switch"
                          - "indoor"
                    """);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) getNestedValue(result, "target", "tags");
            assertThat(tags, contains("light", "switch", "indoor"));
        }

        @Test
        @DisplayName("Omits !default items from target list when source list provides values during !deep merge")
        void defaultListItemsOmittedWhenSourceHasValues() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      tags:
                        - Control
                        - !default Power
                      !deep <<:
                        tags:
                          - Light
                    """);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) getNestedValue(result, "target", "tags");
            assertThat(tags, contains("Control", "Light"));
            assertThat(tags, not(hasItem("Power")));
        }

        @Test
        @DisplayName("Retains !default items in target list when source list is empty during !deep merge")
        void defaultListItemsRetainedWhenSourceIsEmpty() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      tags:
                        - Control
                        - !default Power
                      !deep <<:
                        tags: []
                    """);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) getNestedValue(result, "target", "tags");
            assertThat(tags, contains("Control", "Power"));
        }

        @Test
        @DisplayName("Retains non-tagged items in target list and appends source items during !deep merge")
        void nonDefaultListItemsRetainedAlongsideSourceValues() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      tags:
                        - Control
                        - Power
                      !deep <<:
                        tags:
                          - Light
                    """);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) getNestedValue(result, "target", "tags");
            assertThat(tags, contains("Control", "Power", "Light"));
        }
    }

    @Nested
    @DisplayName("Invalid Payload Handling")
    class InvalidPayloadHandling {

        @Test
        @DisplayName("Refuses processing when !deep payload is a sequence instead of a map")
        void refusesProcessingWhenPayloadIsSequence() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Light
                      !deep "<< # first layer":
                        - "invalid_sequence_item_1"
                        - "invalid_sequence_item_2"
                    """);

            // Local value preserved; sequence payload ignored
            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("Invalid !deep sequence element")));
        }

        @Test
        @DisplayName("Refuses processing when !deep payload is a scalar string")
        void refusesProcessingWhenPayloadIsScalar() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Light
                      !deep <<: "just_a_string"
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("Invalid !deep value")));
        }
    }

    @Nested
    @DisplayName("Key Ordering Semantics")
    class KeyOrdering {

        @Test
        @DisplayName("Preserves key ordering inside nested maps during deep merge")
        void deepMergePreservesKeyOrderingInsideNestedMaps() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    parent:
                      a_first: "local"
                      !deep "<< # second layer":
                        b_nested: "from_template"
                        c_nested: "from_template"
                      d_last: "local"
                    """);

            @SuppressWarnings("unchecked")
            Map<String, Object> parent = (Map<String, Object>) Objects.requireNonNull(result.get("parent"));

            assertThat(parent.keySet(), contains("a_first", "b_nested", "c_nested", "d_last"));
            assertThat(parent.keySet(), not(contains("a_first", "d_last", "b_nested", "c_nested")));
        }

        @Test
        @DisplayName("Recursively merges nested map keys while preserving key definition order")
        void deepMergeRecursivelyMergesNestedMapKeysPreservingOrder() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    settings:
                      network:
                        ip: "192.168.1.1"
                      !deep "<< # second":
                        network:
                          subnet: "255.255.255.0"
                          gateway: "192.168.1.254"
                        storage:
                          path: "/mnt/data"
                    """);

            @SuppressWarnings("unchecked")
            Map<String, Object> settings = (Map<String, Object>) Objects.requireNonNull(result.get("settings"));
            assertThat(settings.keySet(), contains("network", "storage"));

            @SuppressWarnings("unchecked")
            Map<String, Object> network = (Map<String, Object>) Objects.requireNonNull(settings.get("network"));
            assertThat(network.keySet(), contains("ip", "subnet", "gateway"));
        }
    }

    @Nested
    @DisplayName("Complex Composition Scenarios")
    class ComplexComposition {

        @Test
        @DisplayName("Applies multiple directives at the same mapping level in document order")
        void multipleDirectivesAtSameLevelAreAppliedInDocumentOrder() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Sensor
                      !deep "<< # first":
                        metadata:
                          alexa: Switch
                          autoupdate: true
                      !deep "<< # second":
                        metadata:
                          autoupdate: false
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Sensor"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
        }

        @Test
        @DisplayName("Recurses deep merge rules through multi-level nested maps")
        void deepMergeRecursesThroughMultiLevelMaps() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      config:
                        network:
                          host: "192.168.1.1"
                      !deep <<:
                        config:
                          network:
                            port: 8080
                    """);

            assertThat(getNestedValue(result, "target", "config", "network", "host"), equalTo("192.168.1.1"));
            assertThat(getNestedValue(result, "target", "config", "network", "port"), equalTo(8080));
        }

        @Test
        @DisplayName("Deep merge expands null local keys and merges incoming nested maps")
        void deepMergePopulatesAndMergesIntoNullLocalKeys() throws IOException {
            String yaml = """
                    items:
                      parent:
                        !deep "<< #top":
                          top: top
                          l1:
                            l2:
                              top: n1
                        l1:
                          l2:
                          foo: bar
                        !deep "<< #bottom":
                          bottom: bottom
                          l1:
                            l2:
                              bottom: n1
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "items", "parent", "top"), equalTo("top"));
            assertThat(getNestedValue(result, "items", "parent", "bottom"), equalTo("bottom"));
            assertThat(getNestedValue(result, "items", "parent", "l1", "foo"), equalTo("bar"));
            assertThat(getNestedValue(result, "items", "parent", "l1", "l2", "top"), equalTo("n1"));
            assertThat(getNestedValue(result, "items", "parent", "l1", "l2", "bottom"), equalTo("n1"));
        }

        @Test
        @DisplayName("Applies multiple deep-merge directives in document order")
        void appliesMultipleDeepMergeDirectivesInDocumentOrder() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Light
                      !deep "<< #first":
                        metadata:
                          autoupdate: true
                          category: "Lighting"
                      !deep "<< #second":
                        metadata:
                          alexa: Switch
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
            assertThat(getNestedValue(result, "target", "metadata", "category"), equalTo("Lighting"));
        }
    }

    @Nested
    @DisplayName("Directive Parsing Features")
    class DirectiveParsing {

        @Test
        @DisplayName("Ignores comment suffixes specified after directive parameters")
        void deepMergeIgnoresCommentSuffixes() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: Light
                      !deep "<< # default layer":
                        metadata:
                          alexa: Switch
                          autoupdate: true
                      !deep "<< # enforce settings":
                        metadata:
                          category: "Lighting"
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
            assertThat(getNestedValue(result, "target", "metadata", "category"), equalTo("Lighting"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "foobar", "overide", "addition", "12345" })
        @DisplayName("Refuses processing when an unrecognized option is provided")
        void refusesProcessingForInvalidOptions(String invalidOption) throws IOException {
            Map<Object, @Nullable Object> result = loadYaml(String.format("""
                    target:
                      metadata:
                        alexa: Light
                      !deep "%s":
                        metadata:
                          autoupdate: true
                    """, invalidOption));

            // Since processing is refused (returns null), deep-merge keys are ignored completely
            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(null));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("!deep: Invalid key")));
        }
    }

    @Nested
    @DisplayName("Local Key Conflict Resolution Semantics")
    class LocalKeyConflictResolution {

        @Test
        @DisplayName("!freeze retains the target value")
        void freezeLocalWinsIgnoresIncomingData() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: !freeze "Light"
                      !deep <<:
                        metadata:
                          alexa: "Switch"
                          autoupdate: true
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
        }

        @Test
        @DisplayName("!freeze locks a target map")
        void freezeLocksTargetMap() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata: !freeze
                        alexa: "Light"
                      !deep <<:
                        metadata:
                          alexa: "Switch"
                          autoupdate: true
                        other: "value"
                    """);

            assertThat(getNestedValue(result, "target", "metadata"), equalTo(Map.of("alexa", "Light")));
            assertThat(getNestedValue(result, "target", "other"), equalTo("value"));
        }

        @Test
        @DisplayName("!freeze: Preserves immutability across multiple sequential !deep merges and unwraps cleanly")
        void freezeSurvivesMultipleDeepMerges() throws IOException {
            String yaml = """
                    map:
                      killme: !freeze
                        do: not
                        replace: me
                      !deep <<:
                        killme:
                          do: replace
                          other: first_attempt
                      !deep <<:
                        killme:
                          do: replace
                          other: second_attempt
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            @SuppressWarnings("unchecked")
            Map<String, Object> killmeMap = (Map<String, Object>) Objects
                    .requireNonNull(getNestedValue(data, "map", "killme"));

            assertThat("The frozen key 'do' must retain its original value", killmeMap.get("do"), is("not"));
            assertThat("The frozen key 'replace' must retain its original value", killmeMap.get("replace"), is("me"));
            assertThat("No merged keys from any !deep pass should leak into the frozen map", killmeMap,
                    not(hasKey("other")));
        }

        @Test
        @DisplayName("!default permits the incoming value")
        void defaultPermitsIncomingValue() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: !default "Light"
                      !deep <<:
                        metadata:
                          alexa: "Switch"
                          autoupdate: true
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Switch"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
        }

        @Test
        @DisplayName("!default map allows incoming conflicts while retaining unrelated target keys")
        void defaultMapMergesWithIncomingPriority() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      main: !default
                        foo: bar
                        baz: moo
                      !deep <<:
                        main:
                          foo: qux
                          qux: quux
                      no_default:
                        foo: bar
                      !deep "<< # no-default":
                        no_default:
                          foo: qux
                    """);

            assertThat(getNestedValue(result, "target", "main", "foo"), equalTo("qux"));
            assertThat(getNestedValue(result, "target", "main", "baz"), equalTo("moo"));
            assertThat(getNestedValue(result, "target", "main", "qux"), equalTo("quux"));
            assertThat(getNestedValue(result, "target", "no_default", "foo"), equalTo("bar"));
        }

        @Test
        @DisplayName("!remove (Neither Wins): Suppresses key and omits it entirely from output upon merge match")
        void removeSuppressesKeyFromFinalOutput() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    target:
                      metadata:
                        alexa: "Light"
                        autoupdate: !remove true
                      !deep <<:
                        metadata:
                          autoupdate: false
                          category: "Lighting"
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "category"), equalTo("Lighting"));

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) Objects
                    .requireNonNull(getNestedValue(result, "target", "metadata"));
            assertThat(metadata.containsKey("autoupdate"), equalTo(false));
        }

        @Test
        @DisplayName("Collision control tags are harmless without a merge")
        void collisionControlTagsWithoutMerge() throws IOException {
            Map<Object, @Nullable Object> data = loadYaml("""
                    settings:
                      fallback: !default
                        value: default
                      frozen: !freeze
                        value: frozen
                      replaced: !replace
                        value: replaced
                      removed: !remove
                    """);

            assertThat(getNestedValue(data, "settings", "fallback", "value"), equalTo("default"));
            assertThat(getNestedValue(data, "settings", "frozen", "value"), equalTo("frozen"));
            assertThat(getNestedValue(data, "settings", "replaced", "value"), equalTo("replaced"));
            assertThat((Map<?, ?>) getNestedValue(data, "settings"), not(hasKey("removed")));
        }
    }

    @Nested
    @DisplayName("Integration Features")
    class Integration {

        @Test
        @DisplayName("Evaluates !deep in combination with a local inserted template")
        void deepMergeWithInsertedTemplateAppliesTemplateRecursively() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    templates:
                      sensorDefaults:
                        metadata:
                          alexa: Switch
                          autoupdate: true
                        type: Switch

                    target:
                      metadata:
                        alexa: Light
                      !deep <<: !insert
                        template: sensorDefaults
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(true));
            assertThat(getNestedValue(result, "target", "type"), equalTo("Switch"));
        }

        @Test
        @DisplayName("Evaluates !deep over a sequence of local inserted templates")
        void deepMergeWithInsertedSequenceOfTemplatesAppliesTemplatesInOrder() throws IOException {
            Map<Object, @Nullable Object> result = loadYaml("""
                    templates:
                      base:
                        metadata:
                          alexa: Switch
                          autoupdate: false
                      override:
                        metadata:
                          autoupdate: true
                          category: "Lighting"

                    target:
                      metadata:
                        alexa: Light
                      !deep <<:
                        - !insert
                          template: base
                        - !insert
                          template: override
                    """);

            assertThat(getNestedValue(result, "target", "metadata", "alexa"), equalTo("Light"));
            assertThat(getNestedValue(result, "target", "metadata", "autoupdate"), equalTo(false));
            assertThat(getNestedValue(result, "target", "metadata", "category"), equalTo("Lighting"));
        }
    }
}
