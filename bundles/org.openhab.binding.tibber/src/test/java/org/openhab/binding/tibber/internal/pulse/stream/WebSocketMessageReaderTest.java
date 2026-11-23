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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.pulse.Fixtures;
import org.openhab.binding.tibber.internal.pulse.stream.StreamFailure.Kind;

/**
 * Tests the frame reader against the scenarios of the {@code connection} spec, "Websocket frame reading" and
 * "Liveness detection".
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
@SuppressWarnings("resource") // test streams are in-memory and need no closing
class WebSocketMessageReaderTest {

    private static final long LIVENESS_NANOS = Duration.ofSeconds(30).toNanos();

    private final AtomicLong clock = new AtomicLong(1_000_000_000L);
    private final ByteArrayOutputStream pongs = new ByteArrayOutputStream();
    private final AtomicInteger frames = new AtomicInteger();

    private WebSocketMessageReader reader(InputStream in) {
        return new WebSocketMessageReader(in, pongs, clock::get, LIVENESS_NANOS, frames::incrementAndGet);
    }

    private static byte[] observedFramePayload() {
        return FrameBytes.messageBytes(Fixtures.DEVICE_ID, "sml", Fixtures.observedFrame());
    }

    private static byte[] observedWire() {
        return FrameBytes.observed(Fixtures.DEVICE_ID, Fixtures.observedFrame());
    }

    private static StreamFailure failure(WebSocketMessageReader reader) {
        return assertThrows(StreamFailure.class, reader::readMessage);
    }

    private static byte[] payload(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (i * 31 + 7);
        }
        return data;
    }

    @Test
    void whenObservedFragmentedMessageIsReadThenOneMessageIsDelivered() throws IOException {
        byte[] wire = observedWire();
        // The two frame headers of the real bridge: 02 25 and 80 7e 02c8.
        assertEquals(0x02, wire[0] & 0xFF);
        assertEquals(0x25, wire[1] & 0xFF);
        assertEquals(0x80, wire[2 + 37] & 0xFF);
        assertEquals(0x7e, wire[2 + 37 + 1] & 0xFF);
        assertEquals(0x02c8, ((wire[2 + 37 + 2] & 0xFF) << 8) | (wire[2 + 37 + 3] & 0xFF));

        WebSocketMessageReader reader = reader(new ChunkedInputStream(wire));

        byte[] message = reader.readMessage();

        assertEquals(37 + 712, message.length);
        assertArrayEquals(observedFramePayload(), message);
    }

    @Test
    void whenSingleUnfragmentedFrameIsReadThenOneMessageIsDelivered() throws IOException {
        byte[] wire = FrameBytes.frame(FrameBytes.BINARY, true, observedFramePayload());

        byte[] message = reader(new ChunkedInputStream(wire)).readMessage();

        assertArrayEquals(observedFramePayload(), message);
    }

    @Test
    void whenTextFrameIsReadThenItIsDeliveredLikeBinary() throws IOException {
        byte[] wire = FrameBytes.frame(FrameBytes.TEXT, true, "hello".getBytes(StandardCharsets.US_ASCII));

        byte[] message = reader(new ChunkedInputStream(wire)).readMessage();

        assertArrayEquals("hello".getBytes(StandardCharsets.US_ASCII), message);
    }

    @Test
    void whenPayloadLengthsUse7And16And64BitFormsThenAllAreDeliveredUpToTheLimit() throws IOException {
        byte[] first = payload(100);
        byte[] second = payload(712);
        byte[] third = payload(60000);
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.BINARY, true, first),
                FrameBytes.frame(FrameBytes.BINARY, true, second),
                FrameBytes.frame(FrameBytes.BINARY, true, third, FrameBytes.FORM_64, null));
        WebSocketMessageReader reader = reader(new ChunkedInputStream(wire));

        assertArrayEquals(first, reader.readMessage());
        assertArrayEquals(second, reader.readMessage());
        assertArrayEquals(third, reader.readMessage());
    }

    @Test
    void whenPayloadOf70000BytesIsAnnouncedThenTheStreamEndsWithoutReadingIt() {
        byte[] header = FrameBytes.header64(FrameBytes.BINARY, true, 70000);
        WebSocketMessageReader reader = reader(new ChunkedInputStream(header).failBeyond(header.length));

        assertEquals(Kind.PROTOCOL, failure(reader).getKind());
    }

    @Test
    void whenPayloadOf100000BytesIsAnnouncedThenTheStreamEndsAfterTheHeader() {
        byte[] header = FrameBytes.header64(FrameBytes.BINARY, true, 100000);
        WebSocketMessageReader reader = reader(new ChunkedInputStream(header).failBeyond(header.length));

        assertEquals(Kind.PROTOCOL, failure(reader).getKind());
    }

    @Test
    void whenMessageHasExactlyTheLimitThenItIsDeliveredAndOneByteMoreFails() throws IOException {
        byte[] atLimit = payload(WebSocketMessageReader.MAX_MESSAGE_BYTES);
        byte[] over = payload(WebSocketMessageReader.MAX_MESSAGE_BYTES + 1);

        assertArrayEquals(atLimit,
                reader(new ChunkedInputStream(FrameBytes.frame(FrameBytes.BINARY, true, atLimit))).readMessage());
        byte[] overWire = FrameBytes.frame(FrameBytes.BINARY, true, over);
        int headerLength = overWire.length - over.length;
        assertEquals(Kind.PROTOCOL,
                failure(reader(new ChunkedInputStream(overWire).failBeyond(headerLength))).getKind());
    }

    @Test
    void whenFragmentsTogetherExceedTheLimitThenTheStreamEndsAtTheAnnouncingHeader() {
        byte[] firstFragment = FrameBytes.frame(FrameBytes.BINARY, false, payload(40000));
        byte[] secondHeaderOnly = FrameBytes.header64(FrameBytes.CONTINUATION, true, 40000);
        byte[] wire = FrameBytes.concat(firstFragment, secondHeaderOnly);
        WebSocketMessageReader reader = reader(new ChunkedInputStream(wire).failBeyond(wire.length));

        assertEquals(Kind.PROTOCOL, failure(reader).getKind());
    }

    @Test
    void whenFrameIsMaskedThenThePayloadIsUnmasked() throws IOException {
        byte[] content = payload(300);
        byte[] key = { (byte) 0xa1, 0x02, (byte) 0xf3, 0x44 };
        byte[] wire = FrameBytes.frame(FrameBytes.BINARY, true, content, FrameBytes.AUTO, key);

        assertArrayEquals(content, reader(new ChunkedInputStream(wire)).readMessage());
    }

    @Test
    void whenObservedMessageIsDeliveredByteWiseThenExactlyOneMessageIsDelivered() throws IOException {
        WebSocketMessageReader reader = reader(new ChunkedInputStream(observedWire(), 1));

        assertArrayEquals(observedFramePayload(), reader.readMessage());
        assertEquals(Kind.CLOSED_BY_BRIDGE, failure(reader).getKind());
    }

    @Test
    void whenTwoMessagesArriveInOneReadThenTwoMessagesAreDeliveredInOrder() throws IOException {
        byte[] first = FrameBytes.messageBytes(Fixtures.DEVICE_ID, "sml", payload(50));
        byte[] second = FrameBytes.messageBytes(Fixtures.DEVICE_ID, "sml", payload(60));
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.BINARY, true, first),
                FrameBytes.observed(Fixtures.DEVICE_ID, payload(60)));
        WebSocketMessageReader reader = reader(new ChunkedInputStream(wire));

        assertArrayEquals(first, reader.readMessage());
        assertArrayEquals(second, reader.readMessage());
    }

    @Test
    void whenObservedMessageIsSplitAtAnyPointThenItIsDeliveredOnce() throws IOException {
        byte[] wire = observedWire();
        for (int split = 1; split < wire.length; split++) {
            WebSocketMessageReader reader = reader(new ChunkedInputStream(wire, split, Integer.MAX_VALUE));

            assertArrayEquals(observedFramePayload(), reader.readMessage(), "split at " + split);
        }
    }

    @Test
    void whenChunkSizesAreRandomThenTwoMessagesAreDeliveredIntact() throws IOException {
        byte[] wire = FrameBytes.concat(observedWire(), observedWire());
        for (long seed = 1; seed <= 40; seed++) {
            WebSocketMessageReader reader = reader(ChunkedInputStream.random(wire, seed, 40));

            assertArrayEquals(observedFramePayload(), reader.readMessage(), "seed " + seed);
            assertArrayEquals(observedFramePayload(), reader.readMessage(), "seed " + seed);
        }
    }

    @Test
    void whenPingArrivesBetweenFragmentsThenAMaskedPongIsSentAndTheMessageIsComplete() throws IOException {
        byte[] ping = FrameBytes.frame(FrameBytes.PING, true, "abc".getBytes(StandardCharsets.US_ASCII));
        byte[] wire = FrameBytes.concat(
                FrameBytes.frame(FrameBytes.BINARY, false, "one".getBytes(StandardCharsets.US_ASCII)), ping,
                FrameBytes.frame(FrameBytes.CONTINUATION, true, "two".getBytes(StandardCharsets.US_ASCII)));

        byte[] message = reader(new ChunkedInputStream(wire, 1)).readMessage();

        assertArrayEquals("onetwo".getBytes(StandardCharsets.US_ASCII), message);
        byte[] pong = pongs.toByteArray();
        assertEquals(2 + 4 + 3, pong.length);
        assertEquals(0x8A, pong[0] & 0xFF, "FIN and opcode pong");
        assertEquals(0x80 | 3, pong[1] & 0xFF, "mask bit and length");
        byte[] unmasked = new byte[3];
        for (int i = 0; i < 3; i++) {
            unmasked[i] = (byte) (pong[6 + i] ^ pong[2 + (i & 3)]);
        }
        assertEquals("abc", new String(unmasked, StandardCharsets.US_ASCII));
    }

    @Test
    void whenPingHasNoPayloadThenAnEmptyMaskedPongIsSent() throws IOException {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.PING, true, new byte[0]),
                FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 1 }));

        reader(new ChunkedInputStream(wire)).readMessage();

        assertArrayEquals(new byte[] { (byte) 0x8A, (byte) 0x80 }, Arrays.copyOf(pongs.toByteArray(), 2));
        assertEquals(6, pongs.size());
    }

    @Test
    void whenPongArrivesThenItIsIgnoredAndNothingIsSent() throws IOException {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.PONG, true, new byte[] { 1, 2 }),
                FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 9 }));

        assertArrayEquals(new byte[] { 9 }, reader(new ChunkedInputStream(wire)).readMessage());
        assertEquals(0, pongs.size());
    }

    @Test
    void whenReservedOpcodeIsBetweenTwoMessagesThenBothAreDeliveredAndTheStreamStaysOpen() throws IOException {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 1 }),
                FrameBytes.frame(3, true, payload(20)), FrameBytes.frame(11, true, payload(5)),
                FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 2 }));
        WebSocketMessageReader reader = reader(new ChunkedInputStream(wire));

        assertArrayEquals(new byte[] { 1 }, reader.readMessage());
        assertArrayEquals(new byte[] { 2 }, reader.readMessage());
    }

    @Test
    void whenReservedOpcodeIsInsideAFragmentedMessageThenItIsDroppedAndTheMessageCompletes() throws IOException {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.BINARY, false, new byte[] { 1 }),
                FrameBytes.frame(5, true, payload(7)),
                FrameBytes.frame(FrameBytes.CONTINUATION, true, new byte[] { 2 }));

        assertArrayEquals(new byte[] { 1, 2 }, reader(new ChunkedInputStream(wire)).readMessage());
    }

    @Test
    void whenContinuationHasNoMessageInProgressThenTheStreamIsLost() {
        byte[] wire = FrameBytes.frame(FrameBytes.CONTINUATION, true, new byte[] { 1 });

        assertEquals(Kind.PROTOCOL, failure(reader(new ChunkedInputStream(wire))).getKind());
    }

    @Test
    void whenDataFrameArrivesInsideAFragmentedMessageThenTheStreamIsLost() {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.BINARY, false, new byte[] { 1 }),
                FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 2 }));

        assertEquals(Kind.PROTOCOL, failure(reader(new ChunkedInputStream(wire))).getKind());
    }

    @Test
    void whenControlFrameIsFragmentedThenTheStreamIsLost() {
        byte[] wire = FrameBytes.frame(FrameBytes.PING, false, new byte[] { 1 });

        assertEquals(Kind.PROTOCOL, failure(reader(new ChunkedInputStream(wire))).getKind());
    }

    @Test
    void whenControlFrameHasMoreThan125BytesThenTheStreamIsLostBeforeThePayload() {
        byte[] wire = FrameBytes.frame(FrameBytes.PING, true, payload(126));
        int headerLength = wire.length - 126;

        assertEquals(Kind.PROTOCOL, failure(reader(new ChunkedInputStream(wire).failBeyond(headerLength))).getKind());
    }

    @Test
    void whenControlFrameHasExactly125BytesThenItIsAnswered() throws IOException {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.PING, true, payload(125)),
                FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 1 }));

        reader(new ChunkedInputStream(wire)).readMessage();

        assertEquals(6 + 125, pongs.size());
    }

    @Test
    void whenAReservedBitIsSetThenTheStreamIsLost() {
        for (int bit : new int[] { 0x40, 0x20, 0x10 }) {
            byte[] wire = FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 1 });
            wire[0] |= (byte) bit;

            assertEquals(Kind.PROTOCOL, failure(reader(new ChunkedInputStream(wire))).getKind());
        }
    }

    @Test
    void whenA64BitLengthHasTheHighBitSetThenTheStreamIsLost() {
        byte[] header = FrameBytes.header64(FrameBytes.BINARY, true, Long.MIN_VALUE);

        assertEquals(Kind.PROTOCOL,
                failure(reader(new ChunkedInputStream(header).failBeyond(header.length))).getKind());
    }

    @Test
    void whenCloseFrameArrivesThenTheStreamIsReportedAsClosedByTheBridge() {
        byte[] wire = FrameBytes.frame(FrameBytes.CLOSE, true, new byte[] { 0x03, (byte) 0xe8 });

        assertEquals(Kind.CLOSED_BY_BRIDGE, failure(reader(new ChunkedInputStream(wire))).getKind());
    }

    @Test
    void whenStreamEndsInTheMiddleOfAFrameThenItIsReportedAsClosedByTheBridge() {
        byte[] wire = observedWire();
        for (int cut : new int[] { 0, 1, 2, 20, 39, 41, 45, 300, wire.length - 1 }) {
            WebSocketMessageReader reader = reader(new ChunkedInputStream(Arrays.copyOf(wire, cut)));

            assertEquals(Kind.CLOSED_BY_BRIDGE, failure(reader).getKind(), "cut at " + cut);
        }
    }

    @Test
    void whenTheReadTimesOutThenTheStreamIsReportedAsSilent() {
        InputStream timingOut = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new SocketTimeoutException();
            }
        };

        assertEquals(Kind.SILENT, failure(reader(timingOut)).getKind());
    }

    @Test
    void whenBytesDripInWithoutCompletingAFrameThenTheStreamIsReportedAsSilent() {
        byte[] wire = FrameBytes.frame(FrameBytes.BINARY, true, payload(100));
        // Every read takes ten seconds on the injected clock, so the limit of 30 seconds passes after four bytes.
        InputStream drip = new ChunkedInputStream(wire, position -> clock.addAndGet(Duration.ofSeconds(10).toNanos()),
                1);
        WebSocketMessageReader reader = reader(drip);

        assertEquals(Kind.SILENT, failure(reader).getKind());
    }

    @Test
    void whenOnlyPongsArriveEveryTwentySecondsThenTheStreamStaysAliveForTwoMinutes() throws IOException {
        byte[] pong = FrameBytes.frame(FrameBytes.PONG, true, new byte[0]);
        byte[] last = FrameBytes.frame(FrameBytes.BINARY, true, new byte[0]);
        byte[] wire = FrameBytes.concat(pong, pong, pong, pong, pong, pong, last);
        // The clock moves 20 seconds before each frame (every second byte offset) and by nothing inside a frame.
        Set<Integer> boundaries = Set.of(0, 2, 4, 6, 8, 10, 12);
        InputStream in = new ChunkedInputStream(wire, position -> {
            if (boundaries.contains(position)) {
                clock.addAndGet(Duration.ofSeconds(20).toNanos());
            }
        }, 2);

        byte[] message = reader(in).readMessage();

        assertEquals(0, message.length);
        assertEquals(7, frames.get());
    }

    @Test
    void whenThereIsNoFrameForMoreThanThirtySecondsThenTheNextByteEndsTheStream() {
        byte[] wire = FrameBytes.frame(FrameBytes.BINARY, true, new byte[] { 1 });
        InputStream in = new ChunkedInputStream(wire, position -> clock.addAndGet(Duration.ofSeconds(31).toNanos()));

        assertEquals(Kind.SILENT, failure(reader(in)).getKind());
    }

    @Test
    void whenFramesArriveThenTheFrameListenerIsCalledForEveryFrameIncludingControlFrames() throws IOException {
        byte[] wire = FrameBytes.concat(FrameBytes.frame(FrameBytes.PING, true, new byte[0]),
                FrameBytes.frame(7, true, new byte[0]), FrameBytes.frame(FrameBytes.BINARY, false, new byte[] { 1 }),
                FrameBytes.frame(FrameBytes.CONTINUATION, true, new byte[] { 2 }));

        reader(new ChunkedInputStream(wire)).readMessage();

        assertEquals(4, frames.get());
    }
}
