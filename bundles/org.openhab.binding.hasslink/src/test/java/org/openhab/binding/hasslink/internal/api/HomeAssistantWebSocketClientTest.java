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
package org.openhab.binding.hasslink.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

/**
 * Tests outgoing command sequencing for the Home Assistant WebSocket client.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
class HomeAssistantWebSocketClientTest {

    private static final long TEST_TIMEOUT_SECONDS = 5;

    @Test
    void concurrentCommandsAreSentInIncreasingIdOrder() throws Exception {
        // 1. SETUP: Mock WebSocket session and client
        WebSocketClient wsClient = mock(WebSocketClient.class);
        HomeAssistantConnectionListener listener = mock(HomeAssistantConnectionListener.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        HomeAssistantWebSocketClient client = new HomeAssistantWebSocketClient(wsClient, "token", listener, scheduler);
        Session session = mock(Session.class);
        RemoteEndpoint remote = mock(RemoteEndpoint.class);
        when(session.getRemote()).thenReturn(remote);
        client.onWebSocketConnect(session);

        CountDownLatch firstSerializationStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstSerialization = new CountDownLatch(1);
        CountDownLatch secondCallStarted = new CountDownLatch(1);
        List<String> sentMessages = new CopyOnWriteArrayList<>();
        AtomicReference<Thread> firstCallingThread = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        // Create a custom map that acts as a synchronization trap.
        // When Gson serializes this map during the first command, it triggers entrySet(),
        // allowing us to freeze Thread 1 mid-execution to force an overlap with Thread 2.
        Map<String, Object> blockingData = new AbstractMap<>() {
            @Override
            public Set<Entry<String, Object>> entrySet() {
                if (Objects.equals(Thread.currentThread(), firstCallingThread.get())) {
                    firstSerializationStarted.countDown();
                    try {
                        if (!releaseFirstSerialization.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                            throw new AssertionError("Timed out waiting to release first command serialization");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError("Interrupted while waiting to release serialization", e);
                    }
                }
                return Map.<String, Object> of("value", 1).entrySet();
            }
        };
        doAnswer(invocation -> {
            sentMessages.add(invocation.getArgument(0));
            return null;
        }).when(remote).sendString(anyString());

        // 2. TRAP THREAD 1: Fire the first command which will freeze inside
        // Gson serialization due to our custom blockingData map.
        Thread first = new Thread(() -> {
            firstCallingThread.set(Thread.currentThread());
            try {
                client.callService("test", "first", null, blockingData).get(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Throwable e) {
                failure.compareAndSet(null, e);
            }
        });
        Thread second = new Thread(() -> {
            secondCallStarted.countDown();
            try {
                client.callService("test", "second", null, Map.of()).get(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (Throwable e) {
                failure.compareAndSet(null, e);
            }
        });

        // 3. VERIFY THREAD 2 BLOCKS: Wait for Thread 1 to get stuck, start Thread 2,
        // and assert that Thread 2's state is genuinely BLOCKED by the client's lock.
        first.start();
        try {
            assertTrue(firstSerializationStarted.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            second.start();
            assertTrue(secondCallStarted.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(Thread.State.BLOCKED, awaitThreadState(second, Thread.State.BLOCKED, Thread.State.TERMINATED),
                    "The second command should wait while the first command is being serialized and sent");
        } finally {
            releaseFirstSerialization.countDown();
            first.join(TimeUnit.SECONDS.toMillis(TEST_TIMEOUT_SECONDS));
            if (second.getState() != Thread.State.NEW) {
                second.join(TimeUnit.SECONDS.toMillis(TEST_TIMEOUT_SECONDS));
            }
        }

        assertFalse(first.isAlive(), "First command thread did not finish");
        assertFalse(second.isAlive(), "Second command thread did not finish");
        assertNull(failure.get());
        assertEquals(List.of(1, 2), sentMessages.stream()
                .map(message -> JsonParser.parseString(message).getAsJsonObject().get("id").getAsInt()).toList());
    }

    private Thread.State awaitThreadState(Thread thread, Thread.State expected, Thread.State alternative) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TEST_TIMEOUT_SECONDS);
        Thread.State state;
        do {
            state = thread.getState();
            if (state == expected || state == alternative) {
                return state;
            }
            Thread.yield();
        } while (System.nanoTime() < deadline);
        return state;
    }
}
