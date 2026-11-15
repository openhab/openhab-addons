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
package org.openhab.binding.atv.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SmallReadInputStream}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class SmallReadInputStreamTest {

    /** The mark the MP3 decoder leaves on its stream. */
    private static final int DECODER_MARK_LIMIT = 4_096_001;

    private final LiveSource source = new LiveSource(100 * SmallReadInputStream.MAX_READ);

    @AfterEach
    void tearDown() {
        source.end.countDown();
    }

    @Test
    void bulkReadsAreLimited() throws Exception {
        assertEquals(SmallReadInputStream.MAX_READ, new SmallReadInputStream(source).read(new byte[8192]));
    }

    @Test
    void decoderBufferGetsAudioThatHasArrived() {
        BufferedInputStream buffered = new BufferedInputStream(new SmallReadInputStream(source));
        buffered.mark(DECODER_MARK_LIMIT);

        int total = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            byte[] chunk = new byte[4096];
            int read = 0;
            int n;
            while (read < source.arrived && (n = buffered.read(chunk)) != -1) {
                read += n;
            }
            return read;
        });
        assertEquals(source.arrived, total);
    }

    /** A live stream that implements only {@code read()} and waits once the audio that has arrived is used up. */
    private static class LiveSource extends InputStream {

        final int arrived;
        final CountDownLatch end = new CountDownLatch(1);
        private int position;

        LiveSource(int arrived) {
            this.arrived = arrived;
        }

        @Override
        public int read() {
            if (position < arrived) {
                position++;
                return 0;
            }
            try {
                end.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return -1;
        }
    }
}
