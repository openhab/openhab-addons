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
package org.openhab.binding.tibberpulselocal.internal.sml;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.Fixtures;

/**
 * Tests the scenarios of the {@code channels} spec, "SML frame validity", at the level of frame extraction.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class SmlFramesTest {

    private static final byte[] ESC = { 0x1b, 0x1b, 0x1b, 0x1b };

    private static byte[] concat(byte[]... parts) {
        return SmlFrameBuilder.concat(parts);
    }

    private static byte[] messages(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (0x30 + i);
        }
        return data;
    }

    @Test
    void whenCrcOfTheCheckStringIsComputedThenItIsTheX25Value() {
        byte[] check = "123456789".getBytes(StandardCharsets.US_ASCII);

        assertEquals(0x906E, SmlFrames.crc16(check, 0, check.length));
        assertEquals(0x906E, SmlFrameBuilder.crc(check, check.length));
    }

    @Test
    void whenFixtureIsCheckedThenFrameAndMessageCrcsMatchTheBuilderImplementation() {
        byte[] frame = Fixtures.observedFrame();

        int frameCrc = SmlFrameBuilder.crc(frame, frame.length - 2);
        assertEquals(frameCrc & 0xFF, frame[frame.length - 2] & 0xFF, "frame CRC low byte");
        assertEquals(frameCrc >> 8, frame[frame.length - 1] & 0xFF, "frame CRC high byte");

        // The first message of the fixture is 48 bytes long, followed by 63 <low> <high> 00.
        int messageCrc = SmlFrameBuilder.crc(Arrays.copyOfRange(frame, 8, 8 + 48), 48);
        assertEquals(0x63, frame[8 + 48] & 0xFF);
        assertEquals(messageCrc & 0xFF, frame[8 + 49] & 0xFF);
        assertEquals(messageCrc >> 8, frame[8 + 50] & 0xFF);
    }

    @Test
    void whenFixtureIsExtractedThenOneBodyWithoutEscapesAndPaddingIsReturned() {
        byte[] frame = Fixtures.observedFrame();

        List<byte[]> frames = SmlFrames.extract(frame, 0);

        assertEquals(1, frames.size());
        // 712 bytes minus start (8), end escape, marker, padding count and CRC (8), and 3 padding bytes.
        assertArrayEquals(Arrays.copyOfRange(frame, 8, 712 - 8 - 3), frames.get(0));
    }

    @Test
    void whenPayloadHasAHeaderBeforeTheFrameThenTheOffsetSelectsTheFrame() {
        byte[] payload = concat(new byte[37], Fixtures.observedFrame());

        assertEquals(1, SmlFrames.extract(payload, 37).size());
        assertEquals(1, SmlFrames.extract(payload, 0).size());
    }

    @Test
    void whenPayloadIsEmptyOrTooShortThenNothingIsReturned() {
        assertTrue(SmlFrames.extract(new byte[0], 0).isEmpty());
        assertTrue(SmlFrames.extract(new byte[] { 0x1b, 0x1b, 0x1b }, 0).isEmpty());
        assertTrue(SmlFrames.extract(Fixtures.observedFrame(), 712).isEmpty());
        assertTrue(SmlFrames.extract(Fixtures.observedFrame(), 5000).isEmpty());
    }

    @Test
    void whenTheFrameIsCutAtAnyLengthThenNothingIsReturnedAndNothingIsThrown() {
        byte[] frame = Fixtures.observedFrame();
        for (int length = 0; length < frame.length; length++) {
            assertTrue(SmlFrames.extract(Arrays.copyOf(frame, length), 0).isEmpty(), "length " + length);
        }
    }

    @Test
    void whenAnyByteOfTheFrameIsChangedThenTheFrameIsRejected() {
        byte[] frame = Fixtures.observedFrame();
        for (int i = 0; i < frame.length; i++) {
            byte[] changed = Fixtures.with(frame, i, (frame[i] & 0xFF) ^ 0x5a);

            assertTrue(SmlFrames.extract(changed, 0).isEmpty(), "byte " + i);
        }
    }

    @Test
    void whenLastPayloadByteBeforeTheEndMarkerIsAlteredThenTheCrcRejectsTheFrame() {
        byte[] frame = Fixtures.observedFrame();

        assertTrue(SmlFrames.extract(Fixtures.with(frame, 712 - 8 - 1, 0x42), 0).isEmpty());
    }

    @Test
    void whenVersionBytesAreWrongThenTheFrameIsRejected() {
        byte[] frame = Fixtures.observedFrame();
        for (int i = 4; i < 8; i++) {
            frame[i] = 0x02;
        }

        assertTrue(SmlFrames.extract(frame, 0).isEmpty());
    }

    @Test
    void whenTheEndSequenceIsMissingThenTheFrameIsRejected() {
        byte[] frame = Fixtures.observedFrame();

        assertTrue(SmlFrames.extract(Arrays.copyOf(frame, 712 - 8), 0).isEmpty());
        assertTrue(SmlFrames.extract(concat(Arrays.copyOf(frame, 712 - 8), new byte[8]), 0).isEmpty());
    }

    @Test
    void whenPaddingTakesEveryLengthFromZeroToThreeThenTheBodyIsReturnedWithoutPadding() {
        SmlFrameBuilder builder = new SmlFrameBuilder();
        for (int length = 0; length < 12; length++) {
            byte[] body = messages(length);

            List<byte[]> frames = SmlFrames.extract(builder.frame(body), 0);

            assertEquals(1, frames.size(), "length " + length);
            assertArrayEquals(body, frames.get(0), "length " + length);
        }
    }

    @Test
    void whenPaddingCountIsFourThenTheFrameIsRejectedEvenWithAValidCrc() {
        byte[] frame = new SmlFrameBuilder().frame(messages(8));
        int countIndex = frame.length - 3;
        frame[countIndex] = 4;
        int crc = SmlFrameBuilder.crc(frame, frame.length - 2);
        frame[frame.length - 2] = (byte) crc;
        frame[frame.length - 1] = (byte) (crc >> 8);

        assertTrue(SmlFrames.extract(frame, 0).isEmpty());
    }

    @Test
    void whenTheBodyContainsAnEscapedEscapeSequenceThenItIsRestoredInTheBody() {
        byte[] body = concat(new byte[] { 1, 2 }, ESC, new byte[] { 3 }, ESC, ESC, new byte[] { 4, 5 });
        byte[] frame = new SmlFrameBuilder().doubleEscapes().frame(body);

        List<byte[]> frames = SmlFrames.extract(frame, 0);

        assertEquals(1, frames.size());
        assertArrayEquals(body, frames.get(0));
        // The doubled form is really on the wire: 3 sequences, each doubled, make the frame 12 bytes longer.
        assertEquals(new SmlFrameBuilder().frame(body).length + 12, frame.length);
    }

    @Test
    void whenTwoValidFramesAreInOnePayloadThenBothAreReturnedInOrder() {
        byte[] first = new SmlFrameBuilder().frame(messages(6));
        byte[] second = new SmlFrameBuilder().frame(messages(9));

        List<byte[]> frames = SmlFrames.extract(concat(first, second), 0);

        assertEquals(2, frames.size());
        assertArrayEquals(messages(6), frames.get(0));
        assertArrayEquals(messages(9), frames.get(1));
    }

    @Test
    void whenATruncatedFrameIsFollowedByAGoodFrameThenTheGoodFrameIsFound() {
        byte[] bad = Arrays.copyOf(new SmlFrameBuilder().frame(messages(40)), 20);
        byte[] good = new SmlFrameBuilder().frame(messages(9));

        List<byte[]> frames = SmlFrames.extract(concat(bad, good), 0);

        assertEquals(1, frames.size());
        assertArrayEquals(messages(9), frames.get(0));
    }

    @Test
    void whenAFrameWithAWrongCrcIsFollowedByAGoodFrameThenTheGoodFrameIsFound() {
        byte[] bad = new SmlFrameBuilder().frame(messages(40));
        bad[bad.length - 1] ^= 0x01;
        byte[] good = new SmlFrameBuilder().frame(messages(9));

        List<byte[]> frames = SmlFrames.extract(concat(bad, good), 0);

        assertEquals(1, frames.size());
        assertArrayEquals(messages(9), frames.get(0));
    }

    @Test
    void whenGarbageSurroundsAFrameThenTheFrameIsStillFound() {
        byte[] good = new SmlFrameBuilder().frame(messages(9));

        List<byte[]> frames = SmlFrames.extract(concat(new byte[] { 1, 2, 3, 0x1b, 0x1b }, good, new byte[] { 9, 9 }),
                0);

        assertEquals(1, frames.size());
    }

    @Test
    void whenThePayloadIsHostileThenExtractionStaysFastAndReturnsNothing() {
        byte[] escapes = new byte[64 * 1024];
        Arrays.fill(escapes, (byte) 0x1b);
        byte[] starts = new byte[64 * 1024];
        for (int i = 0; i < starts.length; i += 8) {
            System.arraycopy(new byte[] { 0x1b, 0x1b, 0x1b, 0x1b, 1, 1, 1, 1 }, 0, starts, i, 8);
        }

        assertTrue(SmlFrames.extract(escapes, 0).isEmpty());
        assertTrue(SmlFrames.extract(starts, 0).isEmpty());
    }
}
