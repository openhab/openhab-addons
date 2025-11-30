/**
 * Copyright (C) 2010-2025 openHAB.org and the original author(s)
 *
 * See the NOTICE file(s) distributed with this work for additional information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrik Gfeller - Initial contribution
 */
package org.openhab.binding.jellyfin.internal.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;

/**
 * Unit tests for {@link SessionEventBus}.
 *
 * @author Patrik Gfeller - Initial contribution
 */
class SessionEventBusTest {
    @Test
    void subscribePublishUnsubscribe_basicFlow() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger calls = new AtomicInteger(0);

        SessionEventListener l = (SessionInfoDto s) -> calls.incrementAndGet();

        bus.subscribe("device-1", l);
        assertEquals(1, bus.getListenerCount("device-1"));

        bus.publishSessionUpdate("device-1", new SessionInfoDto());
        bus.publishSessionUpdate("device-1", null);
        assertEquals(2, calls.get());

        bus.unsubscribe("device-1", l);
        assertEquals(0, bus.getListenerCount("device-1"));
    }

    @Test
    void multipleListeners_receiveIndependentNotifications() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger calls1 = new AtomicInteger(0);
        AtomicInteger calls2 = new AtomicInteger(0);

        SessionEventListener l1 = (SessionInfoDto s) -> calls1.incrementAndGet();
        SessionEventListener l2 = (SessionInfoDto s) -> calls2.incrementAndGet();

        bus.subscribe("device-1", l1);
        bus.subscribe("device-1", l2);
        assertEquals(2, bus.getListenerCount("device-1"));

        bus.publishSessionUpdate("device-1", new SessionInfoDto());
        assertEquals(1, calls1.get());
        assertEquals(1, calls2.get());

        bus.unsubscribe("device-1", l1);
        bus.publishSessionUpdate("device-1", null);
        assertEquals(1, calls1.get()); // No change
        assertEquals(2, calls2.get()); // Incremented
    }

    @Test
    void exceptionInListener_doesNotBlockOtherListeners() {
        SessionEventBus bus = new SessionEventBus();
        AtomicInteger successfulCalls = new AtomicInteger(0);

        SessionEventListener throwingListener = (SessionInfoDto s) -> {
            throw new RuntimeException("Test exception");
        };
        SessionEventListener successListener = (SessionInfoDto s) -> successfulCalls.incrementAndGet();

        bus.subscribe("device-1", throwingListener);
        bus.subscribe("device-1", successListener);

        // Exception in first listener should not prevent second listener from executing
        bus.publishSessionUpdate("device-1", new SessionInfoDto());
        assertEquals(1, successfulCalls.get());
    }

    @Test
    void concurrentSubscribeUnsubscribe_threadSafe() throws InterruptedException {
        SessionEventBus bus = new SessionEventBus();
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        SessionEventListener listener = (SessionInfoDto s) -> {
                        };
                        String deviceId = "device-" + (threadId % 3); // Use 3 devices
                        bus.subscribe(deviceId, listener);
                        bus.unsubscribe(deviceId, listener);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // After all operations, listener counts should be consistent
        assertEquals(0, bus.getTotalListenerCount());
    }

    @Test
    void concurrentPublish_threadSafe() throws InterruptedException {
        SessionEventBus bus = new SessionEventBus();
        List<Integer> receivedEvents = Collections.synchronizedList(new ArrayList<>());
        int threadCount = 10;
        int eventsPerThread = 50;

        SessionEventListener listener = (SessionInfoDto s) -> receivedEvents.add(1);
        bus.subscribe("device-1", listener);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < eventsPerThread; j++) {
                        bus.publishSessionUpdate("device-1", new SessionInfoDto());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        // All events should have been delivered
        assertEquals(threadCount * eventsPerThread, receivedEvents.size());
    }

    @Test
    void clear_removesAllListeners() {
        SessionEventBus bus = new SessionEventBus();
        SessionEventListener l1 = (SessionInfoDto s) -> {
        };
        SessionEventListener l2 = (SessionInfoDto s) -> {
        };

        bus.subscribe("device-1", l1);
        bus.subscribe("device-2", l2);
        assertEquals(2, bus.getTotalListenerCount());

        bus.clear();
        assertEquals(0, bus.getTotalListenerCount());
        assertEquals(0, bus.getListenerCount("device-1"));
        assertEquals(0, bus.getListenerCount("device-2"));
    }

    @Test
    void publishToNonexistentDevice_doesNotThrow() {
        SessionEventBus bus = new SessionEventBus();
        // Should not throw exception
        bus.publishSessionUpdate("nonexistent-device", new SessionInfoDto());
        bus.publishSessionUpdate("nonexistent-device", null);
    }

    @Test
    void unsubscribeNonexistentListener_doesNotThrow() {
        SessionEventBus bus = new SessionEventBus();
        SessionEventListener listener = (SessionInfoDto s) -> {
        };

        // Should not throw exception
        bus.unsubscribe("nonexistent-device", listener);

        bus.subscribe("device-1", listener);
        bus.unsubscribe("device-1", listener);
        // Unsubscribing again should not throw
        bus.unsubscribe("device-1", listener);
    }

    @Test
    void nullSessionUpdate_deliveredToListeners() {
        SessionEventBus bus = new SessionEventBus();
        List<SessionInfoDto> receivedSessions = new ArrayList<>();

        SessionEventListener listener = (SessionInfoDto s) -> receivedSessions.add(s);
        bus.subscribe("device-1", listener);

        bus.publishSessionUpdate("device-1", null);
        assertEquals(1, receivedSessions.size());
        assertEquals(null, receivedSessions.get(0));
    }
}
