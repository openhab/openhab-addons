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
package org.openhab.binding.atv.internal.client.protocols.raop;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for how {@link RaopStreamClient} paces audio after the source stalls.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class RaopStreamClientTest {

    private final AtomicLong nanos = new AtomicLong();
    private final StreamContext context = new StreamContext();
    private final RaopStreamClient.Statistics stats = new RaopStreamClient.Statistics(context.sampleRate, nanos::get);

    @BeforeEach
    void setUp() {
        context.reset(new Random(1), Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC));
        send(context.sampleRate);
        nanos.addAndGet(TimeUnit.SECONDS.toNanos(1));
    }

    @Test
    void longStallIsSkippedAndTimestampKeepsUpWithTheClock() {
        nanos.addAndGet(TimeUnit.SECONDS.toNanos(10));

        RaopStreamClient.skipStall(context, stats);

        assertEquals(0, stats.framesBehind());
        // Sync packets turn headTs into the current time, so it must match the 11 seconds that passed
        assertEquals(11L * context.sampleRate, context.headTs - context.startTs);
    }

    @Test
    void shortDelayIsCaughtUpInsteadOfSkipped() {
        nanos.addAndGet(TimeUnit.MILLISECONDS.toNanos(500));
        long headTs = context.headTs;

        RaopStreamClient.skipStall(context, stats);

        assertEquals(headTs, context.headTs);
        assertEquals(context.sampleRate / 2, stats.framesBehind());
    }

    /** Mirrors what sending a packet does to the timestamp and the statistics. */
    private void send(int frames) {
        context.headTs += frames;
        stats.tick(frames);
    }
}
