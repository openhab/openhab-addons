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

import java.io.IOException;
import java.nio.file.Path;
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
 * The {@link YamlComposerInsertTagTest} contains tests for the !insert tag functionality in the {@link YamlComposer}
 * class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Insert Tag Specification")
class YamlComposerInsertTagTest extends AbstractYamlComposerTest {

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
        @DisplayName("Templates node can be dynamically generated via substitutions and still be resolved")
        void dynamicTemplatesNodeSupported() throws IOException {
            Path main = writeFixture("main.yaml", """
                    variables:
                      tplname: "my_template"
                      value: "foo"

                    templates:
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
                        key: ${arg1}
                        bool: ${bool}
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
                      tpl: ${global_val}

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
                      vartpl: ${foo}

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
