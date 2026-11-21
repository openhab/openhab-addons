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
package org.openhab.binding.tibberpulselocal.internal.stream;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Lost;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.Timing;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamFailure.Kind;

/**
 * Tests the reconnect rules of the {@code connection} spec, "Reconnect with backoff". The delays are the ones the
 * client asks its waiter for, so no test waits for them; stream age is moved by an injected clock.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class StreamClientBackoffTest extends StreamTestSupport {

    private static final String HTTP_401 = "HTTP/1.1 401 Unauthorized\r\n\r\n";

    private final AtomicLong nanos = new AtomicLong(5_000_000_000L);

    private void advance(Duration duration) {
        nanos.addAndGet(duration.toNanos());
    }

    /** A listener that treats every message as valid and moves the clock on every second message by the given time. */
    private RecordingListener advancingOnEverySecondMessage(Duration step) {
        AtomicInteger count = new AtomicInteger();
        return new RecordingListener(message -> {
            if (count.incrementAndGet() % 2 == 0) {
                advance(step);
            }
            return true;
        });
    }

    @Test
    void whenEveryAttemptIsRefusedThenTheDelaysAreTwoFourEightSixteenThirtyTwoSixtySixty() throws Exception {
        RecordingWaiter waiter = new RecordingWaiter(7);
        RecordingListener listener = new RecordingListener();
        client(closedPort(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L, 16L, 32L, 60L, 60L), waiter.seconds());
        for (long seconds : waiter.seconds()) {
            assertTrue(seconds >= 2, "never less than two seconds between attempts");
        }
    }

    @Test
    void whenTheFirstAttemptStartsThenNoDelayHasBeenRequestedBefore() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        RecordingWaiter waiter = new RecordingWaiter(1);
        AtomicInteger waitsAtFirstMessage = new AtomicInteger(-1);
        CountDownLatch inspected = new CountDownLatch(1);
        RecordingListener listener = new RecordingListener(message -> {
            waitsAtFirstMessage.compareAndSet(-1, waiter.waits());
            inspected.countDown();
            return true;
        });
        client(bridge, threadName(), LONG_TIMING, waiter, listener).start();

        // The listener records the event before it runs this predicate, so wait for the predicate itself.
        assertTrue(inspected.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        assertEquals(0, waitsAtFirstMessage.get());
    }

    @Test
    void whenAStreamWasValidAndOpenForThirtyFiveSecondsThenTheNextDelayIsTwoSeconds() throws Exception {
        // Three immediate closes grow the delay to eight seconds, then one stable stream, then closes again.
        FakeBridge bridge = bridge(FakeBridge.closing(), FakeBridge.closing(), FakeBridge.closing(),
                FakeBridge.sendingThenClosing(validWire(), validWire()), FakeBridge.closing());
        RecordingWaiter waiter = new RecordingWaiter(5);
        RecordingListener listener = advancingOnEverySecondMessage(Duration.ofSeconds(35));
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L, 2L, 4L), waiter.seconds());
    }

    @Test
    void whenValidStreamsLiveOnlyFiveSecondsThenTheBackoffIsNotReset() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sendingThenClosing(validWire(), validWire()));
        RecordingWaiter waiter = new RecordingWaiter(3);
        RecordingListener listener = advancingOnEverySecondMessage(Duration.ofSeconds(5));
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L), waiter.seconds());
    }

    @Test
    void whenTheStreamLivesLongButNeverDeliversAValidReadingThenTheBackoffIsNotReset() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sendingThenClosing(validWire(), validWire()));
        RecordingWaiter waiter = new RecordingWaiter(3);
        AtomicInteger count = new AtomicInteger();
        RecordingListener listener = new RecordingListener(message -> {
            if (count.incrementAndGet() % 2 == 0) {
                advance(Duration.ofSeconds(120));
            }
            return false;
        });
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L), waiter.seconds());
    }

    @Test
    void whenTheStreamIsValidForExactlyThirtySecondsThenTheBackoffIsReset() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.closing(), FakeBridge.closing(),
                FakeBridge.sendingThenClosing(validWire(), validWire()), FakeBridge.closing());
        RecordingWaiter waiter = new RecordingWaiter(4);
        RecordingListener listener = advancingOnEverySecondMessage(Duration.ofSeconds(30));
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 2L, 4L), waiter.seconds());
    }

    @Test
    void whenConnectionsStaySilentThenTheDelaysKeepGrowing() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.silent());
        RecordingWaiter waiter = new RecordingWaiter(3);
        RecordingListener listener = new RecordingListener();
        Timing shortLiveness = new Timing(Duration.ofSeconds(5), Duration.ofMillis(200), Duration.ofSeconds(2));
        client(bridge, threadName(), shortLiveness, waiter, listener).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L), waiter.seconds());
        assertEquals(Kind.SILENT, listener.await(Lost.class, WAIT_MILLIS).failure().getKind());
        assertEquals(3, bridge.accepted());
    }

    @Test
    void whenTheBridgeAnswers401AfterAnOpenStreamThenTheClientStopsRetrying() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sendingThenClosing(validWire()),
                FakeBridge.answeringThenClosing(HTTP_401));
        RecordingWaiter waiter = new RecordingWaiter(10);
        RecordingListener listener = new RecordingListener();
        String name = threadName();
        client(bridge, name, LONG_TIMING, waiter, listener).start();

        assertEquals(Kind.CLOSED_BY_BRIDGE, listener.await(Lost.class, WAIT_MILLIS).failure().getKind());
        assertEquals(Kind.REJECTED, listener.await(Lost.class, WAIT_MILLIS).failure().getKind());

        assertTrue(awaitThreadEnd(name, WAIT_MILLIS));
        assertEquals(2, bridge.accepted());
        assertEquals(1, waiter.waits());
    }

    @Test
    void whenAnotherClientKeepsTakingTheSlotThenTheAttemptsStillFollowTheDelays() throws Exception {
        // The bridge closes every stream right after the first message, as it does when another client evicts ours.
        FakeBridge bridge = bridge(FakeBridge.sendingThenClosing(validWire()));
        RecordingWaiter waiter = new RecordingWaiter(6);
        client(bridge.port(), threadName(), LONG_TIMING, waiter, nanos::get, new RecordingListener()).start();

        assertTrue(waiter.awaitFinished(WAIT_MILLIS));

        assertEquals(List.of(2L, 4L, 8L, 16L, 32L, 60L), waiter.seconds());
        assertEquals(6, bridge.accepted());
    }
}
