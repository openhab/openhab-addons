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
package org.openhab.binding.candy.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Independent known-answer and malformed-input coverage for the local codec.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class CandyCodecTest {
    private static final String KEY = "abcdefghijklmnop";

    @Test
    void knownAnswer() throws CandyProtocolException {
        assertEquals("36100A10005B564E3A1E3818505E", CandyCodec.encode("Write=1&StSt=0", KEY));
        assertEquals("Write=1&StSt=0", CandyCodec.decode(" \r\n36100a10005b564e3a1e3818505e\n", KEY));
        // Seventeen 'a' bytes, XORed with a sixteen-byte key, exercise key wraparound independently.
        assertEquals("0003020504070609080B0A0D0C0F0E1100", CandyCodec.encode("a".repeat(17), KEY));
    }

    @Test
    void rejectsMalformedAndOversizedResponses() {
        for (String wire : List.of("", "  ", "1", "GG", "00 00", "00\n00", "00".repeat(32769))) {
            assertThrows(CandyProtocolException.class, () -> CandyCodec.decode(wire, KEY));
        }
        assertThrows(CandyProtocolException.class, () -> CandyCodec.encode("x".repeat(32769), KEY));
        assertThrows(CandyProtocolException.class, () -> CandyCodec.decode("FF", KEY));
    }

    @Test
    void rejectsUnsupportedKeysWithoutExposingThem() {
        String message = ResourceBundle.getBundle("OH-INF.i18n.candy", Locale.getDefault())
                .getString("protocol-error-key");
        for (String key : List.of("", "short", "12345678901234567", "abcdefghijklmnóq", "abcdefghijklmno\n")) {
            CandyProtocolException exception = assertThrows(CandyProtocolException.class,
                    () -> CandyCodec.encode("status", key));
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }
    }
}
