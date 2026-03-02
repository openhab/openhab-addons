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
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.io.yamlcomposer.internal.YamlComposer.CacheEntry;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;
import org.snakeyaml.engine.v2.api.Load;

/**
 * The {@link YamlComposerTest} contains tests for the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class YamlComposerTest {
    private static final Path SOURCE_PATH = Path.of("src/test/resources/model/composer");
    private @Nullable TestInfo currentTest = null; // so loadFixture can tell which test is calling it

    private @TempDir @Nullable Path sharedTempDir = null; // Initialized to avoid null warning

    private final Load loader = ComposerUtils.createYamlLoader("dummy.yaml");

    // logSession is used by loadFixture to capture logs during YAML loading,
    // which can be helpful for debugging test failures
    private final LogSession logSession = new LogSession();

    @BeforeEach
    void setup(TestInfo testInfo) {
        this.currentTest = testInfo;
    }

    @AfterEach
    void tearDown() {
        logSession.close(); // This will flush logs to the console automatically
    }

    @Nested
    class TestInfrastructure {
        @Test
        @DisplayName("Extracts values from deeply nested maps")
        void extractsValuesFromDeeplyNestedMaps() {
            Map<Object, @Nullable Object> data = Map.of("top", Map.of("level1", Map.of("level2", "value")));

            assertThat(getNestedValue(data, "top", "level1", "level2"), equalTo("value"));
        }

        @Test
        @DisplayName("Returns null when nested keys are missing")
        void returnsNullWhenNestedKeysAreMissing() {
            Map<Object, @Nullable Object> data = Map.of("top", Map.of("level1", "value"));

            assertNull(getNestedValue(data, "top", "nonexistent"));
            assertNull(getNestedValue(data, "missing", "level1"));
        }
    }

    @Nested
    class YamlParsing {
        @Test
        @DisplayName("Allows null values")
        void allowsNullValues() throws IOException {
            assertThat(loader.loadFromString(""), is(nullValue()));
            assertThat(loader.loadFromString("# Comment"), is(nullValue()));
            assertThat(loader.loadFromString("null"), is(nullValue()));
            assertThat(loader.loadFromString("a: null"), equalTo(Collections.singletonMap("a", null)));
            assertThat(loader.loadFromString("null: null"), equalTo(Collections.singletonMap(null, null)));
            assertThat(loader.loadFromString("- null"), equalTo(Collections.singletonList(null)));
        }

        @Test
        @DisplayName("Parses true and false as boolean")
        void parsesTrueAndFalseAsBoolean() throws IOException {
            assertThat(loader.loadFromString("true"), equalTo(true));
            assertThat(loader.loadFromString("True"), equalTo(true));
            assertThat(loader.loadFromString("TRUE"), equalTo(true));

            assertThat(loader.loadFromString("false"), equalTo(false));
            assertThat(loader.loadFromString("False"), equalTo(false));
            assertThat(loader.loadFromString("FALSE"), equalTo(false));
        }

        @Test
        @DisplayName("Treats boolean-like strings as regular strings")
        void treatsBooleanLikeStringsAsRegularStrings() throws IOException {
            List.of("on", "On", "ON", "oN").forEach(value -> {
                assertThat(loader.loadFromString(value), equalTo(value));
            });

            List.of("off", "Off", "OFF", "oFf").forEach(value -> {
                assertThat(loader.loadFromString(value), equalTo(value));
            });

            List.of("yes", "Yes", "YES", "yEs").forEach(value -> {
                assertThat(loader.loadFromString(value), equalTo(value));
            });

            List.of("no", "No", "NO", "nO").forEach(value -> {
                assertThat(loader.loadFromString(value), equalTo(value));
            });
        }

        @Test
        @DisplayName("Supports anchors and aliases")
        void supportsAnchorsAndAliases() throws IOException {
            @SuppressWarnings("unchecked")
            Map<Object, Object> data = (Map<Object, Object>) loader.loadFromString("""
                    foo: &name bar
                    baz: *name
                    ? *name
                    : qux
                    """);
            assertThat(data.get("baz"), equalTo("bar"));
            assertThat(data.get("bar"), equalTo("qux"));
        }

        @Test
        @DisplayName("Handles self-referencing container")
        @SuppressWarnings("null")
        void handlesSelfReferencingContainer() throws IOException {
            String yaml = """
                    baz: &id001
                      me: *id001
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(data.get("baz"), instanceOf(Map.class));
            @SuppressWarnings("unchecked")
            Map<Object, Object> baz = (Map<Object, Object>) data.get("baz");

            // The 'me' entry should reference the same Map instance (self-reference)
            assertSame(baz, baz.get("me"));
        }
    }

    @Nested
    class ModelCleanup {
        @Test
        @DisplayName("Removes preprocessing metadata")
        void removesPreprocessingMetadata() throws IOException {
            String yaml = """
                    composer:
                      generate_resolved_file: false

                    variables:
                      foo: bar

                    templates:
                      sample_template: foo

                    packages:
                      foo: {}
                    """;

            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data, not(hasKey("composers")));
            assertThat(data, not(hasKey("variables")));
            assertThat(data, not(hasKey("templates")));
            assertThat(data, not(hasKey("packages")));
        }

        @Test
        @DisplayName("Removes hidden keys")
        @SuppressWarnings("null")
        void removesHiddenKeys() throws IOException {
            String yaml = ".energy_type: foo";
            Map<Object, @Nullable Object> data = loadYaml(yaml);
            List<String> keys = data.keySet().stream().map(Object::toString).collect(Collectors.toList());
            assertThat(keys, everyItem(not(startsWith("."))));
        }

        @Test
        @DisplayName("Retains other keys")
        void retainsOtherKeys() throws IOException {
            String yaml = """
                    version: 1
                    items: a
                    things: b
                    other: c
                    """;
            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data.get("version"), equalTo(1));
            assertThat(data.get("items"), equalTo("a"));
            assertThat(data.get("things"), equalTo("b"));
            assertThat(data.get("other"), equalTo("c"));
        }

        @Test
        @DisplayName("Removes null keys in maps")
        void removesNullKeysInMaps() throws IOException {
            String yaml = "map: { null: value, key1: val1 }";
            Map<Object, Object> data = loadYaml(yaml);
            assertThat(data.get("map"), equalTo(Map.of("key1", "val1")));
        }
    }

    @Nested
    @DisplayName("Variables and Substitutions")
    class VariablesAndSubstitutions {

        @Nested
        @DisplayName("Resolution and Scoping")
        class ResolutionAndScoping {

            @Test
            @DisplayName("Resolves plain variables in simple !sub strings")
            void resolvesPlainVariables() throws IOException {
                String yaml = """
                        variables:
                          greeting: "Hello"
                          target: "World"
                        test: !sub "${greeting}, ${target}!"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Basic variable resolution should work in a simple !sub string",
                        getNestedValue(data, "test"), is("Hello, World!"));
            }

            @Test
            @DisplayName("Supports defining variables at end of file")
            void supportsVariablesDefinedAtEndOfFile() throws IOException {
                String yaml = """
                        test:
                          result: !sub "${late_var}"

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
                        file: !sub "${__FILE__}"
                        filename: !sub "${__FILE_NAME__}"
                        ext: !sub "${__FILE_EXT__}"
                        path: !sub "${__DIRECTORY__}"
                        openhab_conf: !sub "${OPENHAB_CONF}"
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
                        filename: !sub "${__FILE_NAME__}"
                        """);

                Path mainFile = writeFixture("predefinedVarsOverride.yaml", """
                        variables:
                          __FILE_NAME__: "main_override"

                        filename: !sub "${__FILE_NAME__}"

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

            @Test
            @DisplayName("Loads entire variables block from an !include file")
            void loadsEntireVariablesBlockFromInclude() throws IOException {
                writeFixture("vars_file.inc.yaml", """
                        external_var: "from_file"
                        another_var: "hello"
                        """);

                Path mainFile = writeFixture("main.yaml", """
                        variables: !include vars_file.inc.yaml

                        test:
                          result: !sub "${external_var} ${another_var}"
                        """);

                Map<Object, @Nullable Object> data = loadFixture(mainFile);

                assertThat("Variables from the included file should be available in the global context",
                        getNestedValue(data, "test", "result"), is("from_file hello"));
            }

            @Test
            @DisplayName("Loads variables from include files within a variables map")
            void loadsVariablesFromIncludeFiles() throws IOException {
                writeFixture("variableFromInclude.inc.yaml", "qux");

                Path mainFile = writeFixture("variableFromInclude.yaml", """
                        variables: !sub
                          foo: !include variableFromInclude.inc.yaml

                        included_value: !sub ${foo}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(mainFile);

                assertThat("Simple inclusion should work", getNestedValue(data, "included_value"), is("qux"));
            }

            @Test
            @DisplayName("Loads variables within a variables map from include files with vars")
            void loadsVariablesFromIncludeFilesWithVars() throws IOException {
                // This also tests that a simple scalar inside an include file
                // will resolve substitutions using the inherited context.
                writeFixture("include.inc.yaml", "!sub ${var}");

                Path mainFile = writeFixture("main.yaml", """
                        variables: !sub
                          foo: !include
                            file: include.inc.yaml
                            vars:
                              var: qux

                        included_value: !sub ${foo}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(mainFile);

                assertThat("Simple inclusion should work", getNestedValue(data, "included_value"), is("qux"));
            }

            @Test
            @DisplayName("Supports substitution within variables block (Recursive resolution)")
            void supportsSubstitutionWithinVariablesBlock() throws IOException {
                String yaml = """
                        variables:
                          a: "root"
                          b: !sub "${a}-to-middle"
                          c: !sub "${b}-to-leaf"

                        test:
                          result: !sub "${c}"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Variables must support self-referential resolution", getNestedValue(data, "test", "result"),
                        is("root-to-middle-to-leaf"));
            }

            @Test
            @DisplayName("Supports !sub tag on the variables node itself")
            void supportsSubTagOnVariablesNode() throws IOException {
                String yaml = """
                        variables: !sub
                          foo: "bar"
                          baz: "${foo}"

                        test:
                          result: !sub "${baz}"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("The variables node itself should support the !sub tag",
                        getNestedValue(data, "test", "result"), is("bar"));
            }
        }

        @Nested
        @DisplayName("Substitution Syntax")
        class SubstitutionSyntax {

            private static final String SYNTAX_VARS = """
                    variables:
                      exist: value1
                      one: 1
                      "varname-with-dash": dashvalue
                      "varname with space": spacevalue
                    """;

            @Test
            @DisplayName("Handles quoting and escaping in expressions")
            void handlesQuotingAndEscaping() throws IOException {
                String yaml = SYNTAX_VARS + """
                        plain: !sub ${exist}
                        double_quoted: !sub "${exist}"
                        single_quoted: !sub '${exist}'
                        braces_in_double_quotes: !sub "${'${}'}"
                        braces_in_single_quotes: !sub '${"${}"}'
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
                String yaml = SYNTAX_VARS + """
                        empty: !sub ${}
                        null_value: !sub ${null}
                        null_string: !sub "${null}"
                        padded: !sub ${   exist    }
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
                        empty_map: !sub ${ {} }
                        compare_empty_maps: !sub ${ {} == {} }
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(data.get("empty_map"), instanceOf(Map.class));
                assertThat((Map<?, ?>) data.get("empty_map"), is(anEmptyMap()));
                assertThat(data.get("compare_empty_maps"), equalTo(true));
            }

            @Test
            @DisplayName("Handles special characters in variable names via VARS lookup")
            void handlesSpecialVariableNames() throws IOException {
                String yaml = SYNTAX_VARS + """
                        vars_with_dash: !sub ${VARS["varname-with-dash"]}
                        vars_with_space: !sub ${VARS['varname with space']}
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(data.get("vars_with_dash"), is("dashvalue"));
                assertThat(data.get("vars_with_space"), is("spacevalue"));
            }

            @Test
            @DisplayName("Handles basic data types and mathematical expressions")
            void handlesDataTypesAndMath() throws IOException {
                String yaml = SYNTAX_VARS + """
                        int_const: !sub ${1 + 1}
                        int_var_math: !sub ${one * 5}
                        int_quoted: !sub "${100}"
                        string: !sub ${'100'}
                        string_quoted: !sub "${'100'}"
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
                        map_lookup: !sub ${mapping.foo}
                        list_lookup: !sub ${list[0]}
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(data.get("map_lookup"), is("bar"));
                assertThat(data.get("list_lookup"), is("item0"));
            }

            @Test
            @DisplayName("Allows execution of Java String methods within expressions")
            void executesJavaObjectMethod() throws IOException {
                String yaml = """
                        repeated: !sub ${'bar'.repeat(2) + 'ian'}
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(data.get("repeated"), is("barbarian"));
            }
        }

        @Nested
        @DisplayName("Expression Filters")
        class ExpressionFilters {

            @Nested
            @DisplayName("Standard Filters")
            class StandardFilters {

                private static final String FILTER_VARS = """
                        variables:
                          exist: value1
                          empty_value: ""
                          one: 1
                        """;

                @Test
                @DisplayName("Applies default values to missing or empty variables")
                void appliesDefaultValues() throws IOException {
                    String yaml = FILTER_VARS + """
                            exists: !sub ${exist|default('fallback')}
                            missing: !sub ${unknown|default('fallback')}
                            empty_loose: !sub ${empty_value|default('fallback')}
                            empty_strict: !sub ${empty_value|default('fallback', true)}
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
                            "StatusLED                     | Status LED" //
                    })
                    void convertsStringsToHumanReadableLabels(String input, String expected) throws IOException {
                        String yaml = """
                                expression: !sub ${'%s' | label}
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
                                result: !sub ${system | dig('network', 'ip')}
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
                                missing: !sub ${system | dig('network', 'proxy', 'host')}
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
                                result: !sub ${hardware | dig('usb_ports', 1)}
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
                                val: !sub ${matrix | dig('coords', 1, 0)}
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
                                result_ip: !sub ${system | dig('network.ip')}
                                val: !sub ${matrix | dig('coords.1.0')}
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
                                res1: !sub ${root | dig('nested', 'arr.1')}
                                # Mixed: dot-notation plus numeric arg
                                res2: !sub ${matrix | dig('coords.1', 0)}
                                """;

                        Map<Object, Object> data = loadYaml(yaml);
                        assertThat(data.get("res1"), is(20));
                        assertThat(data.get("res2"), is(3));
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
                String yaml = "expression: !sub " + expression;
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
                String yaml = "test: !sub " + expression;
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

                        test: !sub "${corret_name}"
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
                String yaml = "list: !sub ${[ undefined_variable, 'normal string' ]}";
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
            @DisplayName("Ignores patterns in untagged nodes")
            void ignoresPatternsInUntaggedNodes() throws IOException {
                String yaml = """
                        variables: { foo: bar }
                        standard: "${foo}"
                        """;
                Map<Object, Object> data = loadYaml(yaml);
                assertThat(data.get("standard"), equalTo("${foo}"));
            }

            @Test
            @DisplayName("Ignores patterns in untagged variables")
            void ignoresPatternsInUntaggedVariables() throws IOException {
                String yaml = """
                        variables:
                          first: hello
                          second: ${first} world
                        literal_pattern: !sub ${second}
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);
                assertThat(getNestedValue(data, "literal_pattern"), equalTo("${first} world"));
            }

            @Test
            @DisplayName("!nosub tag prevents interpolation of variable patterns")
            void nosubTagPreventsInterpolation() throws IOException {
                String yaml = """
                        variables: { foo: bar }
                        top: !sub
                          enabled: ${foo}
                          disabled_branch: !nosub
                            level2: ${foo}
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "top", "enabled"), is("bar"));
                assertThat(getNestedValue(data, "top", "disabled_branch", "level2"), is("${foo}"));
            }

            @Test
            @DisplayName("!sub tag can 'punch a hole' through a parent !nosub boundary")
            void subTagCanOverrideParentNosubTag() throws IOException {
                String yaml = """
                        variables: { foo: bar }
                        top: !nosub
                          disabled: ${foo}
                          re_enabled: !sub ${foo}
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "top", "disabled"), is("${foo}"));
                assertThat(getNestedValue(data, "top", "re_enabled"), is("bar"));
            }
        }

        @Nested
        @DisplayName("Substitution Isolation")
        class SubstitutionIsolation {

            @Nested
            @DisplayName("Opaque Boundaries (Context Isolation)")
            class OpaqueBoundaries {
                @Test
                @DisplayName("Maintains isolation across !include files")
                void inclusionAndMergeMaintainTagIsolation() throws IOException {
                    writeFixture("included.yaml", "plain: ${foo}");

                    String yaml = """
                            variables: { foo: bar }
                            sub_leak_test: !sub
                              data: !include 'included.yaml'
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);
                    assertThat(getNestedValue(data, "sub_leak_test", "data", "plain"), equalTo("${foo}"));
                }

                @Test
                @DisplayName("Maintains isolation across Anchors and Aliases")
                void anchorAndAliasMaintainTagIsolation() throws IOException {
                    String yaml = """
                            variables: { foo: bar }
                            definitions: { plain: &PLAIN "${foo}" }
                            sub_leak_test: !sub
                              val: *PLAIN
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);
                    assertThat(getNestedValue(data, "sub_leak_test", "val"), equalTo("${foo}"));
                }

                @Test
                @DisplayName("Maintains isolation across !insert templates")
                void insertionMaintainTagIsolation() throws IOException {
                    String yaml = """
                            variables: { foo: bar }
                            templates: { plain_tpl: "${foo}" }
                            sub_leak_test: !sub
                              val: !insert plain_tpl
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);
                    assertThat(getNestedValue(data, "sub_leak_test", "val"), equalTo("${foo}"));
                }
            }

            @Nested
            @DisplayName("Transparent Passthroughs (Context Flow)")
            class TransparentPassthroughs {
                @Test
                @DisplayName("Applies substitutions to scalar form !include paths")
                void appliesSubstitutionsToScalarFormInclude() throws IOException {
                    writeFixture("target.yaml", "key: value");

                    String yaml = """
                            variables: { my_path: 'target.yaml' }
                            test: !sub
                              result: !include "${my_path}"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);
                    assertThat(getNestedValue(data, "test", "result", "key"), is("value"));
                }

                @Test
                @DisplayName("Applies substitutions to block form !include (file and vars)")
                void appliesSubstitutionsToBlockFormInclude() throws IOException {
                    writeFixture("target.inc.yaml", "result: !sub 'processed-${inner}'");

                    String yaml = """
                            variables:
                              path_var: 'target.inc.yaml'
                              val_var: 'from-parent'

                            test: !sub
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

                            test: !sub
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
                              my_template: !sub "power-${suffix}"

                            test: !sub
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
                              MyThing: !sub
                                foo: !replace
                                  qux: "${moo}"
                            """;

                    Map<Object, @Nullable Object> data = loadYaml(yaml);

                    assertThat(getNestedValue(data, "things", "MyThing", "foo", "qux"), equalTo("cow"));
                }
            }
        }
    }

    @Nested
    @DisplayName("!if Tag")
    class IfTag {

        @Nested
        @DisplayName("Syntax Forms")
        class SyntaxForms {

            @Test
            @DisplayName("Resolves simple mapping form")
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

            @Test
            @DisplayName("Resolves sequence form with multiple branches")
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

        @Nested
        @DisplayName("Expression Evaluation")
        class ExpressionEvaluation {

            @Test
            @DisplayName("Evaluates expressions in conditions")
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
            @DisplayName("Evaluates !sub expressions in conditions")
            void evaluatesSubExpressionsInConditions() throws IOException {
                String yaml = """
                        variables:
                          threshold: 10
                        test: !if
                          if: !sub ${threshold} > 5
                          then: "high"
                          else: "low"
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);
                assertThat("Should evaluate !sub expression in condition", getNestedValue(data, "test"), is("high"));
            }

            @Test
            @DisplayName("Evaluates mixed !sub and plain expressions in conditions")
            void evaluatesMixedExpressionsInConditions() throws IOException {
                String yaml = """
                        variables:
                          value: 7
                        test: !if
                          if: !sub ${value} > 5
                          then: "greater"
                          else: "lesser"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                assertThat("Should evaluate mixed expression in condition", getNestedValue(data, "test"),
                        is("greater"));
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
                ;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "value"), is("yes"));
            }
        }

        @Nested
        @DisplayName("Nested Tag Integration")
        class NestedIntegration {

            @Test
            @DisplayName("Resolves nested !sub and !insert tags within value fields")
            void resolvesNestedTags() throws IOException {
                String yaml = """
                        variables:
                          name: "World"
                        templates:
                          snippet: "Inserted Content"
                        test: !if
                          if: true
                          then: !sub "Hello, ${name}!"
                        other: !if
                          if: true
                          then: !insert snippet
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("Nested !sub inside !if should resolve", getNestedValue(data, "test"), is("Hello, World!"));
                assertThat("Nested !insert inside !if should resolve", getNestedValue(data, "other"),
                        is("Inserted Content"));
            }

            @Test
            @DisplayName("Should not resolve tags in inactive branches (Short-circuiting)")
            void shortCircuitsInactiveBranches() throws IOException {
                // We use a file that doesn't exist to prove the !include never runs
                String yaml = """
                        test: !if
                          if: true
                          then: "active"
                          else: !include non_existent_file.yaml
                        """;

                // If this throws an exception, the short-circuiting failed!
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat("The active branch should be the one resolved", data.get("test"), is("active"));
                assertThat("The inactive branch should not trigger any warnings", logSession.getTrackedWarnings(),
                        not(hasItem(containsString("Failed to process !include"))));
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

                assertThat("Recursive resolution should handle nested !if tags", getNestedValue(data, "test"),
                        is("deep"));
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
        }
    }

    @Nested
    @DisplayName("Include Tag Specification")
    class IncludeTests {

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
                        key: !sub ${arg1}
                        bool: !sub ${bool}
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
                Path main = writeFixture("main.yaml", "data: !include missing.yaml");
                loadFixture(main);

                assertThat(logSession.getTrackedWarnings(), hasItem(allOf(containsString("Failed to process !include"),
                        containsString("missing.yaml"), containsString("No such file"))));
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
                Path main = writeFixture("circular.yaml", "data: !include circular.yaml");
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
                writeFixture("included.inc.yaml", "result: !sub ${parent_var}");

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
                writeFixture("leaf.inc.yaml", "leaf_key: !sub ${top_var}");

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
                        result: !sub ${target}
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
                writeFixture("included.inc.yaml", "result: !sub ${target}");

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

                        var1: !sub ${VARS.var1}
                        var2: !sub ${VARS.var2}
                        var3: !sub ${VARS.local}
                        """);
                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "data", "var1"), equalTo("value1"));
                assertThat(getNestedValue(data, "data", "var2"), equalTo("value2"));
                assertThat(getNestedValue(data, "data", "var3"), equalTo("local_value"));
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

    @Nested
    @DisplayName("Insert Tag Specification")
    class InsertTests {

        @Nested
        @DisplayName("Template Resolution")
        class Resolution {

            @Test
            @DisplayName("Inserts content defined in the top-level templates node")
            void templateLookupWorks() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          simple:
                            scalar: "bar"

                        target: !insert
                          template: simple
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "target", "scalar"), equalTo("bar"));
            }

            @Test
            @DisplayName("Templates node can be dynamically generated via !sub")
            void dynamicTemplatesNodeSupported() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          tplname: "my_template"
                          value: "foo"

                        templates: !sub
                          ${tplname}: ${value}

                        data: !insert my_template
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "data"), equalTo("foo"));
            }

            @Test
            @DisplayName("Warns when the requested template key is missing")
            void missingTemplateWarns() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          exists: "foo"

                        target: !insert
                          template: does_not_exist
                        """);

                loadFixture(main);

                assertThat(logSession.getTrackedWarnings(), hasItem(containsString("template not found")));
            }
        }

        @Nested
        @DisplayName("Syntax & Argument Validation")
        class SyntaxValidation {

            @ParameterizedTest(name = "Input [{0}] should warn about missing template parameter")
            @ValueSource(strings = { "!insert", "!insert ''", "!insert {}", "!insert { template: null }" })
            void warnsOnMalformedInclude(String input) throws IOException {
                String yaml = """
                        templates:
                          valid: "foo"

                        target: %s
                        """.formatted(input);
                loadYaml(yaml);

                assertThat(logSession.getTrackedWarnings(), hasItem(
                        allOf(containsString("Failed to process !insert"), containsString("missing template name"))));
            }

            @Test
            @DisplayName("Supports simple scalar syntax (!insert template_name)")
            void supportsSimpleScalarArgument() throws IOException {
                String yaml = """
                        templates:
                          simple:
                            key: value
                        toplevel: !insert simple
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "toplevel", "key"), equalTo("value"));
            }

            @Test
            @DisplayName("Supports URL-style scalar syntax (!insert template_name?arg=value&bool)")
            void supportsUrlStyleScalarArgument() throws IOException {
                String yaml = """
                        templates:
                          simple:
                            key: !sub ${arg1}
                            bool: !sub ${bool}
                        toplevel: !insert simple?arg1=value&bool
                        """;
                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "toplevel", "key"), equalTo("value"));
                assertThat(getNestedValue(data, "toplevel", "bool"), is(true));
            }

            @Test
            @DisplayName("Supports map syntax with 'template' key (!insert { template: ... })")
            void supportsMapArgument() throws IOException {
                String yaml = """
                        templates:
                          included:
                            key: value
                        toplevel: !insert
                          template: included
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "toplevel", "key"), equalTo("value"));
            }
        }

        @Nested
        @DisplayName("Scoping & Visibility")
        class Scoping {

            @Test
            @DisplayName("Templates resolve global variables by default")
            void templateResolvesGlobalVariables() throws IOException {
                String yaml = """
                        variables:
                          global_val: "from_global"

                        templates:
                          tpl: !sub ${global_val}

                        target: !insert
                          template: tpl
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "target"), equalTo("from_global"));
            }

            @Test
            @DisplayName("Local 'vars' in !insert override global variables")
            void insertVarsOverrideMainVars() throws IOException {
                String yaml = """
                        variables:
                          foo: "global_bar"

                        templates:
                          vartpl: !sub ${foo}

                        target: !insert
                          template: vartpl
                          vars:
                            foo: "overridden"
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertThat(getNestedValue(data, "target"), equalTo("overridden"));
            }

            @Test
            @DisplayName("Insert works within an include file using that file's local templates")
            void insertWithinIncludeWorks() throws IOException {
                Path main = writeFixture("main.yaml", "content: !include child.yaml");

                writeFixture("child.yaml", """
                        templates:
                          local_tpl: "local_value"

                        data: !insert
                          template: local_tpl
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "content", "data"), equalTo("local_value"));
            }

            @Test
            @DisplayName("Templates are file-local and not shared across boundaries")
            void templatesAreFileLocal() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          parent_tpl: "secret"
                        content: !include child.yaml
                        """);

                writeFixture("child.yaml", """
                        data: !insert
                          template: parent_tpl
                        """);

                loadFixture(main);

                assertThat(logSession.getTrackedWarnings(), hasItem(containsString("template not found")));
            }
        }
    }

    @Nested
    @DisplayName("Packaging Specification")
    class PackagingTests {

        @Nested
        @DisplayName("Integration Styles")
        class IntegrationStyles {

            @Test
            @DisplayName("Packages can be defined using external include files")
            void packageInclusionWorks() throws IOException {
                // The Blueprint
                writeFixture("package.inc.yaml", """
                        things:
                          !sub ${name}:
                            label: !sub ${thing_label}
                        items:
                          !sub ${name}:
                            label: !sub ${item_label}
                        """);

                // The Main Composition
                Path main = writeFixture("main.yaml", """
                        packages:
                          basic1: !include
                            file: package.inc.yaml
                            vars:
                              name: basic1
                              thing_label: "B1 Thing"
                              item_label: "B1 Item"
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "things", "basic1", "label"), equalTo("B1 Thing"));
                assertThat(getNestedValue(data, "items", "basic1", "label"), equalTo("B1 Item"));
            }

            @Test
            @DisplayName("Packages can be defined using local templates")
            void packageTemplateWorks() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          pkg_tpl:
                            things:
                              !sub ${name}:
                                label: "Template Label"

                        packages:
                          basic1: !insert
                            template: pkg_tpl
                            vars:
                              name: "from_tpl"
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "things", "from_tpl", "label"), equalTo("Template Label"));
            }

            @Test
            @DisplayName("Non-package content remains after merge")
            void nonPackageContentRemainsAfterMerge() throws IOException {
                Path main = writeFixture("main.yaml", """
                        packages:
                          pkg1: !include { file: package.inc.yaml, vars: { name: "p1" } }
                        things:
                          static_thing:
                            label: "keep_me"
                        """);
                writeFixture("package.inc.yaml", "things: { !sub '${name}': { label: 'pkg' } }");

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "things", "static_thing", "label"), equalTo("keep_me"));
                assertThat(getNestedValue(data, "things", "p1", "label"), equalTo("pkg"));
            }

            @Test
            @DisplayName("Supports !sub attached directly to packages")
            void subOnPackagesWorks() throws IOException {
                writeFixture("package.inc.yaml", "foo: !sub ${package_id}");

                Path main = writeFixture("main.yaml", """
                        packages: !sub
                          pkgname: !include package.inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "foo"), equalTo("pkgname"));
            }
        }

        @Nested
        @DisplayName("Package ID Injection")
        class PackageID {

            @Test
            @DisplayName("Injects package ID into mapping-form include")
            void packageIdInjectedIntoMappingInclude() throws IOException {
                writeFixture("package.yaml", """
                        result:
                          !sub '${package_id}': 'active'
                        """);
                Path main = writeFixture("main.yaml", """
                        packages:
                          test_id: !include
                            file: package.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
            }

            @Test
            @DisplayName("Injects package ID into scalar-form include")
            void packageIdInjectedIntoScalarInclude() throws IOException {
                writeFixture("package.yaml", """
                        result:
                          !sub '${package_id}': 'active'
                        """);
                Path main = writeFixture("main.yaml", """
                        packages:
                          test_id: !include package.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
            }

            @Test
            @DisplayName("Injects package ID into mapping-form insert using named template")
            void packageIdInjectedIntoMappingInsert() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          test_template:
                            result:
                              !sub '${package_id}': 'active'

                        packages:
                          test_id: !insert
                            template: test_template
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
            }

            @Test
            @DisplayName("Injects package ID into scalar-form insert using named template")
            void packageIdInjectedIntoScalarInsert() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          test_template:
                            result:
                              !sub '${package_id}': 'active'

                        packages:
                          test_id: !insert test_template
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "result", "test_id"), equalTo("active"));
            }

            @Test
            @DisplayName("Manual package ID variable takes precedence over automatic injection")
            void packageIdIsOverridable() throws IOException {
                writeFixture("package.yaml", "value: !sub ${package_id}");

                Path main = writeFixture("main.yaml", """
                        packages:
                          default_id: !include
                            file: package.yaml
                            vars:
                              package_id: "custom_id"
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "value"), equalTo("custom_id"));
            }
        }

        @Nested
        @DisplayName("Merge Strategies")
        class MergeStrategies {

            @Test
            @SuppressWarnings("unchecked")
            @DisplayName("Deeply merges maps: overwrites shared scalars, appends lists, and preserves unique keys from both")
            void defaultDeepMergeLogic() throws IOException {
                writeFixture("pkg.yaml", """
                        things:
                          thing:
                            scalar: package
                            config:
                              scalar1: package
                              scalar2: package
                              map1:
                                scalar1: package
                                scalar2: package
                              list1:
                                - package
                        """);

                Path main = writeFixture("main.yaml", """
                        packages:
                          p1: !include pkg.yaml
                        things:
                          thing:
                            main_only_scalar: "preserved"
                            config:
                              scalar2: main
                              map1:
                                scalar2: main
                                scalar3: "main_only"
                              map2:
                                new_key: "main_only"
                              list1:
                                - main
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                Map<Object, @Nullable Object> thing = (Map<Object, @Nullable Object>) getNestedValue(data, "things",
                        "thing");

                // 1. Verify Overwrites (Main wins)
                assertThat(getNestedValue(thing, "config", "scalar2"), equalTo("main"));
                assertThat(getNestedValue(thing, "config", "map1", "scalar2"), equalTo("main"));

                // 2. Verify Package-only values are preserved
                assertThat(getNestedValue(thing, "scalar"), equalTo("package"));
                assertThat(getNestedValue(thing, "config", "scalar1"), equalTo("package"));
                assertThat(getNestedValue(thing, "config", "map1", "scalar1"), equalTo("package"));

                // 3. Verify Main-only values are preserved (The Union)
                assertThat(getNestedValue(thing, "main_only_scalar"), equalTo("preserved"));
                assertThat(getNestedValue(thing, "config", "map1", "scalar3"), equalTo("main_only"));
                assertThat(getNestedValue(thing, "config", "map2", "new_key"), equalTo("main_only"));

                // 4. Verify List behavior (Append)
                assertThat(getNestedValue(thing, "config", "list1"), equalTo(List.of("package", "main")));
            }

            @Nested
            @DisplayName("PackageMergeHelpers")
            class PackageMergeHelpers {

                @Test
                @DisplayName("!remove directive deletes specific keys from the merge result")
                void removeDirectiveWorks() throws IOException {
                    writeFixture("pkg.yaml", """
                            things:
                              thing:
                                label: to_remove
                                scalar: package
                                config:
                                  scalar1: package
                                  map1: { key: val }
                              whole_thing_removed: { key: val }
                              thing_to_keep: { status: 'safe' }
                            """);

                    Path main = writeFixture("main.yaml", """
                            packages:
                              p1: !include pkg.yaml
                            things:
                              thing:
                                label: !remove
                                config:
                                  map1: !remove
                              whole_thing_removed: !remove
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);
                    @SuppressWarnings("unchecked")
                    Map<Object, @Nullable Object> thing = (Map<Object, @Nullable Object>) getNestedValue(data, "things",
                            "thing");

                    // 1. Verify Removals
                    assertThat(thing, not(hasKey("label")));
                    assertThat((Map<?, ?>) getNestedValue(thing, "config"), not(hasKey("map1")));
                    assertThat((Map<?, ?>) getNestedValue(data, "things"), not(hasKey("whole_thing_removed")));

                    // 2. Verify Survival of Neighbors
                    assertThat(getNestedValue(thing, "scalar"), equalTo("package"));
                    assertThat(getNestedValue(thing, "config", "scalar1"), equalTo("package"));
                    assertThat(getNestedValue(data, "things", "thing_to_keep", "status"), equalTo("safe"));
                }

                @Test
                @DisplayName("!replace directive overwrites complex nodes instead of merging them")
                void replaceDirectiveWorks() throws IOException {
                    writeFixture("pkg.yaml", """
                            things:
                              thing:
                                map1:
                                  scalar1: package
                                  scalar2: package
                                list1:
                                  - package
                                scalar_to_keep: package
                            """);

                    Path main = writeFixture("main.yaml", """
                            packages:
                              p1: !include pkg.yaml
                            things:
                              thing:
                                map1: !replace
                                  scalar1: main
                                list1: !replace
                                  - main
                            """);

                    Map<Object, @Nullable Object> data = loadFixture(main);
                    @SuppressWarnings("unchecked")
                    Map<Object, @Nullable Object> thing = (Map<Object, @Nullable Object>) getNestedValue(data, "things",
                            "thing");

                    // !replace results in ONLY main's data
                    assertThat(getNestedValue(thing, "map1"), equalTo(Map.of("scalar1", "main")));
                    assertThat(getNestedValue(thing, "list1"), equalTo(List.of("main")));
                    assertThat(getNestedValue(thing, "scalar_to_keep"), equalTo("package"));
                }

                @Nested
                @DisplayName("Defensive Fallbacks (Outside Package Context)")
                class DefensiveFallbacks {

                    @Test
                    @DisplayName("!remove in Map: Should self-delete even without a merge")
                    void removeMapSafety() throws IOException {
                        Path main = writeFixture("main_only.yaml", """
                                config:
                                  active: true
                                  junk: !remove
                                """);

                        Map<Object, @Nullable Object> data = loadFixture(main);

                        assertThat(getNestedValue(data, "config"), is(instanceOf(Map.class)));
                        assertThat((Map<?, ?>) getNestedValue(data, "config"), not(hasKey("junk")));
                        assertThat(getNestedValue(data, "config", "active"), is(true));
                    }

                    @Test
                    @DisplayName("!remove in List: Should purge the entry even without a merge")
                    void removeListSafety() throws IOException {
                        Path main = writeFixture("list_only.yaml", """
                                items:
                                  - "A"
                                  - !remove
                                  - "B"
                                """);

                        Map<Object, @Nullable Object> data = loadFixture(main);
                        List<?> items = (List<?>) data.get("items");

                        assertThat(items, contains("A", "B"));
                        assertThat(items, hasSize(2));
                    }

                    @Test
                    @DisplayName("!replace: Should unwrap to raw content to avoid 'Opaque Tag' artifacts")
                    void replaceSafety() throws IOException {
                        Path main = writeFixture("replace_only.yaml", """
                                settings:
                                  mode: !replace "standalone"
                                  options: !replace
                                    speed: fast
                                """);

                        Map<Object, @Nullable Object> data = loadFixture(main);

                        assertThat(getNestedValue(data, "settings", "mode"), equalTo("standalone"));
                        assertThat(getNestedValue(data, "settings", "options", "speed"), equalTo("fast"));
                    }

                    @Test
                    @DisplayName("Replace with insert works")
                    void replaceWithInsert() throws IOException {
                        Path main = writeFixture("replace_only.yaml", """
                                templates:
                                  test: bar

                                settings:
                                  mode: !replace
                                    foo: !insert test
                                """);

                        Map<Object, @Nullable Object> data = loadFixture(main);

                        assertThat(getNestedValue(data, "settings", "mode", "foo"), equalTo("bar"));
                    }

                    @Test
                    @DisplayName("Replace with include works")
                    void replaceWithInclude() throws IOException {
                        writeFixture("test.yaml", "bar");
                        Path main = writeFixture("replace_only.yaml", """
                                templates:
                                  test: bar

                                settings:
                                  mode: !replace
                                    foo: !include test.yaml
                                """);

                        Map<Object, @Nullable Object> data = loadFixture(main);

                        assertThat(getNestedValue(data, "settings", "mode", "foo"), equalTo("bar"));
                    }
                }
            }
        }

        @Nested
        @DisplayName("Conflict Resolution")
        class Conflicts {

            @Test
            @DisplayName("Earlier packages take precedence over later packages in the sequence")
            void earlierPackageWinsConflict() throws IOException {
                // Package A: The first one processed
                writeFixture("pkg_a.inc.yaml", """
                        things:
                          shared_id:
                            status: "from_a"
                            only_in_a: "value_a"
                        """);

                // Package B: The second one processed
                writeFixture("pkg_b.inc.yaml", """
                        things:
                          shared_id:
                            status: "from_b"
                            only_in_b: "value_b"
                        """);

                Path main = writeFixture("main.yaml", """
                        packages:
                          instance_1: !include pkg_a.inc.yaml
                          instance_2: !include pkg_b.inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                // 1. Precedence Check: 'from_a' should remain because it was merged into Main
                // first
                assertThat(getNestedValue(data, "things", "shared_id", "status"), equalTo("from_a"));

                // 2. Union Check: Maps are still merged, so unique keys from B are still added
                assertThat(getNestedValue(data, "things", "shared_id", "only_in_a"), equalTo("value_a"));
                assertThat(getNestedValue(data, "things", "shared_id", "only_in_b"), equalTo("value_b"));
            }
        }
    }

    @Nested
    @DisplayName("Universal Structural Tags (Outside Package Context)")
    class UniversalStructuralTagTests {

        @Test
        @DisplayName("!replace: Fallback to raw content for all YAML types (Map, List, Scalar)")
        @SuppressWarnings("null")
        void replaceFallbackForAllTypes() throws IOException {
            String yaml = """
                    map_context:
                      key: !replace { a: b }
                    list_context:
                      key: !replace [ item1, item2 ]
                    scalar_context:
                      key: !replace "just a string"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            // Verify Map preservation: should treat as standard Map
            Object mapNode = getNestedValue(data, "map_context", "key");
            assertThat(mapNode, instanceOf(Map.class));
            assertThat(((Map<?, ?>) mapNode).get("a"), is("b"));

            // Verify List preservation: should treat as standard List
            Object listNode = getNestedValue(data, "list_context", "key");
            assertThat(listNode, instanceOf(List.class));
            assertThat((List<?>) listNode, contains("item1", "item2"));

            // Verify Scalar preservation: should treat as standard String
            Object scalarNode = getNestedValue(data, "scalar_context", "key");
            assertThat(scalarNode, is("just a string"));
        }

        @Test
        @DisplayName("!remove: Map key removal (Primary Use Case)")
        void removeFunctionsGloballyInMaps() throws IOException {
            String yaml = """
                    target:
                      victim_key: !remove "delete me"
                      safe_key: "keep me"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) data.get("target");

            assertThat("The specified key should be removed from the map", target, not(hasKey("victim_key")));
            assertThat("Unrelated keys must be preserved", target, hasEntry("safe_key", "keep me"));
        }

        @Test
        @DisplayName("!remove: List item removal (The 'Filter' Use Case)")
        void removeFunctionsGloballyInLists() throws IOException {
            // If !remove is inside a list, the intuition is that it removes
            // the item it references from the list.
            String yaml = """
                    my_list:
                      - "item 1"
                      - "item 2"
                      - !remove
                    """;

            Map<Object, Object> data = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) Objects.requireNonNull(data.get("my_list"));

            // The result should only contain "item 2"
            assertThat("The list should be filtered by the !remove tag", list, contains("item 1", "item 2"));
            assertThat("The list size should reflect the removal", list.size(), is(2));
        }
    }

    @Nested
    @DisplayName("Merge Key (<<) Specifications")
    class MergeKeyTests {

        /**
         * Ensures our custom {@code RecursiveTransformer} implementation adheres to the
         * official YAML 1.1 Merge Key Language-Independent Type specification.
         * <p>
         * Because we manually handle {@code <<} to support dynamic tags like {@code !if}
         * and {@code !include}, we must strictly replicate the "First-Key-Wins"
         * precedence rules to avoid breaking standard YAML behavior.
         * * @see <a href="https://yaml.org/type/merge.html">YAML 1.1 Merge Key Spec</a>
         */
        @Nested
        @DisplayName("Standard YAML 1.1 Compliance")
        class StandardCompliance {

            @Test
            @DisplayName("Spec: Local keys override merged keys")
            void localKeyPrecedence() throws IOException {
                String yaml = """
                        base: &base
                          status: "original"
                          type: "base"
                        target:
                          status: "overridden"
                          <<: *base
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                // Local 'status' exists first, so 'original' is ignored.
                assertThat(getNestedValue(data, "target", "status"), equalTo("overridden"));
                assertThat(getNestedValue(data, "target", "type"), equalTo("base"));
            }

            @Test
            @DisplayName("Spec: Sequence Merge - Earlier mappings override later ones")
            void sequenceMergePrecedence() throws IOException {
                String yaml = """
                        m1: &m1 { val: "first" }
                        m2: &m2 { val: "second" }
                        target:
                          <<: [*m1, *m2]
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                // m1 is processed first; val is set to "first".
                // When m2 is processed, val already exists, so "second" is skipped.
                assertThat(getNestedValue(data, "target", "val"), equalTo("first"));
            }

            @Test
            @DisplayName("Spec: Multiple merge keys - First merge key wins")
            void multipleMergeKeysFirstWins() throws IOException {
                String yaml = """
                        m1: &m1 { common: "from_m1", unique_a: 1 }
                        m2: &m2 { common: "from_m2", unique_b: 2 }
                        target:
                          <<: *m1
                          <<: *m2
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                // Following the "unless the key already exists" rule:
                // 1. unique_a and common ("from_m1") are inserted.
                // 2. common already exists, so "from_m2" is ignored.
                // 3. unique_b is inserted.
                assertThat(getNestedValue(data, "target", "common"), equalTo("from_m1"));
                assertThat(getNestedValue(data, "target", "unique_a"), equalTo(1));
                assertThat(getNestedValue(data, "target", "unique_b"), equalTo(2));
            }

            @Test
            @DisplayName("Spec: Complex precedence (Local > Merge1 > Merge2)")
            void complexPrecedence() throws IOException {
                String yaml = """
                        m1: &m1 { a: "m1", b: "m1", c: "m1" }
                        m2: &m2 { a: "m2", b: "m2", d: "m2" }
                        target:
                          a: "local"
                          <<: *m1
                          <<: *m2
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);

                assertAll(() -> assertThat("Local wins", getNestedValue(data, "target", "a"), equalTo("local")),
                        () -> assertThat("First merge wins", getNestedValue(data, "target", "b"), equalTo("m1")),
                        () -> assertThat("From first merge", getNestedValue(data, "target", "c"), equalTo("m1")),
                        () -> assertThat("From second merge", getNestedValue(data, "target", "d"), equalTo("m2")));
            }

            @Test
            @DisplayName("Spec: Deep merge is NOT supported")
            void mergeIsNotRecursive() throws IOException {
                String yaml = """
                        default_meta: &default_meta
                          tags: { level: "info", persistent: true }
                        target:
                          <<: *default_meta
                          tags: { level: "debug" }
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                // The local 'tags' key blocks the entire merged 'tags' map.
                assertThat(getNestedValue(data, "target", "tags", "level"), equalTo("debug"));
                assertThat(getNestedValue(data, "target", "tags", "persistent"), is(nullValue()));
            }
        }

        @Nested
        @DisplayName("Alias Support")
        class AliasSupport {

            @Test
            @DisplayName("Merge with simple alias mapping")
            void mergeWithAliasMapping() throws IOException {
                String yaml = """
                        base: &base
                          x: 1
                          y: 2
                          a: [foo, bar]
                        target:
                          z: 3
                          <<: *base
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                assertThat(getNestedValue(data, "target", "x"), equalTo(1));
                assertThat(getNestedValue(data, "target", "y"), equalTo(2));
                assertThat(getNestedValue(data, "target", "a"), equalTo(List.of("foo", "bar")));
                assertThat(getNestedValue(data, "target", "z"), equalTo(3));
            }

            @Test
            @DisplayName("Merge with alias sequence of mappings")
            void mergeWithAliasSequenceOfMappings() throws IOException {
                String yaml = """
                        m1: &m1 { a: 10 }
                        m2: &m2 { b: 20 }
                        target:
                          <<: [*m1, *m2]
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                assertThat(getNestedValue(data, "target", "a"), equalTo(10));
                assertThat(getNestedValue(data, "target", "b"), equalTo(20));
            }

            @Test
            @DisplayName("Merge with alias referencing another alias")
            void mergeWithAliasReferencingAlias() throws IOException {
                String yaml = """
                        base1: &base1 { foo: 1 }
                        base2: &base2 { <<: *base1 }
                        target:
                          <<: *base2
                        """;

                Map<Object, @Nullable Object> data = loadYaml(yaml);
                assertThat(getNestedValue(data, "target", "foo"), equalTo(1));
            }
        }

        @Nested
        @DisplayName("Conditional Integration (!if)")
        class ConditionalIntegration {

            @Test
            @DisplayName("Merge with !if resolving to a mapping (truthy)")
            void mergeWithTruthyIf() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          is_prod: true
                          prod_settings: { timeout: 30, retry: 3 }
                        target:
                          existing: "value"
                          <<: !if
                            if: !sub ${is_prod}
                            then: !sub ${prod_settings}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "target", "timeout"), equalTo(30));
                assertThat(getNestedValue(data, "target", "existing"), equalTo("value"));
            }

            @Test
            @DisplayName("Merge with !if resolving to alternative mapping (else branch)")
            void mergeWithFalsyIfWithElseBranch() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          env: "dev"
                        target:
                          <<: !if
                            if: !sub "${env == 'prod'}"
                            then: { color: "red" }
                            else: { color: "green" }
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "target", "color"), equalTo("green"));
            }

            @Test
            @DisplayName("Merge with !if with falsy condition without else results in a no-op")
            @SuppressWarnings("null")
            void mergeWithFalsyIfWithoutElse() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          is_prod: false
                        target:
                          existing: "preserved"
                          <<: !if
                            if: !sub ${is_prod}
                            then: { secret: "password" }
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) data.get("target");

                assertThat(target, equalTo(Map.of("existing", "preserved")));

                // No warnings should be emitted for this valid use case
                assertThat(logSession.getTrackedWarnings(), is(empty()));
            }

            @Test
            @DisplayName("Merge with sequence form !if (multiple branches)")
            void mergeWithSequenceIf() throws IOException {
                Path main = writeFixture("main.yaml", """
                        target:
                          <<: !if
                            - if: false
                              then: { branch: 1 }
                            - elseif: true
                              then: { branch: 2 }
                            - else: { branch: 3 }
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "target", "branch"), equalTo(2));
            }

            @Test
            @DisplayName("Merge key !if inherits substitution context from parent !sub tag")
            void mergeIfInheritsParentSub() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          is_active: true
                          feature_data: { status: "enabled" }
                        target: !sub
                          <<: !if
                            if: ${is_active}
                            then: ${feature_data}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) data.get("target");

                // The !if should have resolved using the parent's context
                assertThat(target, hasEntry("status", "enabled"));
                // Ensure the merge key itself is flattened/removed
                assertThat(target, not(hasKey("<<")));
            }
        }

        @Nested
        @DisplayName("Inclusion Strategy")
        class Inclusion {

            @Test
            @DisplayName("Merge with !include in scalar form")
            void mergeWithScalarInclude() throws IOException {
                writeFixture("inc.yaml", "foo: include1");
                Path main = writeFixture("main.yaml", """
                        simple:
                          <<: !include inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "foo"), equalTo("include1"));
            }

            @Test
            @DisplayName("Merge with !include in mapping form with variables")
            void mergeWithMappingInclude() throws IOException {
                writeFixture("inc.yaml", "greeting: !sub 'Hello ${name}'");
                Path main = writeFixture("main.yaml", """
                        simple:
                          <<: !include
                            file: inc.yaml
                            vars:
                              name: "World"
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "greeting"), equalTo("Hello World"));
            }

            @Test
            @DisplayName("Merge with !include using substitutions in filename and vars")
            void mergeWithIncludeSubstitutions() throws IOException {
                writeFixture("production.yaml", """
                        mode: prod
                        owner: !sub ${owner}
                        """);
                Path main = writeFixture("main.yaml", """
                        variables:
                          env: production
                          user: gemini

                        target:
                          <<: !include
                            file: !sub "${env}.yaml"
                            vars:
                              owner: !sub "${user}"
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "target", "mode"), equalTo("prod"));
                assertThat(getNestedValue(data, "target", "owner"), equalTo("gemini"));
            }

            @Test
            @DisplayName("Merge with !include in a map")
            void mergeWithIncludeInMap() throws IOException {
                writeFixture("inc.yaml", "foo: include1");
                Path main = writeFixture("main.yaml", """
                        simple:
                          <<:
                            bar: !include inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "bar", "foo"), equalTo("include1"));
            }

            @Test
            @DisplayName("Merge with !include non-existent file results in a no-op")
            void mergeWithNonExistentInclude() throws IOException {
                Path main = writeFixture("main.yaml", "simple: { <<: !include missing.yaml }");

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple"), equalTo(Map.of()));
                assertThat(logSession.getTrackedWarnings(), not(hasItem(containsString("Expected a mapping"))));
            }
        }

        @Nested
        @DisplayName("Substitution and Inheritance")
        class Substitution {

            @Test
            @DisplayName("Merge keys work with !sub for dynamic mapping injection")
            void mergeWithSubstitution() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          map1:
                            foo: bar
                            baz: "${foo}"
                        simple:
                          <<: !sub ${map1}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "foo"), equalTo("bar"));
                assertThat(getNestedValue(data, "simple", "baz"), equalTo("${foo}"));
            }

            @Test
            @DisplayName("Merge keys handle undefined variables as no-ops")
            void mergeWithUndefinedVariable() throws IOException {
                Path main = writeFixture("main.yaml", """
                        target:
                          existing_key: "preserved"
                          <<: !sub ${undefined_var}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) Objects
                        .requireNonNull(getNestedValue(data, "target"));

                assertThat(target, hasEntry("existing_key", "preserved"));
                assertThat(target.size(), equalTo(1));

                assertThat(logSession.getTrackedWarnings(), not(hasItem(containsString("Expected a mapping"))));
            }

            @Test
            @DisplayName("Merge keys support lists of substitutions")
            void mergeWithSubstitutionsThatReturnsListOfMaps() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          map1: { foo: bar }
                          map2: { qux: quux }
                        array_merge:
                          <<: !sub ${[map1, map2]}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "array_merge", "foo"), equalTo("bar"));
                assertThat(getNestedValue(data, "array_merge", "qux"), equalTo("quux"));
            }

            @Test
            @DisplayName("Merge key inherits !sub context from parent node")
            void mergeInheritsParentSub() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          foo: { key: "inherited_value" }
                        target: !sub
                          existing: "constant"
                          <<: ${foo}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) data.get("target");

                assertThat(target, hasEntry("key", "inherited_value"));
                assertThat(target, hasEntry("existing", "constant"));
            }

            @Test
            @DisplayName("!nosub on parent allows internal !sub on merge keys to process")
            void mergeWithParentNoSub() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          map1: { foo: bar }
                        parent_nosub: !nosub
                          <<: !sub ${map1}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "parent_nosub", "foo"), equalTo("bar"));
            }

            @Test
            @DisplayName("Merge keys work with !sub inside a map")
            void mergeWithSubstitutionInMap() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          map1:
                            foo: bar
                            baz: "${foo}"
                        simple:
                          <<:
                            qux: !sub ${map1}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "qux", "foo"), equalTo("bar"));
                assertThat(getNestedValue(data, "simple", "qux", "baz"), equalTo("${foo}"));
            }

            @Test
            @DisplayName("Merge keys work with !sub inside a list")
            void mergeWithSubstitutionInList() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          map1:
                            foo: bar
                            baz: "${foo}"
                        simple:
                          <<:
                            qux:
                              - !sub ${map1}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "qux"),
                        equalTo(List.of((Map.of("foo", "bar", "baz", "${foo}")))));
            }

            @Test
            @DisplayName("Conditional logic in !sub without else results in a no-op merge")
            void mergeWithConditionalWithoutElse() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          empty_map: {}
                        conditionally_empty:
                          <<: !sub ${empty_map if false}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "conditionally_empty"), equalTo(Map.of()));

                assertThat(logSession.getTrackedWarnings(), is(empty()));
            }
        }

        @Nested
        @DisplayName("Templates and Collections")
        class TemplatesAndCollections {

            @Test
            @DisplayName("Merge keys work with !insert templates (scalar form)")
            void mergeWithScalarTemplates() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          base: { foo: bar }
                        simple:
                          <<: !insert base
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "foo"), equalTo("bar"));
            }

            @Test
            @DisplayName("Merge keys work with !insert templates (mapping form)")
            void mergeWithMappingTemplate() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          device:
                            type: !sub ${dev_type}
                            vendor: "openHAB"
                        simple:
                          <<: !insert
                            template: device
                            vars:
                              dev_type: Light
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                assertThat(getNestedValue(data, "simple", "type"), equalTo("Light"));
                assertThat(getNestedValue(data, "simple", "vendor"), equalTo("openHAB"));
            }
        }

        @Test
        @DisplayName("Merge keys in !include vars are visible to included file")
        void mergeKeysInIncludeVars() throws IOException {
            writeFixture("include.inc.yaml", "foo: !sub ${foo}");

            Path main = writeFixture("main.yaml", """
                    packages:
                      foo_package: !include
                        file: include.inc.yaml
                        vars:
                          <<:
                            foo: bar
                    """);

            Map<Object, @Nullable Object> data = loadFixture(main);

            assertThat(getNestedValue(data, "foo"), equalTo("bar"));
        }

        @Nested
        @DisplayName("Type Fidelity")
        class TypeFidelity {

            @Test
            @DisplayName("Merge keys retain primitive types (int, boolean) from !include")
            void mergeRetainsTypesFromInclude() throws IOException {
                // inc.yaml contains actual boolean and int types
                writeFixture("inc.yaml", """
                        active: true
                        timeout: 30
                        """);
                Path main = writeFixture("main.yaml", """
                        device:
                          <<: !include inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                Object active = getNestedValue(data, "device", "active");
                Object timeout = getNestedValue(data, "device", "timeout");

                // Verify values AND types
                assertThat(active, allOf(equalTo(true), instanceOf(Boolean.class)));
                assertThat(timeout, allOf(equalTo(30), instanceOf(Integer.class)));
            }

            @Test
            @DisplayName("Merge keys retain types from !sub map injection")
            void mergeRetainsTypesFromSub() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          settings:
                            enabled: true
                            port: 8080
                        target:
                          <<: !sub ${settings}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                Object enabled = getNestedValue(data, "target", "enabled");
                Object port = getNestedValue(data, "target", "port");

                assertThat(enabled, instanceOf(Boolean.class));
                assertThat(port, instanceOf(Integer.class));
            }

            @Test
            @DisplayName("Merge keys retain types when using !insert with variable overrides")
            void mergeRetainsTypesFromInsert() throws IOException {
                Path main = writeFixture("main.yaml", """
                        templates:
                          base:
                            value: !sub ${in_val}
                        target:
                          <<: !insert
                            template: base
                            vars:
                              in_val: 100
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                Object value = getNestedValue(data, "target", "value");

                // If bug exists, value would be "100" (String) instead of 100 (Integer)
                assertThat(value, allOf(equalTo(100), instanceOf(Integer.class)));
            }

            @Test
            @DisplayName("Merge keys retain nested List and Map structures")
            void mergeRetainsComplexStructures() throws IOException {
                writeFixture("inc.yaml", """
                        tags: [a, b, c]
                        config: { level: 1 }
                        """);
                Path main = writeFixture("main.yaml", """
                        target:
                          <<: !include inc.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);

                assertThat(getNestedValue(data, "target", "tags"), instanceOf(List.class));
                assertThat(getNestedValue(data, "target", "config"), instanceOf(Map.class));
                assertThat(getNestedValue(data, "target", "config", "level"), instanceOf(Integer.class));
            }
        }

        @Nested
        @DisplayName("Merge Isolation (Pattern Leak Tests)")
        class MergeIsolationTests {

            @Test
            @DisplayName("Isolation: !sub parent with deep pattern payloads")
            void isolationSubDeep() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          val: "leak"
                          payload:
                            scalar_pattern: "${val}"
                            list_of_patterns: ["${val}", "${val}"]
                            list_of_map:
                              - { item: "${val}" }
                            list_of_list:
                              - ["${val}"]
                            map_of_patterns:
                              key1: "${val}"
                            map_of_list_patterns:
                              key1: ["${val}"]
                        target: !sub
                          <<: ${payload}
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                verifyDeepStructure(data, "target");
            }

            @Test
            @DisplayName("Isolation: !include with deep pattern payloads")
            void isolationIncludeDeep() throws IOException {
                writeFixture("external.yaml", """
                        scalar_pattern: "${val}"
                        list_of_patterns: ["${val}", "${val}"]
                        list_of_map:
                          - { item: "${val}" }
                        list_of_list:
                          - ["${val}"]
                        map_of_patterns:
                          key1: "${val}"
                        map_of_list_patterns:
                          key1: ["${val}"]
                        """);
                Path main = writeFixture("main.yaml", """
                        variables:
                          val: "leak"
                        target: !sub
                          <<: !include external.yaml
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                verifyDeepStructure(data, "target");
            }

            @Test
            @DisplayName("Isolation: !insert with deep pattern payloads")
            void isolationInsertDeep() throws IOException {
                Path main = writeFixture("main.yaml", """
                        variables:
                          val: "leak"
                        templates:
                          deep_tpl:
                            scalar_pattern: "${val}"
                            list_of_patterns: ["${val}", "${val}"]
                            list_of_map:
                              - { item: "${val}" }
                            list_of_list:
                              - ["${val}"]
                            map_of_patterns:
                              key1: "${val}"
                            map_of_list_patterns:
                              key1: ["${val}"]
                        target: !sub
                          <<: !insert deep_tpl
                        """);

                Map<Object, @Nullable Object> data = loadFixture(main);
                verifyDeepStructure(data, "target");
            }

            @SuppressWarnings("null")
            private void verifyDeepStructure(Map<Object, Object> data, String rootKey) {
                @SuppressWarnings("unchecked")
                Map<Object, @Nullable Object> target = (Map<Object, @Nullable Object>) Objects
                        .requireNonNull(data.get(rootKey));

                assertAll("Pattern Isolation Check: " + rootKey,
                        () -> assertThat("scalar_pattern should remain a raw placeholder string",
                                target.get("scalar_pattern"), equalTo("${val}")),

                        () -> assertThat("list_of_patterns[0] should remain a raw placeholder string",
                                ((List<?>) target.get("list_of_patterns")).get(0), equalTo("${val}")),

                        () -> assertThat("list_of_map[0].item should remain a raw placeholder string",
                                ((Map<?, ?>) ((List<?>) target.get("list_of_map")).get(0)).get("item"),
                                equalTo("${val}")),

                        () -> assertThat("list_of_list[0][0] should remain a raw placeholder string",
                                ((List<?>) ((List<?>) target.get("list_of_list")).get(0)).get(0), equalTo("${val}")),

                        () -> assertThat("map_of_patterns.key1 should remain a raw placeholder string",
                                ((Map<?, ?>) target.get("map_of_patterns")).get("key1"), equalTo("${val}")),

                        () -> assertThat("map_of_list_patterns.key1[0] should remain a raw placeholder string",
                                ((List<?>) ((Map<?, ?>) target.get("map_of_list_patterns")).get("key1")).get(0),
                                equalTo("${val}")));
            }
        }
    }

    @Nested
    @DisplayName("File Cache Behavior")
    class FileCacheTests {
        @Test
        @DisplayName("Caches include bytes and mtime on first load")
        void cachesIncludeEntry() throws Exception {
            Path included = writeFixture("cache_included.inc.yaml", "key: value");
            Path main = writeFixture("main_cache.yaml", """
                    data1: !include cache_included.inc.yaml
                    data2: !include cache_included.inc.yaml
                    """);

            ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
            YamlComposer.load(main, p -> {
            }, logSession, includeCache);

            Path real = included.toRealPath();
            assertTrue(includeCache.containsKey(real));
            CacheEntry entry = includeCache.get(real);
            assertNotNull(entry);
            assertArrayEquals(Files.readAllBytes(real), entry.bytes());
            assertEquals(Files.getLastModifiedTime(real).toMillis(), entry.mtime());
        }

        @Test
        @DisplayName("Refreshes cached entry when mtime differs")
        void refreshesCacheOnMtimeChange() throws Exception {
            Path included = writeFixture("cache_refresh.inc.yaml", "a: old");
            Path main = writeFixture("main_refresh.yaml", "data: !include cache_refresh.inc.yaml");

            Path real = included.toRealPath();

            ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
            // Insert a stale entry with an older mtime and different bytes
            long staleMtime = Math.max(0L, Files.getLastModifiedTime(real).toMillis() - 10_000L);
            includeCache.put(real, new CacheEntry("stale".getBytes(), staleMtime));

            // Update the file to ensure a newer mtime and new content
            Files.writeString(included, "a: refreshed");

            YamlComposer.load(main, p -> {
            }, logSession, includeCache);

            CacheEntry entry = includeCache.get(real);
            assertNotNull(entry);
            assertArrayEquals(Files.readAllBytes(real), entry.bytes());
            assertEquals(Files.getLastModifiedTime(real).toMillis(), entry.mtime());
        }
    }

    /**
     * Load a YAML fixture file from the test resources.
     * <p>
     * This helper method simplifies loading fixture files by automatically resolving the path
     * relative to the standard test resources directory and parsing the YAML content.
     * <p>
     * The method also includes enhanced error handling to provide clear context about
     * which test and fixture caused a failure, making it easier to diagnose issues
     * when a fixture cannot be loaded or parsed correctly.
     * <p>
     * The returned Map is guaranteed to contain no Placeholder instances, as the method will
     * fail the test if any unresolved placeholders are found in the loaded data.
     * This ensures that all placeholders are properly resolved before the test continues.
     *
     * @param source the name of the YAML file to load (relative to the fixture directory)
     * @return the parsed YAML content as a Map
     * @throws IOException if an error occurs reading the file
     */
    @SuppressWarnings({ "unchecked", "null" })
    private Map<Object, @Nullable Object> loadFixture(Path source) throws IOException {
        // If 'source' is absolute (like a temp file), resolve() returns 'source' as is.
        // If it's relative, it appends it to SOURCE_PATH.
        Path filePath = source.isAbsolute() ? source : SOURCE_PATH.resolve(source);

        try {
            ConcurrentHashMap<Path, CacheEntry> includeCache = new ConcurrentHashMap<>();
            Object result = YamlComposer.load(filePath, path -> {
            }, logSession, includeCache);

            if (result instanceof Map<?, ?> dataMap) {
                Map<Object, @Nullable Object> map = (Map<Object, @Nullable Object>) dataMap;
                assertNoPlaceholders(map);
                return map;
            }

            fail("Fixture did not produce a Map structure: " + source);
        } catch (Exception e) {
            fail("\n%s#%s: %s\nError loading fixture '%s': %s".formatted(
                    currentTest.getTestClass().get().getSimpleName(), currentTest.getTestMethod().get().getName(),
                    currentTest.getDisplayName(), source, e.getMessage()), e);
        }
        return Map.of();
    }

    private Map<Object, @Nullable Object> loadYaml(String content) throws IOException {
        Path tempFile = Objects.requireNonNull(sharedTempDir).resolve("inline-test-" + System.nanoTime() + ".yaml");
        Files.writeString(tempFile, content);
        return loadFixture(tempFile);
    }

    /**
     * Recursively assert that the given object graph contains no Placeholder
     * instances. Uses identity-based cycle detection to avoid infinite loops on
     * self-referencing structures.
     */
    private void assertNoPlaceholders(Object root) {
        Objects.requireNonNull(root);
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ArrayDeque<String> path = new ArrayDeque<>();

        checkForPlaceholders(root, visited, path);
    }

    private void checkForPlaceholders(Object obj, Set<Object> visited, ArrayDeque<String> path) {
        if (obj == null || obj instanceof Number || obj instanceof Boolean) {
            return;
        }

        if (obj instanceof String str) {
            // Check for instances where String.valueOf or toString was called on a Placeholder,
            // which would indicate an unresolved placeholder that was coerced to a string during processing
            if (str.matches("(?s)^[A-Z].+Placeholder\\[.*")) {
                failAt(path, "unresolved Placeholder string: " + str);
            }
            return;
        }

        if (!visited.add(obj)) {
            return;
        }

        if (obj instanceof Placeholder) {
            failAt(path, "unresolved Placeholder instance: " + obj.getClass().getName());
        }

        if (obj instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                String keyName = String.valueOf(e.getKey());
                path.addLast(keyName);
                checkForPlaceholders(e.getKey(), visited, path); // Check the key itself
                checkForPlaceholders(e.getValue(), visited, path); // Check the value
                path.removeLast();
            }
        } else if (obj instanceof Iterable<?> it) {
            int i = 0;
            for (Object o : it) {
                path.addLast("[" + (i++) + "]");
                checkForPlaceholders(o, visited, path);
                path.removeLast();
            }
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                path.addLast("[" + i + "]");
                checkForPlaceholders(Array.get(obj, i), visited, path);
                path.removeLast();
            }
        }
    }

    private void failAt(ArrayDeque<String> path, String reason) {
        TestInfo testInfo = Objects.requireNonNull(currentTest);
        String p = path.isEmpty() ? "" : " at " + String.join("/", path);
        fail("\n%s#%s: %s\nFound %s%s".formatted(testInfo.getTestClass().get().getSimpleName(),
                testInfo.getTestMethod().get().getName(), testInfo.getDisplayName(), reason, p));
    }

    /**
     * Writes a helper YAML file to the shared temporary directory.
     *
     * @param fileName The name of the file (e.g.,
     *            "subOverIncludeLiterals.inc.yaml")
     * @param content The YAML content for the include file
     */
    protected Path writeFixture(String fileName, String content) throws IOException {
        Path inputPath = Path.of(fileName);

        if (inputPath.isAbsolute()) {
            throw new IllegalArgumentException("Include filename must be relative: " + fileName);
        }

        Path includePath = Objects.requireNonNull(sharedTempDir).resolve(inputPath).normalize();
        if (!includePath.startsWith(sharedTempDir)) {
            throw new IllegalArgumentException("Target path is outside of the temp directory: " + includePath);
        }

        // Create parent directories if the filename contains a subfolder (e.g.,
        // "includes/file.yaml")
        if (includePath.getParent() != null) {
            Files.createDirectories(includePath.getParent());
        }

        Files.writeString(includePath, content);
        return includePath;
    }

    /**
     * Retrieve a nested value from a map using the provided keys.
     * <p>
     * This method navigates through a map structure using the given keys in order.
     * If a key is not found or the current value is not a map, the method returns
     * {@code null}.
     *
     * @param data the map to retrieve the value from; must not be {@code null}.
     * @param key the sequence of keys to navigate the map; must not be
     *            {@code null}.
     * @return the nested value if found, or {@code null} if a key is missing or the
     *         value is not a map.
     */
    private static @Nullable Object getNestedValue(Map<Object, @Nullable Object> data, String... key) {
        if (data == null) {
            return null;
        }
        Object value = data;
        for (String k : key) {
            if (value instanceof Map<?, ?> map) {
                value = map.get(k);
            } else {
                return null;
            }
        }
        return value;
    }
}
