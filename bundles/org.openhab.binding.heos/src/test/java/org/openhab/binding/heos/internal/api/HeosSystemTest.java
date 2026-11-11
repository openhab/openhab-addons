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
package org.openhab.binding.heos.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.heos.internal.resources.HeosConstants.EVENT_STREAM_TIMEOUT;
import static org.openhab.binding.heos.internal.resources.HeosConstants.EVENT_TYPE_EVENT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.heos.internal.resources.HeosEventListener;

/**
 * Tests how the heartbeat of the {@link HeosSystem} treats an event stream that stays quiet, against a loopback
 * stand-in for the HEOS CLI that never sends change events.
 *
 * @author Kevin Shanahan - Initial contribution
 */
@NonNullByDefault
public class HeosSystemTest {
    private static final int HEARTBEAT = 60;

    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final List<Runnable> heartbeatJobs = new CopyOnWriteArrayList<>();
    private final HeosEventListener listener = mock(HeosEventListener.class);
    private @NonNullByDefault({}) FakeHeosCli cli;
    private @NonNullByDefault({}) HeosSystem system;

    @BeforeEach
    public void setUp() throws IOException {
        cli = new FakeHeosCli();
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        doAnswer(invocation -> {
            heartbeatJobs.add(invocation.getArgument(0));
            return future;
        }).when(scheduler).scheduleWithFixedDelay(any(), eq(30L), eq((long) HEARTBEAT), eq(TimeUnit.SECONDS));
        system = new HeosSystem(scheduler);
    }

    @AfterEach
    public void tearDown() throws IOException {
        system.closeConnection();
        cli.close();
    }

    @Test
    public void heartbeatIsSentWhileTheEventStreamIsFresh() throws Exception {
        connect();

        runLatestHeartbeat();

        assertEquals(1, cli.heartbeats.get());
        verify(listener, never()).bridgeChangeEvent(any(), anyBoolean(), any());
    }

    @Test
    public void quietEventStreamIsResetOnceAndHeartbeatsResumeAfterReconnecting() throws Exception {
        connect();
        backdateLastEvent();

        runLatestHeartbeat();

        verify(listener).bridgeChangeEvent(EVENT_TYPE_EVENT, false, EVENT_STREAM_TIMEOUT);
        assertEquals(0, cli.heartbeats.get());

        // The bridge handler re-establishes the connection when the system is reachable again.
        connect();
        runLatestHeartbeat();

        verify(listener, times(1)).bridgeChangeEvent(EVENT_TYPE_EVENT, false, EVENT_STREAM_TIMEOUT);
        assertEquals(1, cli.heartbeats.get());
    }

    private void connect() throws Exception {
        system.establishConnection(InetAddress.getLoopbackAddress().getHostAddress(), cli.port(), HEARTBEAT)
                .registerForChangeEvents(listener);
    }

    private void runLatestHeartbeat() {
        heartbeatJobs.getLast().run();
    }

    /**
     * The event stream threshold is two hours. Rather than waiting, make the last event older than that.
     */
    private void backdateLastEvent() throws ReflectiveOperationException {
        Field eventController = HeosSystem.class.getDeclaredField("eventController");
        eventController.setAccessible(true);
        Field lastEventTime = HeosEventController.class.getDeclaredField("lastEventTime");
        lastEventTime.setAccessible(true);
        lastEventTime.setLong(eventController.get(system), System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3));
    }

    /**
     * Answers every request with success and never sends a change event, like a HEOS system nobody is using.
     */
    private static class FakeHeosCli implements AutoCloseable {
        private final ServerSocket server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        private final List<Socket> sockets = new CopyOnWriteArrayList<>();
        private final AtomicInteger heartbeats = new AtomicInteger();

        FakeHeosCli() throws IOException {
            Thread acceptor = new Thread(this::accept, "fake-heos-cli");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        int port() {
            return server.getLocalPort();
        }

        private void accept() {
            try {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    sockets.add(socket);
                    Thread connection = new Thread(() -> serve(socket), "fake-heos-cli-connection");
                    connection.setDaemon(true);
                    connection.start();
                }
            } catch (IOException e) {
                // Closed by the test.
            }
        }

        private void serve(Socket socket) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                OutputStream out = socket.getOutputStream();
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    String[] request = line.trim().replaceFirst("^heos://", "").split("\\?", 2);
                    if ("system/heart_beat".equals(request[0])) {
                        heartbeats.incrementAndGet();
                    }
                    String message = request.length > 1 ? request[1] : "";
                    out.write(
                            ("{\"heos\": {\"command\": \"" + request[0] + "\", \"result\": \"success\", \"message\": \""
                                    + message + "\"}}\r\n").getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            } catch (IOException e) {
                // Closed by the binding or the test.
            }
        }

        @Override
        public void close() throws IOException {
            server.close();
            for (Socket socket : sockets) {
                socket.close();
            }
        }
    }
}
