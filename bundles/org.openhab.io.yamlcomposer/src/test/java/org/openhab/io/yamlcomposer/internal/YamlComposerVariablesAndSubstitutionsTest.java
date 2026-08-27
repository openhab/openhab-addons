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
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerVariablesAndSubstitutionsTest} contains tests for the variables and substitutions
 * functionality in the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Variables and Substitutions")
class YamlComposerVariablesAndSubstitutionsTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Resolution and Scoping")
    class ResolutionAndScoping {

        @Test
        @DisplayName("Resolves plain variables in simple string expressions")
        void resolvesPlainVariables() throws IOException {
            String yaml = """
                    variables:
                      greeting: "Hello"
                      target: "World"
                    test: "${greeting}, ${target}!"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("Basic variable resolution should work in a simple string expression",
                    getNestedValue(data, "test"), is("Hello, World!"));
        }

        @Test
        @DisplayName("Supports defining variables at end of file")
        void supportsVariablesDefinedAtEndOfFile() throws IOException {
            String yaml = """
                    test:
                      result: "${late_var}"

                    variables:
                      late_var: "hoisted"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("Variables should be resolvable regardless of their position in the file",
                    getNestedValue(data, "test", "result"), is("hoisted"));
        }

        @Test
        @DisplayName("Resolves predefined system variables (__FILE__, __DIRECTORY__, etc)")
        void resolvesPredefinedSystemVariables() throws IOException {
            Path file = writeFixture("predefinedVars.inc.yaml", """
                    file: "${__FILE__}"
                    filename: "${__FILE_NAME__}"
                    ext: "${__FILE_EXT__}"
                    path: "${__DIRECTORY__}"
                    openhab_conf: "${OPENHAB_CONF}"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(file);

            assertThat("The filename variable should be extracted from the file name", data.get("filename"),
                    is("predefinedVars.inc"));

            assertThat("The extension variable should be extracted from the file extension", data.get("ext"),
                    is("yaml"));

            assertThat("The directory variable should point to the file's parent folder", (String) data.get("path"),
                    containsString(file.getParent().getFileName().toString()));

            assertThat("System environment variables must be injected into the resolution context",
                    (String) data.get("openhab_conf"), is(not(emptyOrNullString())));
        }

        @Test
        @DisplayName("Protects predefined variables from user/include overrides")
        void protectsPredefinedVariablesFromOverrides() throws IOException {
            writeFixture("predefinedVarsOverride.inc.yaml", """
                    filename: "${__FILE_NAME__}"
                    """);

            Path mainFile = writeFixture("predefinedVarsOverride.yaml", """
                    variables:
                      __FILE_NAME__: "main_override"

                    filename: "${__FILE_NAME__}"

                    include: !include
                      file: predefinedVarsOverride.inc.yaml
                      vars:
                        __FILE_NAME__: "include_override"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(mainFile);

            assertThat("System variables in the main file should ignore the local 'variables' block overrides",
                    data.get("filename"), is("predefinedVarsOverride"));

            assertThat("System variables in an include should ignore 'vars' passed via !include",
                    getNestedValue(data, "include", "filename"), is("predefinedVarsOverride.inc"));
        }

        @ParameterizedTest
        // We specifically allow package_id to be overridden, so it is not included in this test
        @ValueSource(strings = { "OPENHAB_CONF", "OPENHAB_USERDATA", "__FILE__", "__FILE_NAME__", "__FILE_EXT__",
                "__DIRECTORY__", "__DIR__", "ENV", "VARS", "ARGS" })
        @DisplayName("Logs a warning when attempting to override predefined variables")
        void logsWarningWhenOverridingPredefinedVariables(String varName) throws IOException {
            String assertionMessage = "Attempting to override a predefined variable %s within a variables: block should log a warning"
                    .formatted(varName);

            // 1. Test top-level variables block
            Path mainFile = writeFixture("predefinedVarsOverride.yaml", """
                    variables:
                      %s: "invalid"

                    foo: ${%s}
                    """.formatted(varName, varName));
            Map<Object, @Nullable Object> result = loadFixture(mainFile);

            assertThat(assertionMessage, logSession.getTrackedWarnings(),
                    hasItem(containsString("Cannot redefine special variable")));

            assertThat(getNestedValue(result, "foo"), is(not("invalid")));

            // Clear the log session to ensure the next assertion is testing the new composition
            logSession.flush();

            // 2. Test inline !var directive
            mainFile = writeFixture("predefinedVarsInlineOverride.yaml", """
                    !var %s: "invalid"
                    foo: ${%s}
                    """.formatted(varName, varName));
            result = loadFixture(mainFile);

            assertionMessage = "Attempting to override a predefined variable %s with !var should log a warning"
                    .formatted(varName);
            assertThat(assertionMessage, logSession.getTrackedWarnings(),
                    hasItem(containsString("Cannot redefine special variable")));

            assertThat(getNestedValue(result, "foo"), is(not("invalid")));
        }

        @Test
        @DisplayName("Loads entire variables block from an !include file")
        void loadsEntireVariablesBlockFromInclude() throws IOException {
            writeFixture("vars_file.inc.yaml", """
                    external_var: "from_file"
                    another_var: "hello"
                    """);

            Path mainFile = writeFixture("main.yaml", """
                    variables: !include vars_file.inc.yaml

                    result: "${external_var} ${another_var}"
                    """);

            Map<Object, @Nullable Object> data = loadFixture(mainFile);

            assertThat("Variables from the included file should be available in the global context",
                    getNestedValue(data, "result"), is("from_file hello"));
        }

        @Test
        @DisplayName("Loads value from an include file")
        void loadsVariablesFromIncludeFiles() throws IOException {
            writeFixture("variableFromInclude.inc.yaml", "qux");

            Path mainFile = writeFixture("variableFromInclude.yaml", """
                    variables:
                      foo: !include variableFromInclude.inc.yaml

                    included_value: ${foo}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(mainFile);

            assertThat("Simple inclusion should work", getNestedValue(data, "included_value"), is("qux"));
        }

        @Test
        @DisplayName("Loads value from an include files with vars")
        void loadsVariablesFromIncludeFilesWithVars() throws IOException {
            // This also tests that a simple scalar inside an include file
            // will resolve substitutions using the inherited context.
            writeFixture("include.inc.yaml", "${var}");

            Path mainFile = writeFixture("main.yaml", """
                    variables:
                      foo: !include
                        file: include.inc.yaml
                        vars:
                          var: qux

                    included_value: ${foo}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(mainFile);

            assertThat("Simple inclusion should work", getNestedValue(data, "included_value"), is("qux"));
        }

        @Test
        @DisplayName("Merge keys at variables TOP-LEVEL with !include placeholder")
        void mergeKeysAtVariablesMapLevel() throws IOException {
            writeFixture("base_variables.yaml", """
                    base_template: from_base
                    """);

            Path mainFile = writeFixture("main.yaml", """
                    variables:
                      <<: !include base_variables.yaml

                    merged_variable: ${base_template}
                    """);

            Map<Object, @Nullable Object> data = loadFixture(mainFile);

            assertThat(getNestedValue(data, "merged_variable"), equalTo("from_base"));
        }

        @Test
        @DisplayName("Merge keys in variables block populate defaults before explicit variables evaluate")
        void mergeKeysWithExplicitVariablesInVariablesBlock() throws IOException {
            String yaml = """
                    variables:
                      <<:
                        domain: "example.com"
                        port: 8080
                      port: 9090
                      host_url: "https://${domain}:${port}"

                    result_domain: "${domain}"
                    result_port: "${port}"
                    result_url: "${host_url}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("result_domain"), is("example.com"));
            assertThat(data.get("result_port"), is(9090));
            assertThat(data.get("result_url"), is("https://example.com:9090"));
        }

        @Test
        @DisplayName("Supports substitution within variables block (Recursive resolution)")
        void supportsSubstitutionWithinVariablesBlock() throws IOException {
            String yaml = """
                    variables:
                      a: "root"
                      b: "${a}-to-middle"
                      c: "${b}-to-leaf"

                    test:
                      result: "${c}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat("Variables must support self-referential resolution", getNestedValue(data, "test", "result"),
                    is("root-to-middle-to-leaf"));
        }
    }

    @Nested
    @DisplayName("Substitution Syntax")
    class SubstitutionSyntax {

        @Test
        @DisplayName("Handles quoting and escaping in expressions")
        void handlesQuotingAndEscaping() throws IOException {
            String yaml = """
                    variables:
                      foo: value1

                    plain: ${foo}
                    double_quoted: "${foo}"
                    single_quoted: '${foo}'
                    braces_in_double_quotes: "${'${}'}"
                    braces_in_single_quotes: '${"${}"}'
                    """;
            Map<Object, Object> data = loadYaml(yaml);

            assertThat(data.get("plain"), is("value1"));
            assertThat(data.get("double_quoted"), is("value1"));
            assertThat(data.get("single_quoted"), is("value1"));
            assertThat(data.get("braces_in_double_quotes"), is("${}"));
            assertThat(data.get("braces_in_single_quotes"), is("${}"));
        }

        @Test
        @DisplayName("Handles empty and null variable values")
        void handlesEmptyAndNullValues() throws IOException {
            String yaml = """
                    variables:
                      nonnull: value1

                    empty: ${}
                    null_value: ${null}
                    null_string: "${null}"
                    padded: ${   nonnull    }
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("empty"), is(nullValue()));
            assertThat(data.get("null_value"), is(nullValue()));
            assertThat(data.get("null_string"), is(nullValue()));
            assertThat(data.get("padded"), is("value1"));
        }

        @Test
        @DisplayName("Handles empty maps and object comparisons")
        void handlesEmptyMap() throws IOException {
            String yaml = """
                    empty_map: ${ {} }
                    compare_empty_maps: ${ {} == {} }
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("empty_map"), instanceOf(Map.class));
            assertThat((Map<?, ?>) data.get("empty_map"), is(anEmptyMap()));
            assertThat(data.get("compare_empty_maps"), equalTo(true));
        }

        @Test
        @DisplayName("Handles special characters in variable names via VARS lookup")
        void handlesSpecialVariableNames() throws IOException {
            String yaml = """
                    variables:
                      "varname-with-dash": dashvalue
                      "varname with space": spacevalue

                    vars_with_dash: ${VARS["varname-with-dash"]}
                    vars_with_space: ${VARS['varname with space']}
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("vars_with_dash"), is("dashvalue"));
            assertThat(data.get("vars_with_space"), is("spacevalue"));
        }

        @Test
        @DisplayName("Handles basic data types and mathematical expressions")
        void handlesDataTypesAndMath() throws IOException {
            String yaml = """
                    variables:
                      one: 1

                    int_const: ${1 + 1}
                    int_var_math: ${one * 5}
                    int_quoted: "${100}"
                    string: ${'100'}
                    string_quoted: "${'100'}"
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("int_const"), is(2));
            assertThat(data.get("int_var_math"), is(5));
            assertThat(data.get("int_quoted"), is(100));
            assertThat(data.get("string"), is("100"));
            assertThat(data.get("string_quoted"), is("100"));
        }

        @Test
        @DisplayName("Navigates complex mapping and list structures")
        void navigatesComplexStructures() throws IOException {
            String yaml = """
                    variables:
                      mapping: { foo: bar }
                      list: [item0]
                    map_lookup: ${mapping.foo}
                    list_lookup: ${list[0]}
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("map_lookup"), is("bar"));
            assertThat(data.get("list_lookup"), is("item0"));
        }

        @Test
        @DisplayName("Allows execution of Java String methods within expressions")
        void executesJavaObjectMethod() throws IOException {
            String yaml = """
                    repeated: ${'bar'.repeat(2) + 'ian'}
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("repeated"), is("barbarian"));
        }
    }

    @Nested
    @DisplayName("ExpressionEvaluator")
    class ExpressionEvaluator {

        @Nested
        @DisplayName("Ranges and Sequences")
        class RangesAndSequencesTests {

            @Nested
            @DisplayName("Standard range() Function")
            class StandardRangeFunctionTests {

                @Test
                @DisplayName("Should evaluate single-argument range(stop)")
                void testSingleArgumentRange() throws IOException {
                    String yaml = """
                            numbers: "${range(3)}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(0, 1, 2));
                }

                @Test
                @DisplayName("Should evaluate range(start, stop)")
                void testStartStopRange() throws IOException {
                    String yaml = """
                            numbers: "${range(1, 4)}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(1, 2, 3));
                }

                @Test
                @DisplayName("Should evaluate positive step range(start, stop, step)")
                void testPositiveStepRange() throws IOException {
                    String yaml = """
                            numbers: "${range(0, 6, 2)}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(0, 2, 4));
                }

                @Test
                @DisplayName("Should evaluate negative step range(start, stop, negative_step)")
                void testNegativeStepRange() throws IOException {
                    String yaml = """
                            numbers: "${range(5, 0, -2)}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(5, 3, 1));
                }
            }

            @Nested
            @DisplayName("Ruby-Style Range Syntax")
            class RubyStyleRangeTests {

                @Test
                @DisplayName("Should evaluate inclusive range [1..3] expression")
                void testInclusiveRangeExpression() throws IOException {
                    String yaml = """
                            numbers: "${[1..3]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(1, 2, 3));
                }

                @Test
                @DisplayName("Should evaluate exclusive range [1...3] expression")
                void testExclusiveRangeExpression() throws IOException {
                    String yaml = """
                            numbers: "${[1...3]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(1, 2));
                }

                @Test
                @DisplayName("Should evaluate inclusive range with negative numbers [-3..-1]")
                void testInclusiveNegativeRangeExpression() throws IOException {
                    String yaml = """
                            numbers: "${[-3..-1]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(-3, -2, -1));
                }

                @Test
                @DisplayName("Should evaluate exclusive range with negative numbers [-3...-1]")
                void testExclusiveNegativeRangeExpression() throws IOException {
                    String yaml = """
                            numbers: "${[-3...-1]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(-3, -2));
                }

                @Test
                @DisplayName("Should evaluate range spanning negative to positive numbers [-2..2]")
                void testNegativeToPositiveRangeExpression() throws IOException {
                    String yaml = """
                            numbers: "${[-2..2]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(-2, -1, 0, 1, 2));
                }

                @Test
                @DisplayName("Should ignore range syntax inside string literals")
                void testRangeInsideStringLiteral() throws IOException {
                    String yaml = """
                            message: "The range is [1..3] today"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat(getNestedValue(result, "message"), is("The range is [1..3] today"));
                }

                @Test
                @DisplayName("Should evaluate inclusive range with variable bounds [from..to]")
                void testInclusiveVariableRangeExpression() throws IOException {
                    String yaml = """
                            variables:
                              from: 1
                              to: 3
                            numbers: "${[from..to]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(1, 2, 3));
                }

                @Test
                @DisplayName("Should evaluate exclusive range with variable bounds [from...to]")
                void testExclusiveVariableRangeExpression() throws IOException {
                    String yaml = """
                            variables:
                              start: 5
                              finish: 8
                            numbers: "${[start...finish]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(5, 6, 7));
                }

                @Test
                @DisplayName("Should evaluate inclusive range with arithmetic bounds [(start + 5)..(end + 5)]")
                void testInclusiveArithmeticRangeExpression() throws IOException {
                    String yaml = """
                            variables:
                              start: 10
                              end: 12
                            numbers: "${[(start + 5)..(end + 5)]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(15, 16, 17));
                }

                @Test
                @DisplayName("Should evaluate exclusive range with arithmetic bounds [start + 5...end + 5]")
                void testExclusiveArithmeticRangeExpression() throws IOException {
                    String yaml = """
                            variables:
                              start: 10
                              end: 13
                            numbers: "${[start + 5...end + 5]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(15, 16, 17));
                }

                @Test
                @DisplayName("Should evaluate range with map indexing bounds [bounds['start']..bounds['end']]")
                void testMapIndexedRangeExpression() throws IOException {
                    String yaml = """
                            variables:
                              bounds:
                                start: 1
                                end: 3
                            numbers: "${[bounds['start']..bounds['end']]}"
                            """;

                    Map<Object, @Nullable Object> result = loadYaml(yaml);

                    assertThat((Iterable<?>) getNestedValue(result, "numbers"), contains(1, 2, 3));
                }
            }
        }

        @Nested
        @DisplayName("Enumeration and Indexing")
        class EnumerationTests {

            @Test
            @DisplayName("Enumerates list elements using the enumerate filter")
            void enumeratesListWithFilter() throws IOException {
                String yaml = """
                        variables:
                          items: ["apple", "banana", "cherry"]
                        result: ${items | enumerate}
                        """;

                Map<Object, Object> data = loadYaml(yaml);
                assertThat(data.get("result"),
                        equalTo(List.of(List.of(0, "apple"), List.of(1, "banana"), List.of(2, "cherry"))));
            }

            @Test
            @DisplayName("Enumerates list elements using the enumerate function")
            void enumeratesListWithFunction() throws IOException {
                String yaml = """
                        variables:
                          items: ["apple", "banana", "cherry"]
                        result: ${enumerate(items)}
                        """;

                Map<Object, Object> data = loadYaml(yaml);
                assertThat(data.get("result"),
                        equalTo(List.of(List.of(0, "apple"), List.of(1, "banana"), List.of(2, "cherry"))));
            }

            @Test
            @DisplayName("Enumerates map entries using the enumerate filter")
            void enumeratesMapWithFilter() throws IOException {
                String yaml = """
                        variables:
                          mapping: { a: "apple", b: "banana" }
                        result: ${mapping | enumerate}
                        """;

                Map<Object, Object> data = loadYaml(yaml);
                List<?> resultList = (List<?>) data.get("result");
                assertThat(resultList, not(empty()));
            }

            @Test
            @DisplayName("Enumerates map entries using the enumerate function")
            void enumeratesMapWithFunction() throws IOException {
                String yaml = """
                        variables:
                          mapping: { a: "apple", b: "banana" }
                        result: ${enumerate(mapping)}
                        """;

                Map<Object, Object> data = loadYaml(yaml);
                List<?> resultList = (List<?>) data.get("result");
                assertThat(resultList, not(empty()));
            }
        }

        @Nested
        @DisplayName("Filters")
        class FilterTests {

            @Nested
            @DisplayName("Standard Filters")
            class StandardFilters {

                @Test
                @DisplayName("Applies default values to missing or empty variables")
                void appliesDefaultValues() throws IOException {
                    String yaml = """
                            variables:
                              exist: value1
                              empty_value: ""

                            exists: ${exist|default('fallback')}
                            missing: ${unknown|default('fallback')}
                            empty_loose: ${empty_value|default('fallback')}
                            empty_strict: ${empty_value|default('fallback', true)}
                            """;
                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(data.get("exists"), is("value1"));
                    assertThat(data.get("missing"), is("fallback"));
                    assertThat(data.get("empty_loose"), is(""));
                    assertThat(data.get("empty_strict"), is("fallback"));
                }
            }

            @Nested
            @DisplayName("Custom Filters")
            class CustomFilters {

                @Nested
                @DisplayName("Label")
                class Label {

                    @ParameterizedTest(name = "[{index}] Label conversion: \"{0}\" -> \"{1}\"")
                    @CsvSource(delimiter = '|', value = { //
                            "foo bar                       | Foo Bar", //
                            "fooBar                        | Foo Bar", //
                            "foo_bar                       | Foo Bar", //
                            "foo---bar_:-baz               | Foo Bar Baz", //
                            "LivingRoom_Light1Dimmer       | Living Room Light 1 Dimmer", //
                            "HTTPServer                    | HTTP Server", //
                            "Multiple   Spaces             | Multiple Spaces", //
                            "StatusLED                     | Status LED" //
                    })
                    void convertsStringsToHumanReadableLabels(String input, String expected) throws IOException {
                        String yaml = """
                                expression: ${'%s' | label}
                                """.formatted(input);
                        Map<Object, @Nullable Object> data = loadYaml(yaml);
                        assertThat(data.get("expression"), is(expected));
                    }
                }

                @Nested
                @DisplayName("Dig")
                class Dig {

                    @Test
                    @DisplayName("Resolves deep paths from a variable")
                    void resolvesDeepMapPath() throws IOException {
                        String yaml = """
                                variables:
                                  system:
                                    network:
                                      ip: "192.168.1.1"

                                # Accessing 2 levels deep from the 'system' root
                                result: ${system | dig('network', 'ip')}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("result"), is("192.168.1.1"));
                    }

                    @Test
                    @DisplayName("Returns null when the middle of the path is missing")
                    void returnsNullForMissingMiddlePath() throws IOException {
                        String yaml = """
                                variables:
                                  system:
                                    network: { dns: "8.8.8.8" }

                                # 'proxy' does not exist inside 'network'
                                missing: ${system | dig('network', 'proxy', 'host')}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("missing"), is(nullValue()));
                        assertThat(logSession.getTrackedWarnings(), empty());
                    }

                    @Test
                    @DisplayName("Accesses list elements inside a nested map")
                    void navigatesNestedLists() throws IOException {
                        String yaml = """
                                variables:
                                  hardware:
                                    usb_ports: ["port_a", "port_b"]

                                # Combining Map lookup and List index
                                result: ${hardware | dig('usb_ports', 1)}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("result"), is("port_b"));
                    }

                    @Test
                    @DisplayName("Navigates deep list-of-lists")
                    void navigatesDeepLists() throws IOException {
                        String yaml = """
                                variables:
                                  matrix:
                                    coords: [[1, 2], [3, 4]]

                                # Digging through two levels of arrays
                                val: ${matrix | dig('coords', 1, 0)}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("val"), is(3));
                    }

                    @Test
                    @DisplayName("Supports dot-notation single-argument paths")
                    void supportsDotNotationSingleArg() throws IOException {
                        String yaml = """
                                variables:
                                  system:
                                    network:
                                      ip: "192.168.1.1"

                                  matrix:
                                    coords: [[1, 2], [3, 4]]

                                # Using dot-notation in a single dig argument
                                result_ip: ${system | dig('network.ip')}
                                val: ${matrix | dig('coords.1.0')}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("result_ip"), is("192.168.1.1"));
                        assertThat(data.get("val"), is(3));
                    }

                    @Test
                    @DisplayName("Supports mixed dot-notation and separate args")
                    void supportsMixedDotAndSeparateArgs() throws IOException {
                        String yaml = """
                                variables:
                                  system:
                                    network:
                                      ip: "192.168.1.1"

                                  matrix:
                                    coords: [[1, 2], [3, 4]]

                                  root:
                                    nested:
                                      arr: [10, 20]

                                # Mixed: combine separate arg and dot-notation arg
                                res1: ${root | dig('nested', 'arr.1')}
                                # Mixed: dot-notation plus numeric arg
                                res2: ${matrix | dig('coords.1', 0)}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("res1"), is(20));
                        assertThat(data.get("res2"), is(3));
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        @ParameterizedTest
        @ValueSource(strings = { "${'}", "${\"}", "${'\"}", "${\"'}", "${${}}", "${${}" })
        @DisplayName("Correctly identifies substitution boundaries during syntax errors")
        void correctlyIdentifiesSubstitutionBoundariesInSyntaxErrors(String expression) throws IOException {
            String yaml = "expression: " + expression;
            Map<Object, @Nullable Object> data = loadYaml(yaml);
            Object expressionValue = data.get("expression");
            assertThat("The parser should pass the entire content inside the braces to the engine", expressionValue,
                    is(nullValue()));

            assertThat("Malformed expressions should still be handed to the engine and log a parse error",
                    logSession.getTrackedWarnings(), hasItem(containsString("Error parsing")));
        }

        @ParameterizedTest
        @ValueSource(strings = { "${undefined_variable}", "${2 + foo}" })
        @DisplayName("Warns when expressions contain unresolved variables or tokens")
        void warnsOnUnresolvedVariables(String expression) throws IOException {
            String yaml = "test: " + expression;
            loadYaml(yaml);

            assertThat("The engine should log a warning for undefined variables or tokens",
                    logSession.getTrackedWarnings(), hasItem(anyOf(containsString("Undefined variable"))));
        }

        @Test
        @DisplayName("Provides spelling suggestions for misspelled variables")
        void providesSuggestionsForMisspelledVariables() throws IOException {
            String yaml = """
                    variables:
                      correct_name: value

                    test: ${corret_name}
                    """;

            loadYaml(yaml);

            assertThat("The engine should suggest similarly named variables for misspellings",
                    logSession.getTrackedWarnings(), hasItem(containsString("Did you mean 'correct_name'?")));
        }
    }

    @Nested
    @DisplayName("Null and Undefined Handling")
    class NullHandling {
        @Test
        @DisplayName("Removes null elements from lists")
        void removesNullListElements() throws IOException {
            String yaml = "list: ${[ undefined_variable, null, 'normal string' ]}";
            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data.get("list"), equalTo(List.of("normal string")));
        }
    }

    @Nested
    @DisplayName("Custom Delimiters")
    class CustomDelimiters {
        @Test
        @DisplayName("Supports varying delimiter styles (brackets, parenthesis, at-symbols)")
        void supportsVaryingDelimiterStyles() throws IOException {
            String yaml = """
                    variables:
                      foo: bar
                      bracket_pattern: "$[[..]]"
                      parenthesis_pattern: "$((..))"
                      at_symbol_pattern: "@[..]"
                    bracket: !sub:bracket_pattern "$[[foo]]"
                    parenthesis: !sub:parenthesis_pattern "$((foo))"
                    at_symbol: !sub:at_symbol_pattern "@[foo]"
                    """;

            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data.get("bracket"), equalTo("bar"));
            assertThat(data.get("parenthesis"), equalTo("bar"));
            assertThat(data.get("at_symbol"), equalTo("bar"));
        }

        @Test
        @DisplayName("Supports multiple occurrences of custom delimiters in one string")
        void supportsMultipleOccurrences() throws IOException {
            String yaml = """
                    variables:
                      foo: bar
                      bracket_pattern: "$[[..]]"
                    multiple: !sub:bracket_pattern "$[[foo]]_$[[foo]]"
                    """;

            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data.get("multiple"), equalTo("bar_bar"));
        }

        @Test
        @DisplayName("Handles empty custom patterns gracefully")
        void supportsEmptyPattern() throws IOException {
            String yaml = """
                    variables:
                      bracket_pattern: "$[[..]]"
                    empty: !sub:bracket_pattern 'A$[[]]B'
                    """;
            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data.get("empty"), equalTo("AB"));
        }

        @Test
        @DisplayName("Maintains delimiter scope through nested YAML structures")
        void maintainsDelimiterScopeThroughNestedStructures() throws IOException {
            String yaml = """
                    variables:
                      foo: bar
                      bracket_pattern: "$[[..]]"
                      at_symbol_pattern: "@[..]"
                    level1: !sub:bracket_pattern
                      data: "$[[foo]]"
                      level2:
                        data: "$[[foo]]"
                      level2_override: !sub:at_symbol_pattern
                        data: "@[foo]"
                      data2: "$[[foo]]"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "level1", "data"), equalTo("bar"));
            assertThat(getNestedValue(data, "level1", "level2", "data"), equalTo("bar"));
            assertThat(getNestedValue(data, "level1", "level2_override", "data"), equalTo("bar"));
            assertThat(getNestedValue(data, "level1", "data2"), equalTo("bar"));
        }
    }

    @Nested
    @DisplayName("Substitution Control")
    class SubstitutionControl {
        @Test
        @DisplayName("!literal tag prevents interpolation of variable patterns")
        void literalTagPreventsInterpolation() throws IOException {
            String yaml = """
                    variables: { foo: bar }
                    top:
                      enabled: ${foo}
                      disabled_branch: !literal
                        level2: ${foo}
                    """;
            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "top", "enabled"), is("bar"));
            assertThat(getNestedValue(data, "top", "disabled_branch", "level2"), is("${foo}"));
        }

        @Test
        @DisplayName("!sub tag re-enables substitutions inside a parent !literal boundary")
        void subTagCanOverrideParentLiteralTag() throws IOException {
            String yaml = """
                    variables: { foo: bar }
                    top: !literal
                      disabled: ${foo}
                      re_enabled: !sub ${foo}
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "top", "disabled"), is("${foo}"));
            assertThat(getNestedValue(data, "top", "re_enabled"), is("bar"));
        }
    }

    @Nested
    @DisplayName("Substitutions inside other tags")
    class SubstitutionsInsideOtherTags {

        @Test
        @DisplayName("Applies substitutions to scalar form !include paths")
        void appliesSubstitutionsToScalarFormInclude() throws IOException {
            writeFixture("target.yaml", "key: value");

            String yaml = """
                    variables: { my_path: 'target.yaml' }
                    test:
                      result: !include "${my_path}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            assertThat(getNestedValue(data, "test", "result", "key"), is("value"));
        }

        @Test
        @DisplayName("Applies substitutions to block form !include (file and vars)")
        void appliesSubstitutionsToBlockFormInclude() throws IOException {
            writeFixture("target.inc.yaml", "result: 'processed-${inner}'");

            String yaml = """
                    variables:
                      path_var: 'target.inc.yaml'
                      val_var: 'from-parent'

                    test:
                      result:
                        !include
                          file: "${path_var}"
                          vars:
                            inner: "${val_var}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "test", "result", "result"), is("processed-from-parent"));
        }

        @Test
        @DisplayName("Applies substitutions to scalar form !insert template names")
        void appliesSubstitutionsToScalarFormInsert() throws IOException {
            String yaml = """
                    variables: { tpl_name: 'my_template' }
                    templates:
                      my_template: "Success"

                    test:
                      result: !insert "${tpl_name}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            assertThat(getNestedValue(data, "test", "result"), is("Success"));
        }

        @Test
        @DisplayName("Applies substitutions to block form !insert (template and vars)")
        void appliesSubstitutionsToBlockFormInsert() throws IOException {
            String yaml = """
                    variables:
                      tpl_var: 'my_template'
                      suffix_var: 'engine'

                    templates:
                      my_template: "power-${suffix}"

                    test:
                      result:
                        !insert
                          template: "${tpl_var}"
                          vars:
                            suffix: "${suffix_var}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "test", "result"), is("power-engine"));
        }

        @Test
        @DisplayName("Substitutions penetrate through !replace tag boundaries")
        void appliesSubstitutionsWithinReplaceBlocks() throws IOException {
            String yaml = """
                    variables:
                      moo: cow
                    packages:
                      things:
                        MyThing:
                          foo: { bar: baz }
                    things:
                      MyThing:
                        foo: !replace
                          qux: "${moo}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "things", "MyThing", "foo", "qux"), equalTo("cow"));
        }
    }
}
