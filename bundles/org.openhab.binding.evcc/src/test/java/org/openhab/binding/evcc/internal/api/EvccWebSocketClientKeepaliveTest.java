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
package org.openhab.binding.evcc.internal.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import com.google.gson.JsonObject;

/**
 * Tests the connection supervision of {@link EvccWebSocketClient}: evcc only sends messages once per control cycle,
 * so a silent connection must be kept open as long as it answers WebSocket pings.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class EvccWebSocketClientKeepaliveTest {

    /** Minimal deterministic scheduler backed by a virtual clock. */
    private static class VirtualScheduler {
        private final ScheduledExecutorService executor = mock(ScheduledExecutorService.class);
        private final List<Task> tasks = new ArrayList<>();
        private long nowMillis;

        private static class Task {
            final Runnable runnable;
            final long periodMillis;
            long dueMillis;

            Task(Runnable runnable, long dueMillis, long periodMillis) {
                this.runnable = runnable;
                this.dueMillis = dueMillis;
                this.periodMillis = periodMillis;
            }
        }

        VirtualScheduler() {
            when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                    .thenAnswer(invocation -> add(invocation, 0));
            when(executor.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenAnswer(invocation -> add(invocation, toMillis(invocation, 2)));
        }

        private static long toMillis(InvocationOnMock invocation, int durationIndex) {
            TimeUnit unit = invocation.getArgument(invocation.getArguments().length - 1);
            return unit.toMillis(invocation.<Long> getArgument(durationIndex));
        }

        private ScheduledFuture<?> add(InvocationOnMock invocation, long periodMillis) {
            Task task = new Task(invocation.getArgument(0), nowMillis + toMillis(invocation, 1), periodMillis);
            tasks.add(task);
            ScheduledFuture<?> future = mock(ScheduledFuture.class);
            when(future.cancel(anyBoolean())).thenAnswer(cancel -> tasks.remove(task));
            when(future.isDone()).thenAnswer(isDone -> !tasks.contains(task));
            return future;
        }

        /** Runs all tasks that become due while advancing the virtual clock by one second. */
        void advanceOneSecond() {
            long end = nowMillis + 1000;
            while (true) {
                Task next = tasks.stream().filter(task -> task.dueMillis <= end)
                        .min(Comparator.comparingLong(task -> task.dueMillis)).orElse(null);
                if (next == null) {
                    break;
                }
                nowMillis = next.dueMillis;
                if (next.periodMillis > 0) {
                    next.dueMillis += next.periodMillis;
                } else {
                    tasks.remove(next);
                }
                next.runnable.run();
            }
            nowMillis = end;
        }
    }

    private final VirtualScheduler scheduler = new VirtualScheduler();
    private final Session session = mock(Session.class);
    private final RemoteEndpoint remote = mock(RemoteEndpoint.class);
    private EvccWebSocketClient client = createClient();
    private boolean evccAnswersPings = true;
    private boolean sessionClosed;

    private EvccWebSocketClient createClient() {
        return new EvccWebSocketClient("ws://evcc/ws", scheduler.executor, state -> {
        }, delta -> {
        }, () -> {
        }, () -> {
        });
    }

    @BeforeEach
    void setUp() throws Exception {
        client = createClient();
        when(session.isOpen()).thenAnswer(invocation -> !sessionClosed);
        when(session.getRemote()).thenReturn(remote);
        doAnswer(invocation -> {
            sessionClosed = true;
            return null;
        }).when(session).close();
        doAnswer(invocation -> {
            if (evccAnswersPings) {
                client.handlePong();
            }
            return null;
        }).when(remote).sendPing(any(ByteBuffer.class));

        client.handleConnected(session);
        JsonObject welcome = new JsonObject();
        welcome.addProperty("interval", 60);
        client.handleMessage(welcome);
    }

    @AfterEach
    void tearDown() {
        client.stop();
    }

    private void waitSeconds(int seconds) {
        for (int i = 0; i < seconds && !sessionClosed; i++) {
            scheduler.advanceOneSecond();
        }
    }

    @Test
    void silentConnectionIsKeptOpenWhileEvccAnswersPings() throws Exception {
        // evcc with "interval: 60s" sends nothing between two control cycles
        waitSeconds(300);

        assertFalse(sessionClosed);
        verify(remote, atLeastOnce()).sendPing(any(ByteBuffer.class));
    }

    @Test
    void connectionIsReconnectedWhenPingsAreNotAnswered() {
        evccAnswersPings = false;

        waitSeconds(300);

        assertTrue(sessionClosed);
        verify(scheduler.executor).schedule(any(Runnable.class), eq(10L), eq(TimeUnit.SECONDS));
    }

    @Test
    void incomingMessagesKeepTheConnectionOpenWithoutPongs() {
        evccAnswersPings = false;

        for (int cycle = 0; cycle < 30; cycle++) {
            waitSeconds(10);
            JsonObject delta = new JsonObject();
            delta.addProperty("homePower", cycle);
            client.handleMessage(delta);
        }

        assertFalse(sessionClosed);
    }
}
