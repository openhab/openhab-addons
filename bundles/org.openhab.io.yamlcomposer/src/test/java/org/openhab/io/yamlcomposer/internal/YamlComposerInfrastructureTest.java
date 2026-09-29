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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.io.yamlcomposer.internal.directives.Directive;
import org.openhab.io.yamlcomposer.internal.directives.IfDirective;
import org.openhab.io.yamlcomposer.internal.placeholders.Placeholder;

/**
 * The {@link YamlComposerInfrastructureTest} contains tests for
 * the infrastructure and utility methods of the {@link YamlComposer} class.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YamlComposerInfrastructureTest extends AbstractYamlComposerTest {
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

    @Test
    @DisplayName("Assert fails when unresolved Placeholder instance is present")
    void assertFailsOnPlaceholderInstance() {
        Placeholder mockPlaceholder = mock(Placeholder.class);
        Map<Object, @Nullable Object> data = Map.of("key", mockPlaceholder);

        assertThrows(AssertionError.class, () -> assertNoUnresolvedNodes(data));
    }

    @Test
    @DisplayName("Assert fails when unresolved Directive instance is present")
    void assertFailsOnDirectiveInstance() {
        Directive directive = new IfDirective(true, "testLocation");
        Map<Object, @Nullable Object> data = Map.of("key", directive);

        assertThrows(AssertionError.class, () -> assertNoUnresolvedNodes(data));
    }

    @Test
    @DisplayName("Assert fails when unresolved Placeholder string representation is present")
    void assertFailsOnPlaceholderString() {
        Map<Object, @Nullable Object> data = Map.of("key", "SomePlaceholder[test]");

        assertThrows(AssertionError.class, () -> assertNoUnresolvedNodes(data));
    }

    @Test
    @DisplayName("Assert fails when unresolved Directive string representation is present")
    void assertFailsOnDirectiveString() {
        Map<Object, @Nullable Object> data = Map.of("key", "SomeDirective[test]");

        assertThrows(AssertionError.class, () -> assertNoUnresolvedNodes(data));
    }
}
