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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerForTagTest} contains tests for the !for tag functionality in the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("!for Tag")
class YamlComposerForTagTest extends AbstractYamlComposerTest {

    @Nested
    @DisplayName("Simple List Iteration")
    class SimpleListTests {

        @Test
        @DisplayName("Should expand map entries from inline array expression")
        void testInlineArrayIteration() throws IOException {
            String yaml = """
                    servers:
                      !for "env in ['dev', 'prod']":
                        "server_${env}":
                          host: "app-${env}.example.com"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "servers", "server_dev", "host"), is("app-dev.example.com"));
            assertThat(getNestedValue(result, "servers", "server_prod", "host"), is("app-prod.example.com"));
        }

        @Test
        @DisplayName("Should expand map entries from context variable list")
        void testContextVariableListIteration() throws IOException {
            String yaml = """
                    variables:
                      sensors: ["temp_1", "humidity_1"]
                    metrics:
                      !for sensor in sensors:
                        ${sensor}_metric:
                          enabled: true
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "metrics", "temp_1_metric", "enabled"), is(true));
            assertThat(getNestedValue(result, "metrics", "humidity_1_metric", "enabled"), is(true));
        }
    }

    @Nested
    @DisplayName("Sequence / Range Generation")
    class RangeTests {

        @Test
        @DisplayName("Should expand range(start, end) expressions")
        void testRangeGeneration() throws IOException {
            String yaml = """
                    port_configs:
                      !for "i in range(1, 4)":
                        "port_${i}":
                          number: ${8000 + i}
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            Map<Object, @Nullable Object> ports = (Map<Object, @Nullable Object>) Objects
                    .requireNonNull(result.get("port_configs"));

            assertThat(ports.keySet(), contains("port_1", "port_2", "port_3"));
            assertThat(getNestedValue(ports, "port_1", "number"), is(8001));
            assertThat(getNestedValue(ports, "port_2", "number"), is(8002));
            assertThat(getNestedValue(ports, "port_3", "number"), is(8003));
        }

        @Test
        @DisplayName("Should preserve native types of loop variables during expression evaluation")
        void testLoopVariableTypePreservation() throws IOException {
            String yaml = """
                    variables:
                      mixed_list: [100, "200", true, 3.14]
                    result:
                      !for item in mixed_list:
                        "item_${item}": ${item}
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            // Unquoted types preserve their exact runtime objects
            assertThat(getNestedValue(data, "result", "item_100"), is(100));
            assertThat(getNestedValue(data, "result", "item_100"), instanceOf(Integer.class));

            assertThat(getNestedValue(data, "result", "item_true"), is(true));
            assertThat(getNestedValue(data, "result", "item_true"), instanceOf(Boolean.class));

            assertThat(getNestedValue(data, "result", "item_3.14"), is(3.14));
            assertThat(getNestedValue(data, "result", "item_3.14"), instanceOf(Double.class));

            // Numeric strings remain Strings without unintended numeric coercion
            assertThat(getNestedValue(data, "result", "item_200"), is("200"));
            assertThat(getNestedValue(data, "result", "item_200"), instanceOf(String.class));
        }

        @Test
        @DisplayName("Should expand inclusive Ruby-style range [1..3] expressions")
        void testInclusiveRubyRangeGeneration() throws IOException {
            String yaml = """
                    port_configs:
                      !for "i in [1..3]":
                        "port_${i}":
                          number: ${8000 + i}
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            Map<Object, @Nullable Object> ports = (Map<Object, @Nullable Object>) Objects
                    .requireNonNull(result.get("port_configs"));

            assertThat(ports.keySet(), contains("port_1", "port_2", "port_3"));
            assertThat(getNestedValue(ports, "port_1", "number"), is(8001));
            assertThat(getNestedValue(ports, "port_2", "number"), is(8002));
            assertThat(getNestedValue(ports, "port_3", "number"), is(8003));
        }

        @Test
        @DisplayName("Should expand exclusive Ruby-style range [1...4] expressions")
        void testExclusiveRubyRangeGeneration() throws IOException {
            String yaml = """
                    port_configs:
                      !for "i in [1...4]":
                        "port_${i}":
                          number: "${800 + i}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            Map<Object, @Nullable Object> ports = (Map<Object, @Nullable Object>) Objects
                    .requireNonNull(result.get("port_configs"));

            assertThat(ports.keySet(), contains("port_1", "port_2", "port_3"));
        }
    }

    @Nested
    @DisplayName("Map / Dictionary Key-Value Iteration")
    class MapDictTests {

        @Test
        @DisplayName("Should iterate over map entries using 'key, value in map' syntax")
        void testMapKeyValueIteration() throws IOException {
            String yaml = """
                    variables:
                      equipment_channels:
                        ch1: { name: "Main Light", mode: "dimmer" }
                        ch2: { name: "Fan", mode: "switch" }
                    processed_channels:
                      !for "channel_id, config in equipment_channels":
                        "channel_${channel_id}":
                          label: "${config.name}"
                          type: "${config.mode}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "processed_channels", "channel_ch1", "label"), is("Main Light"));
            assertThat(getNestedValue(result, "processed_channels", "channel_ch1", "type"), is("dimmer"));
            assertThat(getNestedValue(result, "processed_channels", "channel_ch2", "label"), is("Fan"));
            assertThat(getNestedValue(result, "processed_channels", "channel_ch2", "type"), is("switch"));
        }
    }

    @Nested
    @DisplayName("Tuple / List Unpacking")
    class UnpackingTests {

        @Test
        @DisplayName("Should unpack nested pairs directly into loop variables")
        void testTupleUnpacking() throws IOException {
            String yaml = """
                    devices:
                      !for "id, type in [['Light1', 'Switch'], ['Temp1', 'Number']]":
                        "${id}":
                          deviceType: "${type}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "devices", "Light1", "deviceType"), is("Switch"));
            assertThat(getNestedValue(result, "devices", "Temp1", "deviceType"), is("Number"));
        }

        @Test
        @DisplayName("Should unpack more than two elements into multiple loop variables")
        void testMultiElementTupleUnpacking() throws IOException {
            String yaml = """
                    records:
                      !for "key, type, status in [['light1', 'Switch', 'active'], ['temp1', 'Sensor', 'idle']]":
                        "${key}":
                          deviceType: "${type}"
                          state: "${status}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "records", "light1", "deviceType"), is("Switch"));
            assertThat(getNestedValue(result, "records", "light1", "state"), is("active"));
            assertThat(getNestedValue(result, "records", "temp1", "deviceType"), is("Sensor"));
            assertThat(getNestedValue(result, "records", "temp1", "state"), is("idle"));
        }

        @Test
        @DisplayName("Should set extra variables to null when row has fewer elements")
        void testTupleUnpackingWithNulls() throws IOException {
            String yaml = """
                    records:
                      !for "a, b, c in [[1, 2], [2]]":
                        item_${a}:
                          first: "${a}"
                          second: "${b}"
                          third: "${c}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "records", "item_1", "first"), is(1));
            assertThat(getNestedValue(result, "records", "item_1", "second"), is(2));
            assertThat(getNestedValue(result, "records", "item_1", "third"), is(nullValue()));
            assertThat(getNestedValue(result, "records", "item_2", "first"), is(2));
            assertThat(getNestedValue(result, "records", "item_2", "second"), is(nullValue()));
            assertThat(getNestedValue(result, "records", "item_2", "third"), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Enumerate Integration")
    class EnumerateIntegrationTests {

        @Test
        @DisplayName("Supports !for loop with enumerate filter and tuple unpacking on lists")
        void supportsForLoopWithEnumerateFilterList() throws IOException {
            String yaml = """
                    variables:
                      items: ["alpha", "beta", "gamma"]

                    devices:
                      !for "index, item in items | enumerate":
                        "dev_${index}": "${item}"
                    """;

            Map<Object, Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "devices", "dev_0"), is("alpha"));
            assertThat(getNestedValue(data, "devices", "dev_1"), is("beta"));
            assertThat(getNestedValue(data, "devices", "dev_2"), is("gamma"));
        }

        @Test
        @DisplayName("Supports !for loop with enumerate function and tuple unpacking on lists")
        void supportsForLoopWithEnumerateFunctionList() throws IOException {
            String yaml = """
                    variables:
                      items: ["alpha", "beta", "gamma"]

                    devices:
                      !for "index, item in enumerate(items)":
                        "dev_${index}": "${item}"
                    """;

            Map<Object, Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "devices", "dev_0"), is("alpha"));
            assertThat(getNestedValue(data, "devices", "dev_1"), is("beta"));
            assertThat(getNestedValue(data, "devices", "dev_2"), is("gamma"));
        }

        @Test
        @DisplayName("Supports !for loop with enumerate filter and map entry unpacking")
        void supportsForLoopWithEnumerateFilterMap() throws IOException {
            String yaml = """
                    variables:
                      mapping:
                        a: "val1"
                        b: "val2"

                    results:
                      !for "idx, entry in mapping | enumerate":
                        "item_${idx}_${entry.key}": "${entry.value}"
                    """;

            Map<Object, Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "results", "item_0_a"), is("val1"));
            assertThat(getNestedValue(data, "results", "item_1_b"), is("val2"));
        }

        @Test
        @DisplayName("Supports !for loop with enumerate function and map entry unpacking")
        void supportsForLoopWithEnumerateFunctionMap() throws IOException {
            String yaml = """
                    variables:
                      mapping:
                        alpha: "one"
                        beta: "two"

                    results:
                      !for "idx, entry in enumerate(mapping)":
                        "item_${idx}_${entry.key}": "${entry.value}"
                    """;

            Map<Object, Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "results", "item_0_alpha"), is("one"));
            assertThat(getNestedValue(data, "results", "item_1_beta"), is("two"));
        }
    }

    @Nested
    @DisplayName("Inline Conditional Filtering")
    class ConditionalFilteringTests {

        @Test
        @DisplayName("Should filter items matching the inline 'if' clause")
        void testFilteredIteration() throws IOException {
            String yaml = """
                    variables:
                      sensor_list:
                        - { id: "s1", enabled: true }
                        - { id: "s2", enabled: false }
                        - { id: "s3", enabled: true }
                    active_sensors:
                      !for "sensor in sensor_list if sensor.enabled":
                        "${sensor.id}": "active"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "active_sensors", "s1"), is("active"));
            assertThat(getNestedValue(result, "active_sensors", "s3"), is("active"));
            assertThat(getNestedValue(result, "active_sensors", "s2"), is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore 'if' inside quotes within the expression list (before structural 'if')")
        void testIgnoresIfInsideQuotesBeforeStructuralIf() throws IOException {
            String yaml = """
                    filtered:
                      !for "item in ['if_item', 'normal_item'] if item == 'if_item'":
                        "${item}": "matched"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "filtered", "if_item"), is("matched"));
            assertThat(getNestedValue(result, "filtered", "normal_item"), is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore 'if' inside double quotes within the expression list (before structural 'if')")
        void testIgnoresIfInsideDoubleQuotesBeforeStructuralIf() throws IOException {
            String yaml = """
                    filtered:
                      !for 'item in ["if_item", "normal_item"] if item == "if_item"':
                        "${item}": "matched"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "filtered", "if_item"), is("matched"));
            assertThat(getNestedValue(result, "filtered", "normal_item"), is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore 'if' inside quotes within the condition check (after structural 'if')")
        void testIgnoresIfInsideQuotesInCondition() throws IOException {
            String yaml = """
                    variables:
                      my_list:
                        - "if status"
                        - "standard"
                    filtered:
                      !for "item in my_list if item == 'if status'":
                        "${item}": "matched"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "filtered", "if status"), is("matched"));
            assertThat(getNestedValue(result, "filtered", "standard"), is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore 'if' inside double quotes within the condition check (after structural 'if')")
        void testIgnoresIfInsideDoubleQuotesInCondition() throws IOException {
            String yaml = """
                    variables:
                      my_list:
                        - "if status"
                        - "standard"
                    filtered:
                      !for 'item in my_list if item == "if status"':
                        "${item}": "matched"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "filtered", "if status"), is("matched"));
            assertThat(getNestedValue(result, "filtered", "standard"), is(nullValue()));
        }

        @Test
        @DisplayName("Should ignore ' if ' when it appears inside a literal string within the expression itself")
        void testIgnoresSpacedIfInsideExpressionLiteral() throws IOException {
            String yaml = """
                    items:
                      !for "item in ['a if b', 'c'] if item == 'a if b'":
                        "${item}": "matched"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "items", "a if b"), is("matched"));
            assertThat(getNestedValue(result, "items", "c"), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Disambiguation Comments & Multiple Blocks")
    class DisambiguationTests {

        @Test
        @DisplayName("Should support trailing comments for YAML key disambiguation")
        void testDisambiguatingComments() throws IOException {
            String yaml = """
                    variables:
                      equipment:
                        eq1: "Pump"
                    items:
                      !for "id, label in equipment # pass 1":
                        "Item_${id}_pass1": "${label}"
                      !for "id, label in equipment # pass 2":
                        "Item_${id}_pass2": "${label}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "items", "Item_eq1_pass1"), is("Pump"));
            assertThat(getNestedValue(result, "items", "Item_eq1_pass2"), is("Pump"));
        }

        @Test
        @DisplayName("Should support trailing comments when !for expression contains hashes inside quotes/brackets")
        void testDisambiguatingCommentsWithHashesInExpression() throws IOException {
            String yaml = """
                    items:
                      !for "item in ['a#b', 'c'] # pass 1":
                        "Item_${item}": "active"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            // Verify that the hash inside the list was ignored, but the trailing comment was stripped successfully
            assertThat(getNestedValue(result, "items", "Item_a#b"), is("active"));
            assertThat(getNestedValue(result, "items", "Item_c"), is("active"));
        }

        @Test
        @DisplayName("Should support trailing comments when !for expression contains hashes inside double quotes")
        void testDisambiguatingCommentsWithDoubleQuotesHashesInExpression() throws IOException {
            // Note: Java will unescape the \\ so the raw string/YAML will see \" and not \\"
            String yaml = """
                    items:
                      !for "item in [\\"a#b\\", \\"c\\"] # pass 2":
                        "Item_${item}": "active"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "items", "Item_a#b"), is("active"));
            assertThat(getNestedValue(result, "items", "Item_c"), is("active"));
        }
    }

    @Nested
    @DisplayName("Nesting and Composition")
    class NestingTests {

        @Test
        @DisplayName("Should support nested !for loop expansion")
        void testNestedForLoop() throws IOException {
            String yaml = """
                    grid:
                      !for "x in ['a', 'b']":
                        !for "y in [1, 2]":
                          "cell_${x}_${y}": "val_${x}_${y}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "grid", "cell_a_1"), is("val_a_1"));
            assertThat(getNestedValue(result, "grid", "cell_a_2"), is("val_a_2"));
            assertThat(getNestedValue(result, "grid", "cell_b_1"), is("val_b_1"));
            assertThat(getNestedValue(result, "grid", "cell_b_2"), is("val_b_2"));
        }
    }

    @Nested
    @DisplayName("Integration and Advanced Composition Tests")
    class Integration {

        @Test
        @DisplayName("Dynamically inserts templates within a loop using interpolated names")
        void dynamicallyInsertsTemplatesInLoop() throws IOException {
            String yaml = """
                    templates:
                      template_1: "one"
                      template_2: "two"
                    result:
                      !for template in [1..2]:
                        template_${template}: !insert template_${template}
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "result", "template_1"), is("one"));
            assertThat(getNestedValue(data, "result", "template_2"), is("two"));
        }

        @Test
        @DisplayName("Loop variable is visible inside inserted templates")
        void loopVariableVisibleInInsertedTemplate() throws IOException {
            String yaml = """
                    templates:
                      foo: ${loop_var}
                    result:
                      !for loop_var in [1, 2]:
                        inserted_${loop_var}: !insert foo
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "result", "inserted_1"), is(1));
            assertThat(getNestedValue(data, "result", "inserted_2"), is(2));
        }

        @Test
        @DisplayName("Loop variable is visible inside included files")
        void loopVariableVisibleInIncludedFile() throws IOException {
            writeFixture("file1.inc.yaml", """
                    foo: ${loop_var}
                    """);

            String mainYaml = """
                    result:
                      !for loop_var in [1, 2]:
                        included_${loop_var}: !include file1.inc.yaml
                    """;

            Map<Object, @Nullable Object> data = loadYaml(mainYaml);

            assertThat(getNestedValue(data, "result", "included_1", "foo"), is(1));
            assertThat(getNestedValue(data, "result", "included_2", "foo"), is(2));
        }

        @Test
        @DisplayName("Loop variable is visible inside !if conditional directive")
        void loopVariableVisibleInIfDirective() throws IOException {
            String yaml = """
                    result:
                      !for loop_var in [1, 2]:
                        !if "loop_var > 1":
                          loop_${loop_var}: "included"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "result", "loop_1"), is(nullValue()));
            assertThat(getNestedValue(data, "result", "loop_2"), is("included"));
        }

        @Test
        @DisplayName("Evaluates !for loop directives within packages section")
        void forLoopInsidePackagesSection() throws IOException {
            String yaml = """
                    packages:
                      !for "id in [1, 2]":
                        "package_${id}":
                          items:
                            "Switch_${id}":
                              label: "Switch ${id}"
                    """;

            Map<Object, @Nullable Object> data = loadYaml(yaml);

            assertThat(getNestedValue(data, "items", "Switch_1", "label"), is("Switch 1"));
            assertThat(getNestedValue(data, "items", "Switch_2", "label"), is("Switch 2"));
        }
    }
}
