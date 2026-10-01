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
package org.openhab.binding.miio.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Utils}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class UtilsTest {

    @Test
    public void obfuscateTokenTest() {
        String tokenString = "";
        assertEquals("", Utils.obfuscateToken(tokenString));

        tokenString = "6614";
        assertEquals("6614", Utils.obfuscateToken(tokenString));

        tokenString = "6614****";
        assertEquals("6614****", Utils.obfuscateToken(tokenString));

        tokenString = "6614798643fe781563c1";
        assertEquals("6614****************", Utils.obfuscateToken(tokenString));

        tokenString = "6614798643fe781563c1eebe";
        assertEquals("6614********************", Utils.obfuscateToken(tokenString));

        tokenString = "6614798643fe781563c1eebeda22479a";
        assertEquals("6614********************da22479a", Utils.obfuscateToken(tokenString));

        tokenString = "6614798643fe781563c1eebeda22479a6614798643fe781563c1eebeda22479a";
        assertEquals("6614********************da22479a6614798643fe781563c1eebeda22479a",
                Utils.obfuscateToken(tokenString));
    }

    @Test
    public void fromToDiD() {
        String did = "03BD3CE5";
        assertEquals("62733541", Utils.fromHEX(did));

        did = "0ABD3CE5";
        assertEquals("180174053", Utils.fromHEX(did));

        did = "62733541";
        assertEquals("03BD3CE5", Utils.toHEX(did));

        did = "cant parse";
        assertEquals("cant parse", Utils.toHEX(did));
        assertEquals("cant parse", Utils.fromHEX(did));
    }

    private static final String TOKEN = "17a8da0b48bd12902a495c8608eb8a73";

    @Test
    public void maskSecretsTest() {
        String masked = Utils.obfuscateToken(TOKEN);
        assertEquals("{\"did\":\"1\",\"token\":\"" + masked + "\",\"name\":\"Plug\"}",
                Utils.maskSecrets("{\"did\":\"1\",\"token\":\"" + TOKEN + "\",\"name\":\"Plug\"}"));
        // several members, different keys and spacing
        assertEquals("{\"a\": {\"bindkey\" : \"" + masked + "\", \"ssecurity\":\"" + masked + "\"}}",
                Utils.maskSecrets("{\"a\": {\"bindkey\" : \"" + TOKEN + "\", \"ssecurity\":\"" + TOKEN + "\"}}"));
        assertEquals("{\"serviceToken\":\"" + masked + "\"}",
                Utils.maskSecrets("{\"serviceToken\":\"" + TOKEN + "\"}"));
        // quotes escaped, as when the Json is the value of another member
        assertEquals("{\"extra\":\"{\\\"token\\\":\\\"" + masked + "\\\"}\"}",
                Utils.maskSecrets("{\"extra\":\"{\\\"token\\\":\\\"" + TOKEN + "\\\"}\"}"));
        // location
        assertEquals("{\"longitude\":\"***\",\"latitude\":\"***\",\"name\":\"Home\"}",
                Utils.maskSecrets("{\"longitude\":\"4.9123\",\"latitude\":\"52.3702\",\"name\":\"Home\"}"));
    }

    @Test
    public void maskSecretsLeavesOtherTextTest() {
        for (String text : new String[] { "", "no json at all", "token: 17a8da0b48bd12902a495c8608eb8a73",
                "{\"token\":\"\"}", "{\"token\":123,\"name\":\"x\"}", "{\"tokens\":[\"abc\"],\"mytoken\":\"abc\"}",
                "{\"result\":{\"recipes\":[{\"recipeID\":1}]}}" }) {
            assertEquals(text, Utils.maskSecrets(text));
        }
    }

    @Test
    public void truncateTest() {
        assertEquals("", Utils.truncate("", 10));
        assertEquals("0123456789", Utils.truncate("0123456789", 10));
        assertEquals("01234... [truncated, 10 characters in total]", Utils.truncate("0123456789", 5));
    }

    @Test
    public void sanitizeForLogTest() {
        // short values are logged as is
        assertEquals("{\"result\":[1,2,3]}", Utils.sanitizeForLog("{\"result\":[1,2,3]}").toString());
        assertEquals("null", Utils.sanitizeForLog(null).toString());

        // long values are limited, with secrets masked in the part that is kept
        String longText = "{\"token\":\"" + TOKEN + "\",\"data\":\"" + "x".repeat(10000) + "\"}";
        String logged = Utils.sanitizeForLog(longText).toString();
        assertTrue(logged.startsWith("{\"token\":\"" + Utils.obfuscateToken(TOKEN) + "\",\"data\":\"xxx"));
        assertFalse(logged.contains(TOKEN));
        assertTrue(logged.length() < Utils.MAX_LOG_LENGTH + 100);
        assertTrue(logged.endsWith("... [truncated, " + longText.length() + " characters in total]"));
    }
}
