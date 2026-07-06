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
package org.openhab.binding.shelly.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verifies the BASE64 encode/decode logic used for LoRa TX/RX channels in
 * {@link ShellyRelayHandler} and {@code Shelly2ApiRpc}.
 *
 * <p>
 * The encode path (CHANNEL_LORA_TXDATA) produces standard padded base64.
 * The decode paths (CHANNEL_LORA_TXDATARAW and the lora_received event) must
 * tolerate both padded and unpadded base64 by adding missing '=' chars before
 * passing the string to {@link Base64.Decoder}.
 */
@NonNullByDefault
class ShellyLoraBase64Test {

    /** Mirrors ShellyRelayHandler: text → padded base64 sent to device. */
    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Mirrors the pad-then-decode pattern in ShellyRelayHandler (CHANNEL_LORA_TXDATARAW)
     * and Shelly2ApiRpc (lora_received event).
     */
    private static String decode(String b64) {
        int rem = b64.length() % 4;
        String padded = rem == 2 ? b64 + "==" : rem == 3 ? b64 + "=" : b64;
        return new String(Base64.getDecoder().decode(padded), StandardCharsets.UTF_8);
    }

    // ── Encode ────────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "encode ''{0}'' → ''{1}''")
    @CsvSource({
            // 1-byte input → 2 padding chars in output
            "A,                 QQ==",
            // 2-byte input → 1 padding char in output
            "AB,                QUI=",
            // 3-byte input → no padding (boundary case)
            "ABC,               QUJD",
            // realistic LoRa payloads
            "Hello,             SGVsbG8=", "0123456789,        MDEyMzQ1Njc4OQ==", })
    void encode_producesStandardPaddedBase64(String text, String expected) {
        assertThat(encode(text.strip()), is(expected.strip()));
    }

    // ── Decode padded ─────────────────────────────────────────────────────────

    @ParameterizedTest(name = "decode padded ''{0}'' → ''{1}''")
    @CsvSource({ "QQ==,              A", "QUI=,              AB", "QUJD,              ABC", "SGVsbG8=,          Hello",
            // API-docs example (LoRa.SendBytes / lora_received event)
            "MDEyMzQ1Njc4OQ==,  0123456789", })
    void decode_paddedBase64_returnsOriginalText(String b64, String expected) {
        assertThat(decode(b64.strip()), is(expected.strip()));
    }

    // ── Decode unpadded ───────────────────────────────────────────────────────

    @ParameterizedTest(name = "decode unpadded ''{0}'' → ''{1}''")
    @CsvSource({
            // missing "==" (len%4 == 2)
            "QQ,                A", "MDEyMzQ1Njc4OQ,    0123456789",
            // missing "=" (len%4 == 3)
            "QUI,               AB", "SGVsbG8,           Hello",
            // no padding needed (len%4 == 0) — identical to padded case
            "QUJD,              ABC", })
    void decode_unpaddedBase64_returnsOriginalText(String b64, String expected) {
        assertThat(decode(b64.strip()), is(expected.strip()));
    }

    // ── Roundtrip ─────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "roundtrip ''{0}''")
    @ValueSource(strings = {
            // 1 / 2 / 3-byte boundaries (determines padding count)
            "x", "xy", "xyz",
            // realistic LoRa payloads
            "Hello, World!", "0123456789", "sensor:temp=22.5",
            // multi-byte UTF-8 (2-byte sequences)
            "élève", })
    void roundtrip_encodeDecodeRestoresOriginal(String text) {
        assertThat(decode(encode(text)), is(text));
    }
}
