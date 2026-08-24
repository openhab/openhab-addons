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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerUniversalStructuralTagTest} contains tests for the universal structural tags (!replace,
 * !remove) in the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Universal Structural Tags (Outside Package Context)")
class YamlComposerUniversalStructuralTagTest extends AbstractYamlComposerTest {

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
