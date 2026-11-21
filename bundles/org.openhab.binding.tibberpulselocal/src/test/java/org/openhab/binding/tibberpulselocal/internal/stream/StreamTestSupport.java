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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.openhab.binding.tibberpulselocal.internal.Fixtures;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.BackoffWaiter;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.Timing;

/**
 * Base class of the stream client tests: creates clients against the local {@link FakeBridge} and cleans up.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
abstract class StreamTestSupport {

    static final String HOST = "127.0.0.1";
    static final String PASSWORD = "p4ss:word";
    static final long WAIT_MILLIS = 15_000;

    private static final AtomicInteger COUNTER = new AtomicInteger();

    /** Generous limits for tests that do not shorten them. */
    static final Timing LONG_TIMING = new Timing(Duration.ofSeconds(5), Duration.ofHours(1), Duration.ofSeconds(2));

    private final List<StreamClient> clients = new ArrayList<>();
    private final List<FakeBridge> bridges = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (StreamClient client : clients) {
            client.stop();
        }
        for (FakeBridge bridge : bridges) {
            bridge.close();
        }
    }

    FakeBridge bridge(FakeBridge.Behaviour... behaviours) throws IOException {
        FakeBridge bridge = new FakeBridge(behaviours);
        bridges.add(bridge);
        return bridge;
    }

    /** @return a port on the loopback address on which nothing listens */
    static int closedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }

    /** The wire bytes of one message as the bridge sends it, with a valid SML frame. */
    static byte[] validWire() {
        return FrameBytes.observed(Fixtures.DEVICE_ID, Fixtures.observedFrame());
    }

    static String threadName() {
        return "tpl-test-reader-" + COUNTER.incrementAndGet();
    }

    /** Creates a client, registers it for cleanup, and does not start it. */
    StreamClient client(int port, String threadName, Timing timing, BackoffWaiter waiter, LongSupplier clock,
            StreamListener listener) {
        return client(port, PASSWORD, threadName, timing, waiter, clock, listener);
    }

    StreamClient client(int port, String password, String threadName, Timing timing, BackoffWaiter waiter,
            LongSupplier clock, StreamListener listener) {
        return client(HOST, port, password, threadName, timing, waiter, clock, listener);
    }

    StreamClient client(String host, int port, String password, String threadName, Timing timing, BackoffWaiter waiter,
            LongSupplier clock, StreamListener listener) {
        StreamClient client = new StreamClient(host, port, password, threadName, timing, waiter, clock, listener);
        clients.add(client);
        return client;
    }

    StreamClient client(FakeBridge bridge, String threadName, Timing timing, BackoffWaiter waiter,
            StreamListener listener) {
        return client(bridge.port(), threadName, timing, waiter, System::nanoTime, listener);
    }

    /** Waits until no thread of this name is alive. */
    static boolean awaitThreadEnd(String name, long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        while (isAlive(name)) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            Thread.sleep(10);
        }
        return true;
    }

    static boolean isAlive(String name) {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && name.equals(t.getName()));
    }
}
