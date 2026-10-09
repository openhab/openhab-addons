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
package org.openhab.binding.smartthings.internal.local;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Bounded CBOR tests for public listings and secure resource representations.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class LocalCborTest {
    @Test
    void roundTripsBatchAndTypedValues() throws IOException {
        var value = JsonParser.parseString("""
                [{"href":"/power/0","rep":{"value":true}},
                 {"href":"/temperature/current/0","rep":{"temperature":21.5,"units":"C"}},
                 {"href":"/custom/0","rep":{"text":"ümlaut","nullable":null,"range":[16,30]}}]
                """);
        assertEquals(value, LocalCbor.decode(LocalCbor.encode(value)));
        assertEquals(JsonParser.parseString("{\"value\":true}"),
                LocalCbor.decode(HexFormat.of().parseHex("bf6576616c7565f5ff")));
    }

    @Test
    void acceptsTheExactMaximumBodySize() throws IOException {
        byte[] bytes = new byte[65536];
        bytes[0] = 0x79;
        bytes[1] = (byte) 0xff;
        bytes[2] = (byte) 0xfd;
        java.util.Arrays.fill(bytes, 3, bytes.length, (byte) 'x');
        assertEquals(new JsonPrimitive("x".repeat(65533)), LocalCbor.decode(bytes));
    }

    @Test
    void rejectsEmptyTruncatedTrailingAndOversizedRepresentations() {
        for (byte[] input : new byte[][] { new byte[0], new byte[65537], { (byte) 0xa1 }, { (byte) 0xf5, (byte) 0xf4 },
                { (byte) 0xff } }) {
            assertThrows(IOException.class, () -> LocalCbor.decode(input));
        }
        assertThrows(IOException.class, () -> LocalCbor.encode(new JsonPrimitive("x".repeat(65536))));
    }

    @Test
    void rejectsExcessiveNesting() throws IOException {
        JsonArray root = new JsonArray();
        JsonArray current = root;
        for (int i = 0; i < 70; i++) {
            JsonArray next = new JsonArray();
            current.add(next);
            current = next;
        }
        assertThrows(IOException.class, () -> LocalCbor.decode(LocalCbor.encode(root)));
    }
}
