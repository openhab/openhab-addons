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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.Collections;
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
 * The {@link YamlComposerYamlParsingTest} contains tests for the YAML parsing functionality in the {@link YamlComposer}
 * class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YamlComposerYamlParsingTest extends AbstractYamlComposerTest {
    @Test
    @DisplayName("Allows null values")
    void allowsNullValues() throws IOException {
        assertThat(loadYamlValue(""), is(nullValue()));
        assertThat(loadYamlValue("# Comment"), is(nullValue()));
        assertThat(loadYamlValue("null"), is(nullValue()));
        assertThat(loadYamlValue("a: null"), equalTo(Collections.singletonMap("a", null)));
        assertThat(loadYamlValue("null: null"), equalTo(Collections.emptyMap()));
        assertThat(loadYamlValue("- null"), equalTo(Collections.emptyList()));
    }

    @Test
    @DisplayName("Parses true and false as boolean")
    void parsesTrueAndFalseAsBoolean() throws IOException {
        assertThat(loadYamlValue("true"), equalTo(true));
        assertThat(loadYamlValue("True"), equalTo(true));
        assertThat(loadYamlValue("TRUE"), equalTo(true));

        assertThat(loadYamlValue("false"), equalTo(false));
        assertThat(loadYamlValue("False"), equalTo(false));
        assertThat(loadYamlValue("FALSE"), equalTo(false));
    }

    @Test
    @DisplayName("Treats boolean-like strings as regular strings")
    void treatsBooleanLikeStringsAsRegularStrings() throws IOException {
        for (String value : List.of("on", "On", "ON", "oN")) {
            assertThat(loadYamlValue(value), equalTo(value));
        }

        for (String value : List.of("off", "Off", "OFF", "oFf")) {
            assertThat(loadYamlValue(value), equalTo(value));
        }

        for (String value : List.of("yes", "Yes", "YES", "yEs")) {
            assertThat(loadYamlValue(value), equalTo(value));
        }

        for (String value : List.of("no", "No", "NO", "nO")) {
            assertThat(loadYamlValue(value), equalTo(value));
        }
    }

    @Test
    @DisplayName("Supports anchors and aliases")
    void supportsAnchorsAndAliases() throws IOException {
        @SuppressWarnings("unchecked")
        Map<Object, Object> data = (Map<Object, Object>) Objects.requireNonNull(loadYamlValue("""
                foo: &name bar
                baz: *name
                ? *name
                : qux
                """));
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
