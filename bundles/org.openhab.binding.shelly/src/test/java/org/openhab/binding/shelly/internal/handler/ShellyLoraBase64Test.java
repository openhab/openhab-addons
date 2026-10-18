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
 * {@link ShellyComponents#handleLoraCommand} and {@code Shelly2ApiRpc}.
 *
 * <p>
 * The encode path (CHANNEL_LORA_TXDATA) produces standard padded base64.
 * The decode paths (CHANNEL_LORA_TXDATARAW and the "lora" event) must
 * tolerate both padded and unpadded base64 by adding missing '=' chars before
 * passing the string to {@link Base64.Decoder}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyLoraBase64Test {

    private static String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String b64) {
        return new String(Base64.getDecoder().decode(fixBase64Padding(b64)), StandardCharsets.UTF_8);
    }

    @ParameterizedTest(name = "fixBase64Padding(''{0}'') = ''{1}''")
    @CsvSource({ "QUJD,          QUJD", "QQ,            QQ==", "QUI,           QUI=", "QQ==,          QQ==",
            "QUI=,          QUI=", })
    void fixBase64PaddingAddsCorrectPadding(String input, String expected) {
        assertThat(fixBase64Padding(input.strip()), is(expected.strip()));
    }

    @Test
    void fixBase64PaddingRem1LeavesStringUnchanged() {
        String invalid = "Q";
        assertThat(fixBase64Padding(invalid), is(invalid));
    }

    @ParameterizedTest(name = "encode ''{0}'' → ''{1}''")
    @CsvSource({ "A,                 QQ==", "AB,                QUI=", "ABC,               QUJD",
            "Hello,             SGVsbG8=", "0123456789,        MDEyMzQ1Njc4OQ==", })
    void encodeProducesStandardPaddedBase64(String text, String expected) {
        assertThat(encode(text.strip()), is(expected.strip()));
    }

    @ParameterizedTest(name = "decode padded ''{0}'' → ''{1}''")
    @CsvSource({ "QQ==,              A", "QUI=,              AB", "QUJD,              ABC", "SGVsbG8=,          Hello",
            "MDEyMzQ1Njc4OQ==,  0123456789", })
    void decodePaddedBase64ReturnsOriginalText(String b64, String expected) {
        assertThat(decode(b64.strip()), is(expected.strip()));
    }

    @ParameterizedTest(name = "decode unpadded ''{0}'' → ''{1}''")
    @CsvSource({ "QQ,                A", "MDEyMzQ1Njc4OQ,    0123456789", "QUI,               AB",
            "SGVsbG8,           Hello", "QUJD,              ABC", })
    void decodeUnpaddedBase64ReturnsOriginalText(String b64, String expected) {
        assertThat(decode(b64.strip()), is(expected.strip()));
    }

    @ParameterizedTest(name = "roundtrip ''{0}''")
    @ValueSource(strings = { "x", "xy", "xyz", "Hello, World!", "0123456789", "sensor:temp=22.5", "élève", })
    void roundtripEncodeDecodeRestoresOriginal(String text) {
        assertThat(decode(encode(text)), is(text));
    }
}
