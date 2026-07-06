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
import static org.openhab.binding.shelly.internal.util.ShellyUtils.fixBase64Padding;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
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
     * and Shelly2ApiRpc (lora_received event), now delegating padding to ShellyUtils.
     */
    private static String decode(String b64) {
        return new String(Base64.getDecoder().decode(fixBase64Padding(b64)), StandardCharsets.UTF_8);
    }

    @ParameterizedTest(name = "fixBase64Padding(''{0}'') = ''{1}''")
    @CsvSource({
            // len%4==0 — no padding needed
            "QUJD,          QUJD",
            // len%4==2 — needs "=="
            "QQ,            QQ==",
            // len%4==3 — needs "="
            "QUI,           QUI=",
            // already padded — unchanged
            "QQ==,          QQ==", "QUI=,          QUI=", })
    void fixBase64PaddingAddsCorrectPadding(String input, String expected) {
        assertThat(fixBase64Padding(input.strip()), is(expected.strip()));
    }

    @Test
    void fixBase64PaddingRem1LeavesStringUnchanged() {
        // len%4==1 is structurally invalid Base64; fixBase64Padding must not crash —
        // the caller's try/catch handles the downstream decode failure.
        String invalid = "Q"; // length 1, rem==1
        assertThat(fixBase64Padding(invalid), is(invalid));
    }

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
    void encodeProducesStandardPaddedBase64(String text, String expected) {
        assertThat(encode(text.strip()), is(expected.strip()));
    }

    @ParameterizedTest(name = "decode padded ''{0}'' → ''{1}''")
    @CsvSource({ "QQ==,              A", "QUI=,              AB", "QUJD,              ABC", "SGVsbG8=,          Hello",
            // API-docs example (LoRa.SendBytes / lora_received event)
            "MDEyMzQ1Njc4OQ==,  0123456789", })
    void decodePaddedBase64ReturnsOriginalText(String b64, String expected) {
        assertThat(decode(b64.strip()), is(expected.strip()));
    }

    @ParameterizedTest(name = "decode unpadded ''{0}'' → ''{1}''")
    @CsvSource({
            // missing "==" (len%4 == 2)
            "QQ,                A", "MDEyMzQ1Njc4OQ,    0123456789",
            // missing "=" (len%4 == 3)
            "QUI,               AB", "SGVsbG8,           Hello",
            // no padding needed (len%4 == 0) — identical to padded case
            "QUJD,              ABC", })
    void decodeUnpaddedBase64ReturnsOriginalText(String b64, String expected) {
        assertThat(decode(b64.strip()), is(expected.strip()));
    }

    @ParameterizedTest(name = "roundtrip ''{0}''")
    @ValueSource(strings = {
            // 1 / 2 / 3-byte boundaries (determines padding count)
            "x", "xy", "xyz",
            // realistic LoRa payloads
            "Hello, World!", "0123456789", "sensor:temp=22.5",
            // multi-byte UTF-8 (2-byte sequences)
            "élève", })
    void roundtripEncodeDecodeRestoresOriginal(String text) {
        assertThat(decode(encode(text)), is(text));
    }
}
