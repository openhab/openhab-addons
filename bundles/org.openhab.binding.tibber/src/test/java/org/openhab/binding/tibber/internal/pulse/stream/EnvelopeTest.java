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
package org.openhab.binding.tibber.internal.pulse.stream;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.pulse.Fixtures;

/**
 * Tests the message envelope scenarios of the {@code connection} spec.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
class EnvelopeTest {

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] withPayload(String header, byte... payload) {
        return FrameBytes.concat(ascii(header), payload);
    }

    @Test
    void whenBinaryMessageHasUpperCaseDeviceIdThenItIsNormalisedAndThePayloadOffsetPointsBehindTheHeader() {
        byte[] message = withPayload("<device:0123456789ABCDEF topic:\"sml\">", (byte) 0x1b, (byte) 0x1b);

        Envelope envelope = Envelope.parse(message);

        assertNotNull(envelope);
        assertEquals("0123456789abcdef", envelope.deviceId());
        assertEquals("sml", envelope.topic());
        assertTrue(envelope.isSml());
        assertEquals(37, envelope.payloadOffset());
        assertEquals(0x1b, message[envelope.payloadOffset()]);
    }

    @Test
    void whenTextMessageIsParsedThenTheResultEqualsTheBinaryMessage() {
        byte[] binary = FrameBytes.messageBytes(Fixtures.DEVICE_ID, "sml", Fixtures.observedFrame());
        // The bytes are what makes a message text or binary on the wire, not the content; both reach the parser alike.
        byte[] text = binary.clone();

        assertEquals(Envelope.parse(binary), Envelope.parse(text));
    }

    @Test
    void whenHeaderIsMissingThenTheMessageIsIgnored() {
        assertNull(Envelope.parse(withPayload("device:0123456789abcdef topic:\"sml\">", (byte) 1)));
        assertNull(Envelope.parse(new byte[0]));
        assertNull(Envelope.parse(Fixtures.observedFrame()));
    }

    @Test
    void whenClosingBracketIsMissingThenTheMessageIsIgnored() {
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdef topic:\"sml\"", (byte) 1, (byte) 2)));
    }

    @Test
    void whenClosingBracketIsBeyondTheSearchWindowThenTheMessageIsIgnored() {
        String padded = "<device:0123456789abcdef topic:\"" + "s".repeat(60) + "\">";

        assertNull(Envelope.parse(ascii(padded)));
    }

    @Test
    void whenDeviceIdHasTwelveDigitsThenTheMessageIsIgnored() {
        assertNull(Envelope.parse(withPayload("<device:0123456789ab topic:\"sml\">", (byte) 1)));
    }

    @Test
    void whenDeviceIdHasSeventeenDigitsOrNonHexCharactersThenTheMessageIsIgnored() {
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdef0 topic:\"sml\">", (byte) 1)));
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdeg topic:\"sml\">", (byte) 1)));
    }

    @Test
    void whenTopicIsUnknownThenTheEnvelopeIsParsedButIsNotSml() {
        Envelope envelope = Envelope.parse(withPayload("<device:0123456789abcdef topic:\"status\">", (byte) 1));

        assertNotNull(envelope);
        assertEquals("status", envelope.topic());
        assertFalse(envelope.isSml());
    }

    @Test
    void whenTopicIsMalformedThenTheMessageIsIgnored() {
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdef topic:sml>", (byte) 1)));
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdef topic:\"\">", (byte) 1)));
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdef>", (byte) 1)));
        assertNull(Envelope.parse(withPayload("<device:0123456789abcdef topic:\"a\"b\">", (byte) 1)));
    }

    @Test
    void whenThereIsNoPayloadThenTheEnvelopeHasAnEmptyPayload() {
        byte[] message = ascii("<device:0123456789abcdef topic:\"sml\">");

        Envelope envelope = Envelope.parse(message);

        assertNotNull(envelope);
        assertEquals(message.length, envelope.payloadOffset());
    }

    @Test
    void whenEnvelopeIsPrintedThenTheDeviceIdIsNotIncluded() {
        Envelope envelope = Envelope.parse(withPayload("<device:0123456789abcdef topic:\"sml\">", (byte) 1));

        assertNotNull(envelope);
        assertFalse(envelope.toString().contains("0123456789abcdef"));
    }
}
