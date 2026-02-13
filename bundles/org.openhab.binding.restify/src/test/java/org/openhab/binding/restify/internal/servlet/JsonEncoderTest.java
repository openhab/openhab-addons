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
package org.openhab.binding.restify.internal.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.restify.internal.servlet.Json.BooleanValue;
import org.openhab.binding.restify.internal.servlet.Json.JsonArray;
import org.openhab.binding.restify.internal.servlet.Json.JsonObject;
import org.openhab.binding.restify.internal.servlet.Json.NullValue;
import org.openhab.binding.restify.internal.servlet.Json.NumberValue;
import org.openhab.binding.restify.internal.servlet.Json.StringValue;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
class JsonEncoderTest {
    private final JsonEncoder sut = new JsonEncoder();

    @Test
    void encodeEncodesPrimitiveJsonValues() {
        // Given
        var rootValues = new LinkedHashMap<String, Json>();
        rootValues.put("string", new StringValue("text"));
        rootValues.put("number", new NumberValue(42));
        rootValues.put("boolean", new BooleanValue(true));
        rootValues.put("null", NullValue.NULL_VALUE);
        var root = new JsonObject(rootValues);

        // When
        var actual = sut.encode(root);

        // Then
        assertThat(actual).contains("\"string\":\"text\"").contains("\"number\":42").contains("\"boolean\":true")
                .contains("\"null\":null");
    }

    @Test
    void encodeEncodesNestedObjectsAndArrays() {
        // Given
        var nestedObjectValues = new LinkedHashMap<String, Json>();
        nestedObjectValues.put("enabled", new BooleanValue(false));
        nestedObjectValues.put("name", new StringValue("bridge"));
        var nestedObject = new JsonObject(nestedObjectValues);
        var nestedArray = new JsonArray(List.of(new StringValue("a"), new NumberValue(7), NullValue.NULL_VALUE));
        var rootValues = new LinkedHashMap<String, Json>();
        rootValues.put("meta", nestedObject);
        rootValues.put("values", nestedArray);
        var root = new JsonObject(rootValues);

        // When
        var actual = sut.encode(root);

        // Then
        assertThat(actual).isEqualTo("{\"meta\":{\"enabled\":false,\"name\":\"bridge\"},\"values\":[\"a\",7,null]}");
    }

    @Test
    void encodeEscapesSpecialCharactersInStrings() {
        // Given
        var raw = "\"\\\\/\b\f\n\r\t\u0001";
        var root = new JsonObject(java.util.Map.of("escaped", new StringValue(raw)));

        // When
        var actual = sut.encode(root);

        // Then
        assertThat(actual).isEqualTo("{\"escaped\":\"\\\"\\\\\\\\/\\b\\f\\n\\r\\t\\u0001\"}");
    }

    @Test
    void encodeUsesNoTrailingCommasForObjectOrArray() {
        // Given
        var root = new JsonObject(
                java.util.Map.of("array", new JsonArray(List.of(new NumberValue(1), new NumberValue(2))), "single",
                        new JsonObject(java.util.Map.of("value", new StringValue("x")))));

        // When
        var actual = sut.encode(root);

        // Then
        assertThat(actual).doesNotContain(",}").doesNotContain(",]");
    }

    @Test
    void encodeHandlesEmptyObjectAndEmptyArray() {
        // Given
        var rootValues = new LinkedHashMap<String, Json>();
        rootValues.put("emptyObject", new JsonObject(java.util.Map.of()));
        rootValues.put("emptyArray", new JsonArray(List.of()));
        var root = new JsonObject(rootValues);

        // When
        var actual = sut.encode(root);

        // Then
        assertThat(actual).isEqualTo("{\"emptyObject\":{},\"emptyArray\":[]}");
    }
}
