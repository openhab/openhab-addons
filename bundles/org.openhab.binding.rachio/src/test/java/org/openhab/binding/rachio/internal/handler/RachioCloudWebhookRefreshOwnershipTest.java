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
package org.openhab.binding.rachio.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests ownership of a refresh timer when concurrent acquisitions publish different Cloud generations.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
@Timeout(15)
class RachioCloudWebhookRefreshOwnershipTest {
    @Test
    void earlierAcquisitionCannotReplaceNewerRefreshTimer() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        WebhookService provider = mock(WebhookService.class);
        when(provider.requestWebhook(anyString())).thenAnswer(invocation -> CompletableFuture.completedFuture(
                new Webhook(URI.create("https://example.test/operation-" + requests.incrementAndGet()).toURL(),
                        Instant.now().plusSeconds(86400))));
        when(provider.removeWebhook(anyString())).thenReturn(CompletableFuture.completedFuture(null));
        RachioCloudWebhookRegistry registry = spy(new RachioCloudWebhookRegistry(() -> provider));
        CloudWebhookLease otherBridge = registry.acquire("other-bridge");
        CountDownLatch firstAcquired = new CountDownLatch(1);
        CountDownLatch secondAcquired = new CountDownLatch(1);
        CountDownLatch allowFirstAcquire = new CountDownLatch(1);
        CountDownLatch allowSecondAcquire = new CountDownLatch(1);
        CountDownLatch firstPublished = new CountDownLatch(1);
        CountDownLatch allowFirstSchedule = new CountDownLatch(1);
        doAnswer(invocation -> {
            CloudWebhookLease lease = (CloudWebhookLease) invocation.callRealMethod();
            if (Thread.currentThread().getName().equals("first-acquire")) {
                firstAcquired.countDown();
                await(allowFirstAcquire);
            } else if (Thread.currentThread().getName().equals("second-acquire")) {
                secondAcquired.countDown();
                await(allowSecondAcquire);
            }
            return lease;
        }).when(registry).acquire(anyString());
        doAnswer(invocation -> {
            long lease = invocation.getArgument(1);
            if (Thread.currentThread().getName().equals("first-acquire") && lease == -1) {
                firstPublished.countDown();
                await(allowFirstSchedule);
            }
            return invocation.callRealMethod();
        }).when(registry).release(anyString(), anyLong());
        List<Runnable> timerTasks = new CopyOnWriteArrayList<>();
        List<ScheduledFuture<?>> timers = new CopyOnWriteArrayList<>();
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        List<Runnable> reconciliations = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            reconciliations.add(invocation.getArgument(0));
            return null;
        }).when(scheduler).execute(any(Runnable.class));
        when(scheduler.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
            timerTasks.add(invocation.getArgument(0));
            ScheduledFuture<?> timer = mock(ScheduledFuture.class);
            timers.add(timer);
            return timer;
        });
        RachioBridgeHandler bridge = new RachioBridgeHandler(
                BridgeBuilder.create(THING_TYPE_CLOUD, "refresh-timer-review").build(), mock(HttpClient.class),
                registry);
        bridge.setCallback(mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        RachioApi api = mock(RachioApi.class);
        when(api.fillProperties()).thenReturn(new HashMap<>());
        setField(bridge, "thingConfig", config);
        setField(bridge, "rachioApi", api);
        setField(bridge, "apiReady", true);
        setField(bridge, "webhookCoordinator", new RachioWebhookCoordinator(registry, scheduler));
        AtomicInteger workerNames = new AtomicInteger();
        ExecutorService workers = Executors.newFixedThreadPool(2, runnable -> new Thread(runnable,
                workerNames.getAndIncrement() == 0 ? "first-acquire" : "second-acquire"));
        try {
            Future<String> first = workers.submit(bridge::getModernWebhookUrlForRegistration);
            await(firstAcquired);
            registry.invalidateCachedWebhook(otherBridge.generation());
            Future<String> second = workers.submit(bridge::getModernWebhookUrlForRegistration);
            await(secondAcquired);
            allowFirstAcquire.countDown();
            await(firstPublished);
            allowSecondAcquire.countDown();
            String currentUrl = second.get(5, TimeUnit.SECONDS);
            require(timers.size() == 1, "Second operation must install its refresh first");
            allowFirstSchedule.countDown();
            first.get(5, TimeUnit.SECONDS);
            assertEquals(1, timers.size(), "Older acquisition must not replace the newer refresh timer");
            verify(timers.getFirst(), never()).cancel(anyBoolean());
            assertEquals(currentUrl, bridge.getModernWebhookUrlForRegistration());
            timerTasks.getFirst().run();
            assertEquals(1, reconciliations.size(), "Current timer must still request URL renewal");
            assertEquals(2, requests.get());
        } finally {
            allowFirstAcquire.countDown();
            allowSecondAcquire.countDown();
            allowFirstSchedule.countDown();
            workers.shutdownNow();
            require(workers.awaitTermination(5, TimeUnit.SECONDS), "Workers must stop");
            bridge.dispose();
            registry.release("other-bridge", otherBridge.consumerLease());
        }
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        require(latch.await(5, TimeUnit.SECONDS), "Synchronization timed out");
    }

    private static void require(boolean condition, String message) {
        assertTrue(condition, message);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }
}
