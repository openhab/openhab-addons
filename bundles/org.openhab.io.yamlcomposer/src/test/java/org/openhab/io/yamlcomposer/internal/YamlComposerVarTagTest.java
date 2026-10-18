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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.io.yamlcomposer.internal.core.DirectiveProcessor;
import org.openhab.io.yamlcomposer.internal.directives.VarDirective;
import org.openhab.io.yamlcomposer.internal.processors.VarProcessor;

/**
 * Tests for {@link VarProcessor} and {@link VarDirective} handling in {@link DirectiveProcessor}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class YamlComposerVarTagTest extends AbstractYamlComposerTest {

    @Test
    @DisplayName("Verify scalar !var declaration populates scope and leaves no output keys")
    void testScalarVarDeclaration() throws IOException {
        String yaml = """
                !var prefix: "sensor_1"
                sensor_id: "${prefix}_temp"
                sensor_status: "${prefix}_active"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals(2, result.size());
        assertEquals("sensor_1_temp", result.get("sensor_id"));
        assertEquals("sensor_1_active", result.get("sensor_status"));
        assertNull(result.get("prefix"), "!var directives should not leak into output keys");
    }

    @Test
    @DisplayName("Verify !var declaration with block mapping value populates scope")
    void testMappingVarDeclaration() throws IOException {
        String yaml = """
                !var mymap:
                  foo: bar
                  nested:
                    key: value

                map_ref: "${mymap.foo}"
                nested_map_ref: "${mymap.nested.key}"
                full_map: "${mymap}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals("bar", result.get("map_ref"));
        assertEquals("value", result.get("nested_map_ref"));

        @SuppressWarnings("unchecked")
        Map<String, Object> fullMap = (Map<String, Object>) result.get("full_map");
        assertNotNull(fullMap);
        assertEquals("bar", fullMap.get("foo"));
    }

    @Test
    @DisplayName("Verify !var declaration with block list value populates scope")
    void testListVarDeclaration() throws IOException {
        String yaml = """
                !var mylist:
                  - one
                  - two

                list_ref: "${mylist[0]}"
                full_list: "${mylist}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals("one", result.get("list_ref"));

        @SuppressWarnings("unchecked")
        List<Object> fullList = (List<Object>) result.get("full_list");
        assertNotNull(fullList);
        assertEquals(List.of("one", "two"), fullList);
    }

    @Test
    @DisplayName("Verify sequential key-form !var directives can reference previously declared variables")
    void testSequentialKeyFormVariableResolution() throws IOException {
        String yaml = """
                !var host: "localhost"
                !var port: 8080
                !var base_url: "http://${host}:${port}"
                !var api_url: "${base_url}/v1"

                endpoint: "${api_url}/users"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals(1, result.size());
        assertEquals("http://localhost:8080/v1/users", result.get("endpoint"));
    }

    @Test
    @DisplayName("Verify self-referential !var declaration fails gracefully and tracks unresolved variable warning")
    void testSelfReferentialVarDeclaration() throws IOException {
        String yaml = """
                !var count: "${count}"
                result: "${count}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertNull(result.get("result"));
        assertThat(logSession.getTrackedWarnings(), hasItem(containsString("count")));
    }

    @Test
    @DisplayName("Verify self-referential !var re-assignment evaluates expression against current scope before updating")
    void testSelfReferentialVarWithExistingVariable() throws IOException {
        String yaml = """
                !var count: 10
                !var count: "${count + 1}"
                result: "${count}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals(1, result.size());
        assertEquals(11, result.get("result"));
    }

    @Test
    @DisplayName("Verify variable shadowing and sequential re-assignment in local map traversal")
    void testVariableShadowingAndSequentialReassignment() throws IOException {
        String yaml = """
                !var mode: "dev"
                env_first: "${mode}"
                !var mode: "prod"
                env_second: "${mode}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals(2, result.size());
        assertEquals("dev", result.get("env_first"));
        assertEquals("prod", result.get("env_second"));
    }

    @Test
    @DisplayName("Verify local scope shadowing does not mutate higher-level scope after child block exits")
    void testLocalScopeShadowingDoesNotMutateParentScope() throws IOException {
        String yaml = """
                !var env: "production"

                before_child: "${env}"

                child_service:
                  !var env: "staging"
                  child_env: "${env}"

                after_child: "${env}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals("production", result.get("before_child"));
        assertThat(getNestedValue(result, "child_service", "child_env"), equalTo("staging"));
        assertEquals("production", result.get("after_child"));
    }

    @Test
    @DisplayName("Verify local !var shadowing of global 'variables:' section leaves global scope unaffected after child block")
    void testLocalVarShadowingGlobalVariablesBlock() throws IOException {
        String yaml = """
                variables:
                  mode: "global_mode"

                before_child: "${mode}"

                child_block:
                  !var mode: "local_mode"
                  child_val: "${mode}"

                after_child: "${mode}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals("global_mode", result.get("before_child"));
        assertThat(getNestedValue(result, "child_block", "child_val"), equalTo("local_mode"));
        assertEquals("global_mode", result.get("after_child"));
    }

    @Test
    @DisplayName("Verify special system variables cannot be overridden by !var declarations")
    void testSpecialVariablesCannotBeRedefined() throws IOException {
        String yaml = """
                !var __FILE_NAME__: "hacked_name"
                !var OPENHAB_CONF: "/tmp/fake_conf"
                file_check: "${__FILE_NAME__}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertNotEquals("hacked_name", result.get("file_check"));
        assertThat(logSession.getTrackedWarnings(), hasItem(containsString("Cannot redefine special variable")));
    }

    @Test
    @DisplayName("Verify lone !var directive in list item evaluates to an empty map")
    void testLoneVarInListItemEvaluatesToEmptyMap() throws IOException {
        String yaml = """
                items:
                  - "item_1"
                  - !var prefix: "isolated"
                  - "item_2"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) result.get("items");

        assertNotNull(items);
        assertEquals(3, items.size());
        assertEquals("item_1", items.get(0));

        // The second item is syntactically a map with only !var, so it evaluates to {}
        assertThat(items.get(1), instanceOf(Map.class));
        assertTrue(((Map<?, ?>) items.get(1)).isEmpty());

        assertEquals("item_2", items.get(2));
    }

    @Test
    @DisplayName("Verify lone !var directive in map context evaluates to an empty map")
    void testLoneVarInMapEvaluatesToEmptyMap() throws IOException {
        String yaml = """
                sub_map:
                  !var inner: "registered"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        @SuppressWarnings("unchecked")
        Map<String, Object> subMap = (Map<String, Object>) result.get("sub_map");
        assertNotNull(subMap);
        assertTrue(subMap.isEmpty(), "A map containing only !var directives should resolve to an empty map");
    }

    @Test
    @DisplayName("Verify sequential !var directives, forward-reference warnings, and retention")
    void testMultipleVarDirectivesAndForwardReferences() throws IOException {
        String yaml = """
                !var foo: "bar"
                foo: "${foo}"
                qux: "${qux}"
                !var qux: "quux"

                after_foo: "${foo}"
                after_qux: "${qux}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertEquals("bar", result.get("foo"));
        assertEquals("bar", result.get("after_foo"));
        assertEquals("quux", result.get("after_qux"));

        assertNotEquals("quux", result.get("qux"));
        assertThat(logSession.getTrackedWarnings(), hasItem(containsString("qux")));
    }

    @Test
    @DisplayName("Verify local !var directives do not leak to parent or sibling scopes")
    void testVarDirectiveScopeIsolation() throws IOException {
        String yaml = """
                parent_before: "${child_var}"

                child_block:
                  !var child_var: "inner_secret"
                  child_val: "${child_var}"

                sibling_block:
                  sibling_val: "${child_var}"

                parent_after: "${child_var}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);

        assertThat(getNestedValue(result, "child_block", "child_val"), equalTo("inner_secret"));

        assertNotEquals("inner_secret", result.get("parent_before"));
        assertNotEquals("inner_secret", result.get("parent_after"));
        assertNotEquals("inner_secret", getNestedValue(result, "sibling_block", "sibling_val"));

        assertThat(logSession.getTrackedWarnings(), hasItem(containsString("child_var")));
    }

    @Test
    @DisplayName("Verify !var as the first entry of a map item inside a list populates local map scope")
    void testVarAsFirstEntryInMapInsideList() throws IOException {
        String yaml = """
                items:
                  - "item_1"
                  - !var name: "value"
                    foo: "bar"
                    result: "${name}"
                """;

        Map<Object, @Nullable Object> result = loadYaml(yaml);
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) result.get("items");

        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("item_1", items.get(0));

        @SuppressWarnings("unchecked")
        Map<String, Object> mapItem = (Map<String, Object>) items.get(1);
        assertEquals("bar", mapItem.get("foo"));
        assertEquals("value", mapItem.get("result"));
        assertNull(mapItem.get("name"), "!var directive entry should be consumed and omitted from output map");
    }

    @Nested
    @DisplayName("Control Flow & Chain Interactions")
    class ControlFlowInteractions {

        @Test
        @DisplayName("Verify !var inside list control map breaks active if-chain for subsequent !else")
        void testVarInListBreaksIfChain() throws IOException {
            String yaml = """
                    items:
                      - !if "true":
                          - "first"
                        !var foo: "bar"
                        !else ~:
                          - "second"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) result.get("items");

            assertNotNull(items);
            assertTrue(items.contains("first"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("!else without preceding !if")));
        }

        @Test
        @DisplayName("Verify !var in map context breaks active if-chain for subsequent !else")
        void testVarInMapBreaksIfChain() throws IOException {
            String yaml = """
                    !if "true":
                      first: "val1"
                    !var foo: "bar"
                    !else ~:
                      second: "val2"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertEquals("val1", result.get("first"));
            assertNull(result.get("second"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("!else without preceding !if")));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class Integration {

        @Test
        @DisplayName("Verify !var inside a !for loop with mapping block unrolls map items into parent list")
        void testVarInsideForLoopWithMapBlockInListContext() throws IOException {
            String yaml = """
                    items:
                      - !for i in [1, 2]:
                          !var item_prefix: "prefix_${i}"
                          "node_${i}": "${item_prefix}_val"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) result.get("items");

            assertNotNull(items);
            assertEquals(2, items.size());

            @SuppressWarnings("unchecked")
            Map<String, Object> item1 = (Map<String, Object>) items.get(0);
            assertEquals("prefix_1_val", item1.get("node_1"));

            @SuppressWarnings("unchecked")
            Map<String, Object> item2 = (Map<String, Object>) items.get(1);
            assertEquals("prefix_2_val", item2.get("node_2"));
        }

        @Test
        @DisplayName("Verify !var inside a !for loop with sequence block unrolls sequence items into parent list")
        void testVarInsideForLoopWithSequenceBlockInListContext() throws IOException {
            String yaml = """
                    items:
                      - !for i in [1, 2]:
                          - !var item_prefix: "prefix_${i}"
                            name: "${item_prefix}_node"
                            val: "${i}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);
            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) result.get("items");

            assertNotNull(items);
            assertEquals(2, items.size());

            @SuppressWarnings("unchecked")
            Map<String, Object> item1 = (Map<String, Object>) items.get(0);
            assertEquals("prefix_1_node", item1.get("name"));

            @SuppressWarnings("unchecked")
            Map<String, Object> item2 = (Map<String, Object>) items.get(1);
            assertEquals("prefix_2_node", item2.get("name"));
        }

        @Test
        @DisplayName("Verify !var declared inside an included file does not leak to calling file scope")
        void testVarInIncludedFileDoesNotLeakToCaller() throws IOException {
            writeFixture("child.yaml", """
                    !var child_secret: "from_include"
                    child_prop: "${child_secret}"
                    """);

            Path mainPath = writeFixture("main.yaml", """
                    imported: !include "child.yaml"
                    parent_prop: "${child_secret}"
                    """);

            Map<Object, @Nullable Object> result = loadFixture(mainPath);

            assertThat(getNestedValue(result, "imported", "child_prop"), equalTo("from_include"));
            assertNotEquals("from_include", result.get("parent_prop"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("child_secret")));
        }

        @Test
        @DisplayName("Verify !var declared inside template definition does not leak to caller scope on !insert")
        void testVarInTemplateDoesNotLeakToCaller() throws IOException {
            String yaml = """
                    templates:
                      component:
                        !var internal_id: "tpl_123"
                        id: "${internal_id}"
                        name: "${component_name}"

                    component_instance: !insert
                      template: component
                      vars:
                        component_name: "sensor_main"

                    outer_id: "${internal_id}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "component_instance", "id"), equalTo("tpl_123"));
            assertThat(getNestedValue(result, "component_instance", "name"), equalTo("sensor_main"));

            assertNotEquals("tpl_123", result.get("outer_id"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("internal_id")));
        }

        @Test
        @DisplayName("Verify !var declared inside a package does not leak to root composition scope")
        void testVarInsidePackageDoesNotLeak() throws IOException {
            String yaml = """
                    packages:
                      !var pkg_global: "package_global"
                      sensor_pkg:
                        !var pkg_internal: "package_internal"
                        items:
                          pkg_global: ${pkg_global}
                          pkg_internal: ${pkg_internal}

                    pkg_global_check: "${pkg_global}"
                    pkg_internal_check: "${pkg_internal}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "items", "pkg_global"), equalTo("package_global"));
            assertThat(getNestedValue(result, "items", "pkg_internal"), equalTo("package_internal"));

            assertNotEquals("package_global", result.get("pkg_global_check"));
            assertNotEquals("package_internal", result.get("pkg_internal_check"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("pkg_internal")));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("pkg_global")));
        }

        @Test
        @DisplayName("Verify !var inside a !for loop body resolves per iteration and does not leak outside loop")
        void testVarInsideForLoopDoesNotLeak() throws IOException {
            String yaml = """
                    !var loop_id: "outer_scope"
                    nodes:
                      !for i in [1, 2]:
                        !var loop_id: "id_${i}"
                        "node_${i}": "${loop_id}"

                      outer_check: "${loop_id}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "nodes", "node_1"), equalTo("id_1"));
            assertThat(getNestedValue(result, "nodes", "node_2"), equalTo("id_2"));

            assertThat(getNestedValue(result, "nodes", "outer_check"), equalTo("outer_scope"));
        }

        @Test
        @DisplayName("Verify !var declared inside an !if block does not leak to outer map scope")
        void testVarInsideIfBlockDoesNotLeak() throws IOException {
            String yaml = """
                    !var condition: true

                    feature_block:
                      !if "${condition}":
                        !var branch_secret: "active_feature"
                        status: "${branch_secret}"

                      outer_check: "${branch_secret}"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "feature_block", "status"), equalTo("active_feature"));

            assertNotEquals("active_feature", result.get("outer_check"));
            assertThat(logSession.getTrackedWarnings(), hasItem(containsString("branch_secret")));
        }

        @Test
        @DisplayName("Verify parent !var is inherited downstream by child !include and !insert template")
        void testParentVarInheritedByChildAndTemplate() throws IOException {
            writeFixture("child.yaml", """
                    child_prop: "${prefix}_child"
                    """);

            Path mainPath = writeFixture("main.yaml", """
                    !var prefix: global_scope

                    templates:
                      tpl:
                        tpl_prop: "${prefix}_tpl"

                    imported:
                      !var prefix: include_scope
                      foo: !include "child.yaml"
                    inserted:
                      !var prefix: insert_scope
                      foo: !insert
                        template: tpl
                    """);

            Map<Object, @Nullable Object> result = loadFixture(mainPath);

            assertThat(getNestedValue(result, "imported", "foo", "child_prop"), equalTo("include_scope_child"));
            assertThat(getNestedValue(result, "inserted", "foo", "tpl_prop"), equalTo("insert_scope_tpl"));
        }

        @Test
        @DisplayName("Verify !var inside included file overwrites 'vars' argument passed via !include")
        void testVarInIncludeOverwritesIncludeVarsArgument() throws IOException {
            writeFixture("child.yaml", """
                    !var arg_var: "overridden_by_var"
                    result: "${arg_var}"
                    """);

            Path mainPath = writeFixture("main.yaml", """
                    imported: !include
                      file: "child.yaml"
                      vars:
                        arg_var: "passed_via_vars"
                    """);

            Map<Object, @Nullable Object> result = loadFixture(mainPath);

            assertThat(getNestedValue(result, "imported", "result"), equalTo("overridden_by_var"));
        }

        @Test
        @DisplayName("Verify !var inside template overwrites 'vars' argument passed via !insert")
        void testVarInTemplateOverwritesInsertVarsArgument() throws IOException {
            String yaml = """
                    templates:
                      component:
                        !var arg_var: "overridden_by_var"
                        result: "${arg_var}"

                    component_instance: !insert
                      template: component
                      vars:
                        arg_var: "passed_via_vars"
                    """;

            Map<Object, @Nullable Object> result = loadYaml(yaml);

            assertThat(getNestedValue(result, "component_instance", "result"), equalTo("overridden_by_var"));
        }
    }
}
