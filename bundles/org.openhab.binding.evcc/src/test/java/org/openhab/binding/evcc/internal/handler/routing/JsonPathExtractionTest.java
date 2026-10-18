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
package org.openhab.binding.evcc.internal.handler.routing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Tests for {@link JsonPathExtraction} strategy.
 *
 * @author Marcel Goerentz - Initial contribution
 */
class JsonPathExtractionTest {

    @Test
    void testSimplePropertyExtraction() {
        String json = "{\"name\": \"test\", \"value\": 42}";
        JsonElement source = JsonParser.parseString(json);

        JsonPathExtraction extraction = new JsonPathExtraction("$.name");
        JsonElement result = extraction.extract(source);

        assertNotNull(result);
        assertEquals("test", result.getAsString());
    }

    @Test
    void testArrayIndexExtraction() {
        String json = "{\"items\": [{\"id\": 1, \"name\": \"first\"}, {\"id\": 2, \"name\": \"second\"}]}";
        JsonElement source = JsonParser.parseString(json);

        JsonPathExtraction extraction = new JsonPathExtraction("$.items[0]");
        JsonElement result = extraction.extract(source);

        assertNotNull(result);
        assertTrue(result.isJsonObject());
        assertEquals(1, result.getAsJsonObject().get("id").getAsInt());
    }

    @Test
    void testNestedPropertyExtraction() {
        String json = "{\"outer\": {\"inner\": {\"value\": \"deep\"}}}";
        JsonElement source = JsonParser.parseString(json);

        JsonPathExtraction extraction = new JsonPathExtraction("$.outer.inner.value");
        JsonElement result = extraction.extract(source);

        assertNotNull(result);
        assertEquals("deep", result.getAsString());
    }

    @Test
    void testNonexistentPathReturnsNull() {
        String json = "{\"name\": \"test\"}";
        JsonElement source = JsonParser.parseString(json);

        JsonPathExtraction extraction = new JsonPathExtraction("$.nonexistent");
        JsonElement result = extraction.extract(source);

        assertNull(result);
    }

    @Test
    void testInvalidPathReturnsNull() {
        String json = "{\"name\": \"test\"}";
        JsonElement source = JsonParser.parseString(json);

        JsonPathExtraction extraction = new JsonPathExtraction("$.invalid[abc]");
        JsonElement result = extraction.extract(source);

        assertNull(result);
    }

    @Test
    void testArrayFilterExtractionNotSupported() {
        // Note: Array filters are not supported by the lightweight implementation
        // This test verifies graceful handling (returns null) for unsupported patterns
        String json = "{\"vehicles\": [{\"id\": \"car1\", \"soc\": 50}, {\"id\": \"car2\", \"soc\": 75}]}";
        JsonElement source = JsonParser.parseString(json);

        JsonPathExtraction extraction = new JsonPathExtraction("$.vehicles[?(@.id=='car1')]");
        JsonElement result = extraction.extract(source);

        // Unsupported patterns should gracefully return null
        assertNull(result);
    }

    @Test
    void testNullSourceReturnsNull() {
        JsonPathExtraction extraction = new JsonPathExtraction("$.name");
        JsonElement result = extraction.extract(JsonParser.parseString("null"));

        assertNull(result);
    }
}
