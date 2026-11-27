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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

/**
 * Tests for the JSON (de-)serialization of {@link GeminiLLMToolCall}, especially the
 * {@code parallel} marker used to keep parallel function calls of one model turn together
 * across separately stored TOOL_CALL conversation messages.
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
        assertNull(deserialized.parallel);
    }

    @Test
    public void parallelMarkerRoundTrips() {
        // calls 2..N of a parallel batch are stored with parallel=true and typically without a
        // thought signature (Gemini only signs the first part of the batch)
        GeminiLLMToolCall call = new GeminiLLMToolCall("item-get-state", Map.of("item", "Lamp2"), "call-2", null,
                Boolean.TRUE);

        GeminiLLMToolCall deserialized = GeminiLLMToolCall.fromJson(call.toJson());

        assertEquals(Boolean.TRUE, deserialized.parallel);
        assertNull(deserialized.thoughtSignature);
        assertEquals("call-2", deserialized.id);
    }

    @Test
    public void absentParallelMarkerIsOmittedFromJson() {
        // single calls and the first call of a batch must keep the original message format
        GeminiLLMToolCall call = new GeminiLLMToolCall("item-send-command", Map.of("item", "Lamp", "command", "ON"),
                null, null);

        assertFalse(call.toJson().contains("parallel"));
    }

    @Test
    public void fromJsonAcceptsLegacyFormatWithoutMarker() {
        // conversations stored by earlier versions know neither id, thoughtSignature nor parallel
        GeminiLLMToolCall deserialized = GeminiLLMToolCall
                .fromJson("{\"tool\":\"item-get-state\",\"params\":{\"item\":\"Lamp\"}}");

        assertEquals("item-get-state", deserialized.tool);
        assertEquals(Map.of("item", "Lamp"), deserialized.params);
        assertNull(deserialized.id);
        assertNull(deserialized.thoughtSignature);
        assertNull(deserialized.parallel);
    }

    @Test
    public void fromJsonRejectsInvalidInput() {
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.fromJson("null"));
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.fromJson("{}"));
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.fromJson("{\"tool\":\"item-get-state\"}"));
        assertThrows(JsonSyntaxException.class, () -> GeminiLLMToolCall.fromJson("{"));
    }
}
