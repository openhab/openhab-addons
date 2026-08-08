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
package org.openhab.binding.gemini.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

/**
 * Tests for the JSON (de-)serialization helpers of {@link GeminiLLMToolCall}, especially the
 * batch format used to store parallel function calls in a single conversation message.
 *
 * @author Christian Heldt - Initial contribution
 */
@NonNullByDefault
public class GeminiLLMToolCallTest {

    @Test
    public void singleCallRoundTripPreservesAllFields() {
        GeminiLLMToolCall call = new GeminiLLMToolCall("item-get-state", Map.of("item", "LivingRoom_Lamp"), "call-1",
                "signature-abc");

        GeminiLLMToolCall deserialized = GeminiLLMToolCall.fromJson(call.toJson());

        assertEquals("item-get-state", deserialized.tool);
        assertEquals(Map.of("item", "LivingRoom_Lamp"), deserialized.params);
        assertEquals("call-1", deserialized.id);
        assertEquals("signature-abc", deserialized.thoughtSignature);
    }

    @Test
    public void listFromJsonAcceptsLegacySingleObjectFormat() {
        GeminiLLMToolCall call = new GeminiLLMToolCall("item-send-command", Map.of("item", "Lamp", "command", "ON"),
                null, null);

        List<GeminiLLMToolCall> deserialized = GeminiLLMToolCall.listFromJson(call.toJson());

        assertEquals(1, deserialized.size());
        assertEquals("item-send-command", deserialized.getFirst().tool);
        assertEquals(Map.of("item", "Lamp", "command", "ON"), deserialized.getFirst().params);
        assertNull(deserialized.getFirst().id);
        assertNull(deserialized.getFirst().thoughtSignature);
    }

    @Test
    public void batchRoundTripPreservesOrderAndFields() {
        // Gemini sends the thought signature only on the first part of a parallel call batch
        List<GeminiLLMToolCall> calls = List.of(
                new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp1"), "call-1", "signature-abc"),
                new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp2"), "call-2", null));

        List<GeminiLLMToolCall> deserialized = GeminiLLMToolCall.listFromJson(GeminiLLMToolCall.toJsonList(calls));

        assertEquals(2, deserialized.size());
        assertEquals("item-get-state", deserialized.get(0).tool);
        assertEquals(Map.of("item", "Lamp1"), deserialized.get(0).params);
        assertEquals("call-1", deserialized.get(0).id);
        assertEquals("signature-abc", deserialized.get(0).thoughtSignature);
        assertEquals("item-get-state", deserialized.get(1).tool);
        assertEquals(Map.of("item", "Lamp2"), deserialized.get(1).params);
        assertEquals("call-2", deserialized.get(1).id);
        assertNull(deserialized.get(1).thoughtSignature);
    }

    @Test
    public void listFromJsonAcceptsLeadingWhitespace() {
        String json = " \n [{\"tool\":\"get-date-time\",\"params\":{}}]";

        List<GeminiLLMToolCall> deserialized = GeminiLLMToolCall.listFromJson(json);

        assertEquals(1, deserialized.size());
        assertEquals("get-date-time", deserialized.getFirst().tool);
    }

    @Test
    public void listFromJsonReturnsEmptyListForEmptyArray() {
        assertTrue(GeminiLLMToolCall.listFromJson("[]").isEmpty());
    }

    @Test
    public void listFromJsonRejectsInvalidInput() {
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.listFromJson("null"));
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.listFromJson("[null]"));
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.listFromJson("[{}]"));
        assertThrows(JsonSyntaxException.class,
                () -> GeminiLLMToolCall.listFromJson("[{\"tool\":\"item-get-state\"}]"));
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.listFromJson("[{"));
    }

    @Test
    public void resultsRoundTripPreservesOrder() {
        List<String> results = List.of("Lamp1 is ON", "Lamp2 is OFF");

        assertEquals(results, GeminiLLMToolCall.resultsFromJson(GeminiLLMToolCall.resultsToJson(results)));
    }

    @Test
    public void resultsFromJsonRejectsInvalidInput() {
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.resultsFromJson("null"));
        // a plain (non-array) tool result must not parse, so the caller can fall back to treating
        // it as a single result
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.resultsFromJson("plain text result"));
    }
}
