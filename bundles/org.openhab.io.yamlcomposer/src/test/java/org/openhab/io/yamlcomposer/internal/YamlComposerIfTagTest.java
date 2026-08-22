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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerIfTagTest} contains tests for the !if, !elif, !elseif, !elsif, and !else tag functionality
 * in the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Conditional Tags (!if, !elif, !elseif, !elsif, !else)")
class YamlComposerIfTagTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Syntax Forms")
    class SyntaxForms {

        @Nested
        @DisplayName("Key-Level Form")
        class KeyLevelFormTests {

            @Test
            @DisplayName("Resolves simple key-level form")
            void resolvesSimpleForm() throws IOException {
                String yaml = """
                        test:
                          !if '"bar" == "bar"':
                            foo: bar
                          other: baz
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Key-level form should resolve correctly when true", getNestedValue(data, "test"),
                        is(Map.of("foo", "bar", "other", "baz")));
            }

            @Test
            @DisplayName("Resolves multiple key-level forms")
            void testMultipleDirectExpressions() throws IOException {
                String yaml = """
                        variables:
                          item_count: 20
                        test:
                          !if '"bar" == "bar"':
                            foo: bar
                          !if item_count > 10:
                            label: a lot
                          !if item_count <= 10:
                            label: not a lot
                          other: baz
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                Map<Object, @Nullable Object> expected = Map.of("foo", "bar", "label", "a lot", "other", "baz");

                assertThat(getNestedValue(data, "test"), is(expected));
            }

            @Test
            @DisplayName("Resolves key-level form with substitution pattern")
            void testDirectExpressionWithSubstitution() throws IOException {
                String yaml = """
                        variables:
                          operator: ">"
                        foo:
                          !if 75 ${operator} 50:
                            result: "High"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "foo", "result"), is("High"));
            }

            @Nested
            @DisplayName("Disambiguating Comments with Hashes")
            class DisambiguatingComments {

                @Test
                @DisplayName("Should support trailing comments when !if expression contains hashes inside single quotes")
                void testDisambiguatingCommentsWithSingleQuoteHashes() throws IOException {
                    String yaml = """
                            test:
                              !if "'a#b' == 'a#b' # check string equality":
                                result: "match"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("match"));
                }

                @Test
                @DisplayName("Should support trailing comments when !if expression contains hashes inside double quotes")
                void testDisambiguatingCommentsWithDoubleQuoteHashes() throws IOException {
                    String yaml = """
                            test:
                              !if '"a#b" == "a#b" # check string equality':
                                result: "match"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("match"));
                }

                @Test
                @DisplayName("Should support trailing comments when !if expression contains hashes inside brackets/lists")
                void testDisambiguatingCommentsWithBracketHashes() throws IOException {
                    String yaml = """
                            test:
                              !if "'a#b' in ['a#b', 'c'] # check list containment":
                                result: "match"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("match"));
                }
            }

            @Nested
            @DisplayName("Block-Level Adjacency and Variants (!elif, !elseif, !elsif, !else)")
            class BlockLevelAdjacencyTests {

                @Test
                @DisplayName("Resolves block-level !if followed by !elseif and !else")
                void resolvesBlockLevelChain() throws IOException {
                    String yaml = """
                            variables:
                              val: 2
                            test:
                              !if val == 1:
                                result: "one"
                              !elseif val == 2:
                                result: "two"
                              !else ~:
                                result: "other"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("two"));
                }

                @ParameterizedTest
                @ValueSource(strings = { "!elif", "!elseif", "!elsif" })
                @DisplayName("Resolves alternative keywords for else-if blocks")
                void resolvesAlternativeElseIfKeywords(String keyword) throws IOException {
                    String yaml = """
                            variables:
                              val: 2
                            test:
                              !if val == 1:
                                result: "one"
                              %s val == 2:
                                result: "match"
                              !else ~:
                                result: "fallback"
                            """.formatted(keyword);

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("match"));
                }

                @Test
                @DisplayName("Warns and ignores orphan !elseif without a preceding !if")
                void warnsOnOrphanElseIf() throws IOException {
                    String yaml = """
                            test:
                              !elseif true:
                                result: "orphan"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(data.get("test"), is(not(Map.of("result", "orphan"))));
                    assertThat(logSession.getTrackedWarnings(),
                            hasItem(containsString("!elseif without preceding !if")));
                }

                @Test
                @DisplayName("Warns and ignores orphan !else without a preceding !if")
                void warnsOnOrphanElse() throws IOException {
                    String yaml = """
                            test:
                              !else ~:
                                result: "orphan"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(data.get("test"), is(not(Map.of("result", "orphan"))));
                    assertThat(logSession.getTrackedWarnings(), hasItem(containsString("!else without preceding !if")));
                }

                @Test
                @DisplayName("Enforces mutual exclusivity in block chains (stops at first truthy branch)")
                void enforcesMutualExclusivityInBlocks() throws IOException {
                    String yaml = """
                            test:
                              !if true:
                                result: "first"
                              !elseif true:
                                result: "second"
                              !else ~:
                                result: "third"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("first"));
                }
            }

            @Nested
            @DisplayName("Chain Integrity and Interruption")
            class ChainIntegrityTests {

                @Test
                @DisplayName("Breaks chain and warns on orphan !else when separated by an unrelated block")
                void breaksChainOnInterveningBlock() throws IOException {
                    String yaml = """
                            test:
                              !if false:
                                result: "first"
                              unrelated_key: "interruption"
                              !else ~:
                                result: "orphan"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    // The 'first' branch evaluates successfully, but the !else is orphaned by the interruption
                    assertNull(getNestedValue(data, "test", "result"));
                    assertThat(getNestedValue(data, "test", "unrelated_key"), is("interruption"));

                    // The orphaned !else should be ignored and trigger a warning
                    assertThat(logSession.getTrackedWarnings(), hasItem(containsString("!else without preceding !if")));
                }

                @Test
                @DisplayName("Breaks chain and warns on orphan !else when separated by a !for directive")
                void breaksChainOnInterveningForDirective() throws IOException {
                    String yaml = """
                            test:
                              !if true:
                                result: "first"
                              !for i in [1]:
                                item_${i}: value
                              !else ~:
                                result: "orphan"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "result"), is("first"));
                    assertThat(getNestedValue(data, "test", "item_1"), is("value"));
                    assertThat(logSession.getTrackedWarnings(), hasItem(containsString("!else without preceding !if")));
                }

                @Test
                @DisplayName("Starts a brand new independent if-chain when encountering a subsequent !if")
                void startsNewChainOnSubsequentIf() throws IOException {
                    String yaml = """
                            test:
                              !if true:
                                first_result: "one"
                              !else ~:
                                first_result: "fallback-one"
                              !if true:
                                second_result: "two"
                              !else ~:
                                second_result: "fallback-two"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "test", "first_result"), is("one"));
                    assertThat(getNestedValue(data, "test", "second_result"), is("two"));
                }
            }
        }

        @Nested
        @DisplayName("Mapping Form")
        class MappingFormTests {
            @Test
            @DisplayName("Resolves mapping form with single condition")
            void resolvesMappingForm() throws IOException {
                String yaml = """
                        test: !if
                          if: true
                          then: "matched"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Mapping form should resolve correctly when true", getNestedValue(data, "test"),
                        is("matched"));
            }
        }

        @Nested
        @DisplayName("Sequence Form")
        class SequenceFormTests {
            @Test
            @DisplayName("Resolves sequence form with multi-branch evaluation")
            void resolvesSequenceForm() throws IOException {
                String yaml = """
                        test: !if
                          - if: false
                            then: "first"
                          - elseif: true
                            then: "second"
                          - else: "third"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Sequence form should pick the first truthy branch", getNestedValue(data, "test"),
                        is("second"));
            }

            @Test
            @DisplayName("Resolves to else value when no branches match")
            void resolvesElseFallback() throws IOException {
                String yaml = """
                        test: !if
                          - if: false
                            then: "no"
                          - else: "fallback"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Should return else value if all conditions fail", getNestedValue(data, "test"),
                        is("fallback"));
            }
        }
    }

    @Nested
    @DisplayName("Expression Evaluation")
    class ExpressionEvaluation {

        @Test
        @DisplayName("Evaluates direct expressions")
        void evaluatesExpressionsInConditions() throws IOException {
            String yaml = """
                    variables:
                      num: 5
                    test: !if
                      if: num > 3
                      then: "greater"
                      else: "lesser"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("Should evaluate expression and resolve to 'greater'", getNestedValue(data, "test"),
                    is("greater"));
        }

        @Test
        @DisplayName("Evaluates expressions in ${} pattern")
        void evaluatesSubExpressionsInConditions() throws IOException {
            String yaml = """
                    variables:
                      threshold: 10
                    test: !if
                      if: ${threshold} > 5
                      then: "high"
                      else: "low"
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);
            assertThat("Should evaluate expression in condition", getNestedValue(data, "test"), is("high"));
        }
    }

    @Nested
    @DisplayName("Truthiness and Logic")
    class Truthiness {

        @ParameterizedTest
        @ValueSource(strings = { "[]", "{}", "0", "'  '", "false", "'false'", "null" })
        @DisplayName("Treats empty collections and zero as falsy")
        void handlesFalsyValues(String ifValue) throws IOException {
            String yaml = "value: !if { if: %s, then: 'yes', else: 'no' }".formatted(ifValue);

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "value"), is("no"));
        }

        @ParameterizedTest
        @ValueSource(strings = { "[1]", "42", "true", "'true'", "\"'hello'\"", "{ key: value }", "[item]" })
        @DisplayName("Treats non-empty collections and strings as truthy")
        void handlesTruthyValues(String ifValue) throws IOException {
            String yaml = """
                    value: !if
                      if: %s
                      then: 'yes'
                      else: 'no'
                    """.formatted(ifValue);

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "value"), is("yes"));
        }
    }

    @Nested
    @DisplayName("Short-Circuiting")
    class ShortCircuiting {

        // This test verifies that our way of asserting the execution of a failed branch is working as intended.
        // This ensures we won't have false positives in the subsequent tests where the test passes even though the
        // branch was evaluated.
        @Test
        @DisplayName("Should resolve and log warning for tags in satisfied key-level branches")
        void keyLevelFormEvaluatesSatisfiedBranch() throws IOException {
            String yaml = """
                    test:
                      !if true:
                        result: !include non_existent_file.yaml
                      !else ~:
                        result: "fallback"
                    """;

            loadYaml(yaml);

            assertThat("The satisfied key-level branch should attempt evaluation and trigger an include warning",
                    logSession.getTrackedWarnings(), hasItem(containsString("Failed to process !include")));
        }

        @Test
        @DisplayName("Should not resolve tags in unsatisfied key-level branches")
        void keyLevelFormShortCircuitsUnsatisfiedBranch() throws IOException {
            String yaml = """
                    test:
                      !if false:
                        result: !include non_existent_file.yaml
                      !elseif true:
                        result: "true"
                      !elseif false:
                        result: !include non_existent_file.yaml
                      !else ~:
                        result: !include non_existent_file.yaml
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("The unsatisfied key-level branch should be short-circuited",
                    getNestedValue(data, "test", "result"), is("true"));
            assertThat("The inactive branch should not trigger any include warnings or errors",
                    logSession.getTrackedWarnings(), not(hasItem(containsString("Failed to process !include"))));
        }

        @Test
        @DisplayName("Should not resolve tags in inactive mapping branches")
        void mappingFormShortCircuitsInactiveBranches() throws IOException {
            String yaml = """
                    test: !if
                      if: true
                      then: "active"
                      else: !include non_existent_file.yaml
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("The active branch should be the one resolved", data.get("test"), is("active"));
            assertThat("The inactive branch should not trigger any warnings", logSession.getTrackedWarnings(),
                    not(hasItem(containsString("Failed to process !include"))));
        }
    }

    @Nested
    @DisplayName("Nested Tag Integration")
    class NestedIntegration {

        @Test
        @DisplayName("Resolves nested expression and !insert tags within value fields")
        void resolvesNestedTags() throws IOException {
            String yaml = """
                    variables:
                      name: "World"
                    templates:
                      snippet: "Inserted Content"
                    test: !if
                      if: true
                      then: "Hello, ${name}!"
                    other: !if
                      if: true
                      then: !insert snippet
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("Nested substitution inside !if should resolve", getNestedValue(data, "test"),
                    is("Hello, World!"));
            assertThat("Nested !insert inside !if should resolve", getNestedValue(data, "other"),
                    is("Inserted Content"));
        }

        @Test
        @DisplayName("Resolves deeply nested !if tags")
        void resolvesDeeplyNestedIf() throws IOException {
            String yaml = """
                    test: !if
                      if: true
                      then: !if
                        if: true
                        then: "deep"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("Recursive resolution should handle nested !if tags", getNestedValue(data, "test"), is("deep"));
        }

        @Test
        @DisplayName("Strips off null items in a list when !if is false and no else is provided")
        @SuppressWarnings("null")
        void stripsNullInSequence() throws IOException {
            String yaml = """
                    list:
                      - item1
                      - !if
                        if: false
                        then: "item2"
                      - item3
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            List<?> list = (List<?>) data.get("list");

            assertThat("The null result should be stripped, reducing list size", list.size(), is(2));
            assertThat("The first item should be preserved", list.get(0), is("item1"));
            assertThat("The third item should shift to index 1", list.get(1), is("item3"));
        }

        @Test
        @DisplayName("Does not strip items in a list when !if resolves to a non-null value")
        @SuppressWarnings("null")
        void preservesElseInSequence() throws IOException {
            String yaml = """
                    list:
                      - !if
                        if: false
                        then: "no"
                        else: "yes"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            List<?> list = (List<?>) data.get("list");

            assertThat("The list size should be 1", list.size(), is(1));
            assertThat("The item should resolve to the else value", list.get(0), is("yes"));
        }

        @Test
        @DisplayName("Merges list returned by key-level branch into parent list")
        @SuppressWarnings("null")
        void keyLevelFormMergesListIntoParentList() throws IOException {
            String yaml = """
                    list:
                      - "item1"
                      - !if true:
                          - "item2"
                          - "item3"
                        !else ~:
                          - "fallback"
                      - "item4"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            List<?> list = (List<?>) data.get("list");

            assertThat("The parent list size should reflect flattened items from the satisfied branch", list.size(),
                    is(4));
            assertThat("Items from the active key-level branch should be merged inline into the parent list", list,
                    is(List.of("item1", "item2", "item3", "item4")));
        }

        @Test
        @DisplayName("Returns nested list as single value for mapping form inside parent list")
        @SuppressWarnings("null")
        void mappingFormReturnsNestedList() throws IOException {
            String yaml = """
                    list:
                      - "item1"
                      - !if
                          if: true
                          then:
                            - "item2"
                            - "item3"
                          else:
                            - "fallback"
                      - "item4"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            List<?> list = (List<?>) data.get("list");

            assertThat("Mapping form acts as a value node, resulting in a parent list size of 3", list.size(), is(3));
            assertThat("The evaluated list should remain nested within the parent list slot", list,
                    is(List.of("item1", List.of("item2", "item3"), "item4")));
        }

        @Test
        @DisplayName("Returns nested list as single value for sequence form inside parent list")
        @SuppressWarnings("null")
        void sequenceFormReturnsNestedList() throws IOException {
            String yaml = """
                    list:
                      - "item1"
                      - !if
                          - if: true
                            then:
                              - "item2"
                              - "item3"
                          - else:
                              - "fallback"
                      - "item4"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            List<?> list = (List<?>) data.get("list");

            assertThat("Sequence form acts as a value node, resulting in a parent list size of 3", list.size(), is(3));
            assertThat("The evaluated list should remain nested within the parent list slot", list,
                    is(List.of("item1", List.of("item2", "item3"), "item4")));
        }
    }

    @Nested
    @DisplayName("Integration and Advanced Composition Tests")
    class Integration {

        @Test
        @DisplayName("Evaluates !if conditional directives within packages section")
        void ifDirectiveInsidePackagesSection() throws IOException {
            String yaml = """
                    variables:
                      enable_feature: true
                    packages:
                      !if enable_feature:
                        feature_pkg:
                          items:
                            Switch_1:
                              label: "Enabled"
                      !else ~:
                        feature_pkg:
                          items:
                            Switch_1:
                              label: "Disabled"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "items", "Switch_1", "label"), is("Enabled"));
        }
    }
}
