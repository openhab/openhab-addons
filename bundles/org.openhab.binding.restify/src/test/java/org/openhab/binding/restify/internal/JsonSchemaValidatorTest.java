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
package org.openhab.binding.restify.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
class JsonSchemaValidatorTest {
    private static final String ENDPOINT_SCHEMA_ID = "https://www.openhab.org/addons/RESTify/endpoint.schema.json";

    private final JsonSchemaValidator sut = new JsonSchemaValidator();

    @BeforeEach
    void seedSchemaCache() throws Exception {
        var field = JsonSchemaValidator.class.getDeclaredField("schemaTextCache");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        var cache = (Map<String, String>) field.get(sut);
        cache.put(ENDPOINT_SCHEMA_ID, readSchemaText());
    }

    @Test
    void validateEndpointConfigReturnsNoErrorsForMinimalValidPayload() {
        // Given
        var config = "{\"response\":{}}";

        // When
        var errors = sut.validateEndpointConfig(config);

        // Then
        assertThat(errors).isEmpty();
    }

    @Test
    void validateEndpointConfigReturnsNoErrorsForValidAuthorizationPayload() {
        // Given
        var config = "{\"authorization\":{\"type\":\"Basic\",\"username\":\"john\",\"password\":\"secret\"},\"response\":{}}";

        // When
        var errors = sut.validateEndpointConfig(config);

        // Then
        assertThat(errors).isEmpty();
    }

    @Test
    void validateEndpointConfigReturnsErrorsWhenRequiredResponseIsMissing() {
        // Given
        var config = "{\"authorization\":{\"type\":\"Bearer\",\"token\":\"abc\"}}";

        // When
        var errors = sut.validateEndpointConfig(config);

        // Then
        assertThat(errors).isNotEmpty();
    }

    @Test
    void validateEndpointConfigReturnsErrorsForUnexpectedTopLevelProperty() {
        // Given
        var config = "{\"response\":{},\"unexpected\":true}";

        // When
        var errors = sut.validateEndpointConfig(config);

        // Then
        assertThat(errors).isNotEmpty();
    }

    @Test
    void validateEndpointConfigHandlesInvalidJsonInput() {
        // Given
        var malformed = "{";

        // When / Then
        assertThatThrownBy(() -> sut.validateEndpointConfig(malformed)).isInstanceOf(RuntimeException.class);
    }

    private String readSchemaText() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("schema/endpoint.schema.json")) {
            if (input == null) {
                throw new IllegalStateException("Missing schema resource: schema/endpoint.schema.json");
            }
            return new String(input.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read schema resource", e);
        }
    }
}
