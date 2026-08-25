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
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerUniversalStructuralTagTest} contains tests for the universal structural tags
 * (!default, !freeze/!replace, !remove) in the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Universal Structural Tags Finalization Tests")
class YamlComposerUniversalStructuralTagTest extends AbstractYamlComposerTest {

    @ParameterizedTest(name = "{0}: Fallback to raw content and native types (Map, List, Primitives)")
    @ValueSource(strings = { "!default", "!freeze", "!replace" })
    @SuppressWarnings("null")
    void rawContentFallbackForAllTypes(String tag) throws IOException {
        String yaml = """
                map_context:
                  key: %s { a: b }
                list_context:
                  key: %s [ item1, item2 ]
                scalar_context:
                  str_val: %s "just a string"
                  int_val: %s 42
                  float_val: %s 3.14
                  bool_val: %s true
                """.formatted(tag, tag, tag, tag, tag, tag);

        Map<Object, @Nullable Object> data = loadYaml(yaml);

        // Verify Map preservation: should treat as standard Map
        Object mapNode = getNestedValue(data, "map_context", "key");
        assertThat(mapNode, instanceOf(Map.class));
        assertThat(((Map<?, ?>) mapNode).get("a"), is("b"));

        // Verify List preservation: should treat as standard List
        Object listNode = getNestedValue(data, "list_context", "key");
        assertThat(listNode, instanceOf(List.class));
        assertThat((List<?>) listNode, contains("item1", "item2"));

        // Verify Primitive Scalar preservation
        Object intVal = getNestedValue(data, "scalar_context", "int_val");
        assertThat("Integer value should retain Integer type", intVal, instanceOf(Integer.class));
        assertThat(intVal, is(42));

        Object floatVal = getNestedValue(data, "scalar_context", "float_val");
        assertThat("Float value should retain Double type", floatVal, instanceOf(Double.class));
        assertThat(floatVal, is(3.14));

        Object boolVal = getNestedValue(data, "scalar_context", "bool_val");
        assertThat("Boolean value should retain Boolean type", boolVal, instanceOf(Boolean.class));
        assertThat(boolVal, is(true));

        Object strVal = getNestedValue(data, "scalar_context", "str_val");
        assertThat(strVal, is("just a string"));
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

    @Test
    @DisplayName("!default: Unwraps cleanly in !var block without any !deep merge directives")
    void defaultInVarUnwrapsWithoutDeepMerge() throws IOException {
        String yaml = """
                !var test:
                  label: !default default_label

                foo: "${test.label} suffix"
                """;

        Map<Object, @Nullable Object> data = loadYaml(yaml);

        assertThat("The !default tag inside !var should unwrap into a clean string during interpolation",
                data.get("foo"), is("default_label suffix"));
    }

    @Test
    @DisplayName("!default: Overridden by !deep merge key inside !var block")
    void defaultInVarOverriddenByDeepMergeInVar() throws IOException {
        String yaml = """
                !var test:
                  label: !default default_label
                  !deep <<:
                    label: overridden_label

                foo: "${test.label} suffix"
                """;

        Map<Object, @Nullable Object> data = loadYaml(yaml);

        assertThat("The !deep merge key should override target tagged with !default inside !var", data.get("foo"),
                is("overridden_label suffix"));
    }

    @Test
    @DisplayName("!default: Unwraps cleanly without any !deep merge directives")
    void defaultUnwrapsWithoutDeepMerge() throws IOException {
        String yaml = """
                test:
                  label: !default default_label
                """;

        Map<Object, @Nullable Object> data = loadYaml(yaml);

        assertThat("The !default tag should unwrap into a clean string", getNestedValue(data, "test", "label"),
                is("default_label"));
    }
}
