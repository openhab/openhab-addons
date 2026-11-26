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
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Randomised frame streams against the reader (QA review, stage 5): whatever the bytes are and however they are
 * segmented, the reader ends with a {@link StreamFailure} and never with another exception, never delivers more than
 * the message limit, and never hangs.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(120)
@SuppressWarnings("resource") // in-memory streams
class WebSocketMessageReaderFuzzTest {

    private static final long LIVENESS_NANOS = Duration.ofSeconds(30).toNanos();

    private static byte[] randomWire(Random random) {
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        int frames = 1 + random.nextInt(8);
        for (int f = 0; f < frames; f++) {
            switch (random.nextInt(6)) {
                case 0:
                    // Pure noise, including bytes that look like long length announcements.
                    byte[] noise = new byte[random.nextInt(40)];
                    random.nextBytes(noise);
                    wire.writeBytes(noise);
                    break;
                case 1:
                    // A header with a random announced length and no payload.
                    wire.writeBytes(FrameBytes.header64(random.nextInt(16), random.nextBoolean(),
                            random.nextInt(4) == 0 ? random.nextLong() : random.nextInt(70_000)));
                    break;
                default:
                    byte[] payload = new byte[random.nextInt(4) == 0 ? random.nextInt(300) : random.nextInt(20)];
                    random.nextBytes(payload);
                    byte[] key = random.nextBoolean() ? null : new byte[] { 1, 2, 3, 4 };
                    int form = new int[] { FrameBytes.AUTO, FrameBytes.FORM_16, FrameBytes.FORM_64 }[random.nextInt(3)];
                    if (form == FrameBytes.FORM_7) {
                        form = FrameBytes.AUTO;
                    }
                    // Lengths of 126 and more cannot use the 7-bit form; AUTO picks a valid one otherwise.
                    wire.writeBytes(FrameBytes.frame(random.nextInt(16), random.nextBoolean(), payload,
                            payload.length < 126 ? form : FrameBytes.AUTO, key));
            }
        }
        return wire.toByteArray();
    }

    @Test
    void whenRandomBytesAreReadInRandomChunksThenOnlyAStreamFailureEndsTheReading() {
        for (int seed = 0; seed < 20_000; seed++) {
            Random random = new Random(seed);
            byte[] wire = randomWire(random);
            AtomicLong clock = new AtomicLong(1);
            AtomicInteger frames = new AtomicInteger();
            WebSocketMessageReader reader = new WebSocketMessageReader(
                    ChunkedInputStream.random(wire, seed, 1 + random.nextInt(9)), new ByteArrayOutputStream(),
                    clock::get, LIVENESS_NANOS, frames::incrementAndGet);
            try {
                for (int i = 0; i < 100; i++) {
                    byte[] message = reader.readMessage();
                    assertTrue(message.length <= WebSocketMessageReader.MAX_MESSAGE_BYTES, "seed " + seed);
                }
                fail("seed " + seed + ": 100 messages from a finite stream");
            } catch (StreamFailure expected) {
                // The stream ended: close, protocol violation, or end of data.
            } catch (IOException | RuntimeException | OutOfMemoryError e) {
                fail("seed " + seed + ": " + e);
            }
        }
    }

    @Test
    void whenAHugeNumberOfEmptyFragmentsArrivesThenReadingContinuesWithoutGrowingMemory() throws IOException {
        // A fragmented message that never ends: each frame is valid, so only the size limit and the liveness clock
        // (which the frames keep alive) bound it. The data is finite here, so the stream ends with end of data.
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        wire.writeBytes(FrameBytes.frame(FrameBytes.BINARY, false, new byte[0]));
        byte[] empty = FrameBytes.frame(FrameBytes.CONTINUATION, false, new byte[0]);
        for (int i = 0; i < 200_000; i++) {
            wire.writeBytes(empty);
        }
        WebSocketMessageReader reader = new WebSocketMessageReader(new ChunkedInputStream(wire.toByteArray()),
                new ByteArrayOutputStream(), () -> 1L, LIVENESS_NANOS, () -> {
                });

        StreamFailure failure = assertThrows(StreamFailure.class, reader::readMessage);

        assertEquals(StreamFailure.Kind.CLOSED_BY_BRIDGE, failure.getKind());
    }
}
