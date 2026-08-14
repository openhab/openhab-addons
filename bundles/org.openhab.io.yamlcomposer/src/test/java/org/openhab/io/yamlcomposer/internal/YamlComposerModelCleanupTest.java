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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link YamlComposerModelCleanupTest} contains tests for the model cleanup functionality in the
 * {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YamlComposerModelCleanupTest extends AbstractYamlComposerTest {
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
