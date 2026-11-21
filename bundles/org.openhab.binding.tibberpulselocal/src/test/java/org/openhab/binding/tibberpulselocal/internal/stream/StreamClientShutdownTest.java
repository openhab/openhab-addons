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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Active;
import org.openhab.binding.tibberpulselocal.internal.stream.RecordingListener.Message;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.BackoffWaiter;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.Timing;

/**
 * Tests the scenarios of the {@code connection} spec, "Shutdown and reconfiguration" and "At most one stream per
 * bridge". Every wait is bounded, and the reader threads are daemons.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class StreamClientShutdownTest extends StreamTestSupport {

    @Test
    void whenTheStreamIsSilentAndStopIsCalledThenTheConnectionIsClosedAndNoThreadKeepsReading() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.silent());
        RecordingListener listener = new RecordingListener();
        String name = threadName();
        StreamClient client = client(bridge, name, LONG_TIMING, new RecordingWaiter(1), listener);
        client.start();
        assertNotNull(bridge.nextRequest(WAIT_MILLIS));
        assertTrue(isAlive(name));

        client.stop();

        assertFalse(isAlive(name), "stop joins the reader thread");
        assertTrue(bridge.awaitNoOpenConnections(WAIT_MILLIS), "the bridge sees the stream closed");
        assertTrue(listener.isEmpty(), "stopping is no failure");
    }

    @Test
    void whenStopIsCalledWhileWaitingToReconnectThenNoFurtherAttemptIsMade() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.closing());
        CountDownLatch waiting = new CountDownLatch(1);
        // Waits like the production waiter, but a long time, and tells the test when the wait has started.
        BackoffWaiter waiter = (delay, stopped) -> {
            waiting.countDown();
            return stopped.await(60, TimeUnit.SECONDS);
        };
        RecordingListener listener = new RecordingListener();
        String name = threadName();
        StreamClient client = client(bridge, name, LONG_TIMING, waiter, listener);
        client.start();
        assertTrue(waiting.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        client.stop();

        assertFalse(isAlive(name));
        assertEquals(1, bridge.accepted());
        assertTrue(bridge.awaitNoOpenConnections(WAIT_MILLIS));
    }

    @Test
    void whenStopIsCalledWhileTheListenerIsBusyThenNoCallbackFollowsAfterItReturns() throws Exception {
        // Two messages are already buffered when the first callback blocks.
        FakeBridge bridge = bridge(FakeBridge.sending(FrameBytes.concat(validWire(), validWire())));
        CountDownLatch inCallback = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        RecordingListener listener = new RecordingListener(message -> {
            inCallback.countDown();
            try {
                release.await(WAIT_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        });
        String name = threadName();
        Timing shortJoin = new Timing(Duration.ofSeconds(5), Duration.ofHours(1), Duration.ofMillis(200));
        StreamClient client = client(bridge, name, shortJoin, new RecordingWaiter(1), listener);
        client.start();
        assertTrue(inCallback.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));

        client.stop(); // the join times out: the callback is still running
        release.countDown();

        assertTrue(awaitThreadEnd(name, WAIT_MILLIS));
        assertInstanceOf(Active.class, listener.next(WAIT_MILLIS));
        assertInstanceOf(Message.class, listener.next(WAIT_MILLIS));
        assertNull(listener.next(200), "neither the second message nor a loss is reported after stop");
    }

    @Test
    void whenAClientIsStartedAndStoppedTenTimesThenAtMostOneStreamIsOpenAtAnyMoment() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.sending(validWire()));
        for (int i = 0; i < 10; i++) {
            RecordingListener listener = new RecordingListener();
            String name = threadName();
            StreamClient client = client(bridge, name, LONG_TIMING, new RecordingWaiter(1), listener);
            client.start();
            if (i % 2 == 0) {
                listener.await(Message.class, WAIT_MILLIS);
            }

            client.stop();

            assertFalse(isAlive(name), "cycle " + i);
            assertTrue(bridge.awaitNoOpenConnections(WAIT_MILLIS), "cycle " + i);
        }

        assertTrue(bridge.maxOpen() <= 1, "at most one open stream, saw " + bridge.maxOpen());
    }

    @Test
    void whenStopIsCalledTwiceOrBeforeStartThenNothingFails() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.silent());
        StreamClient neverStarted = client(bridge, threadName(), LONG_TIMING, new RecordingWaiter(1),
                new RecordingListener());
        neverStarted.stop();
        neverStarted.stop();

        String name = threadName();
        StreamClient client = client(bridge, name, LONG_TIMING, new RecordingWaiter(1), new RecordingListener());
        client.start();
        assertNotNull(bridge.nextRequest(WAIT_MILLIS));
        client.stop();
        client.stop();

        assertFalse(isAlive(name));
        assertEquals(1, bridge.accepted());
    }

    @Test
    void whenTheReaderThreadIsCreatedThenItIsADaemonWithTheGivenName() throws Exception {
        FakeBridge bridge = bridge(FakeBridge.silent());
        String name = threadName();
        client(bridge, name, LONG_TIMING, new RecordingWaiter(1), new RecordingListener()).start();
        assertNotNull(bridge.nextRequest(WAIT_MILLIS));

        Thread reader = Thread.getAllStackTraces().keySet().stream().filter(t -> name.equals(t.getName())).findFirst()
                .orElseThrow();

        assertTrue(reader.isDaemon(), "a stuck reader must not keep the JVM alive");
    }
}
