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

import java.net.URI;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;

/**
 * Tests shared cloud webhook lease lifecycle and serialization.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioCloudWebhookRegistryTest {

    @Test
    public void concurrentAcquiresSharePendingRequest() throws Exception {
        TestWebhookService service = new TestWebhookService(true);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicReference<@Nullable Thread> firstWorker = new AtomicReference<>();
        AtomicReference<@Nullable Thread> secondWorker = new AtomicReference<>();
        Future<CloudWebhookLease> firstAcquire = executor.submit(() -> {
            firstWorker.set(Thread.currentThread());
            start.await();
            return registry.acquire("bridge-1");
        });
        Future<CloudWebhookLease> secondAcquire = executor.submit(() -> {
            secondWorker.set(Thread.currentThread());
            start.await();
            return registry.acquire("bridge-1");
        });

        try {
            start.countDown();
            assertTrue(service.firstRequest.await(1, TimeUnit.SECONDS));
            assertTrue(awaitTimedWaiting(firstWorker, secondWorker));

            service.completeFirstRequest();
            @Nullable
            CloudWebhookLease firstLease = firstAcquire.get(2, TimeUnit.SECONDS);
            @Nullable
            CloudWebhookLease secondLease = secondAcquire.get(2, TimeUnit.SECONDS);

            assertNotNull(firstLease);
            assertNotNull(secondLease);
            assertEquals(1, service.requestCount.get());
            assertEquals(1, registry.activeConsumerCount());
            assertEquals(firstLease.url(), secondLease.url());
            assertEquals(firstLease.generation(), secondLease.generation());
            assertEquals(firstLease.consumerLease(), secondLease.consumerLease());
        } finally {
            service.completeFirstRequest();
            executor.shutdownNow();
        }
    }

    @Test
    public void acquireWaitsForPendingRemoval() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease firstLease = registry.acquire("bridge-1");

        registry.release("bridge-1", firstLease.consumerLease());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch secondAcquireStarted = new CountDownLatch(1);
        try {
            Future<CloudWebhookLease> secondAcquire = executor.submit(() -> {
                secondAcquireStarted.countDown();
                return registry.acquire("bridge-2");
            });
            assertTrue(secondAcquireStarted.await(1, TimeUnit.SECONDS));
            assertFalse(service.secondRequest.await(200, TimeUnit.MILLISECONDS));

            service.removalGate.complete(Boolean.TRUE);
            @Nullable
            CloudWebhookLease secondLease = secondAcquire.get(2, TimeUnit.SECONDS);
            assertNotNull(secondLease);
            assertTrue(service.secondRequest.await(1, TimeUnit.SECONDS));
            assertEquals(2, service.requestCount.get());
            assertNotEquals(firstLease.generation(), secondLease.generation());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void staleConsumerLeaseCannotReleaseReplacement() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease firstLease = registry.acquire("bridge-1");
        registry.invalidateCachedWebhook(firstLease.generation());
        CloudWebhookLease replacementLease = registry.acquire("bridge-1");

        assertNotEquals(firstLease.generation(), replacementLease.generation());
        assertNotEquals(firstLease.consumerLease(), replacementLease.consumerLease());

        registry.release("bridge-1", firstLease.consumerLease());

        assertEquals(1, registry.activeConsumerCount());
        assertFalse(service.removeCalled.await(200, TimeUnit.MILLISECONDS));

        registry.release("bridge-1", replacementLease.consumerLease());
        assertTrue(service.removeCalled.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void providerReplacementRemovesOldWebhookBeforeRequestingNewOne() throws Exception {
        TestWebhookService oldService = new TestWebhookService();
        TestWebhookService newService = new TestWebhookService();
        AtomicReference<WebhookService> currentService = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(currentService::get);
        registry.acquire("bridge-1");

        currentService.set(newService);
        registry.onProviderChanged(oldService, newService);
        assertTrue(oldService.removeCalled.await(1, TimeUnit.SECONDS));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<CloudWebhookLease> acquire = executor.submit(() -> registry.acquire("bridge-2"));
            assertFalse(newService.firstRequest.await(200, TimeUnit.MILLISECONDS));

            oldService.removalGate.complete(Boolean.TRUE);
            assertNotNull(acquire.get(2, TimeUnit.SECONDS));
            assertTrue(newService.firstRequest.await(1, TimeUnit.SECONDS));
            assertEquals(1, newService.requestCount.get());
        } finally {
            oldService.removalGate.complete(Boolean.TRUE);
            executor.shutdownNow();
        }
    }

    @Test
    public void providerReplacementRemovesWebhookCreatedByAbandonedRequest() throws Exception {
        TestWebhookService oldService = new TestWebhookService(true);
        TestWebhookService newService = new TestWebhookService();
        AtomicReference<WebhookService> currentService = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(currentService::get);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<CloudWebhookLease> oldAcquire = executor.submit(() -> registry.acquire("bridge-1"));

        try {
            assertTrue(oldService.firstRequest.await(1, TimeUnit.SECONDS));
            currentService.set(newService);
            registry.onProviderChanged(oldService, newService);

            ExecutionException exception = assertThrows(ExecutionException.class,
                    () -> oldAcquire.get(2, TimeUnit.SECONDS));
            assertInstanceOf(RachioCloudWebhookRegistry.CloudWebhookException.class, exception.getCause());

            oldService.completeFirstRequest();
            assertTrue(oldService.removeCalled.await(1, TimeUnit.SECONDS));
        } finally {
            oldService.completeFirstRequest();
            oldService.removalGate.complete(Boolean.TRUE);
            executor.shutdownNow();
        }
    }

    @Test
    public void timedOutRequestDoesNotPoisonSubsequentAcquire() throws Exception {
        TestWebhookService service = new TestWebhookService(true);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service, 0);

        try {
            assertThrows(RachioCloudWebhookRegistry.CloudWebhookException.class, () -> registry.acquire("bridge-1"));

            CloudWebhookLease lease = registry.acquire("bridge-2");
            assertNotNull(lease);
            assertEquals(2, service.requestCount.get());
            assertEquals(1, registry.activeConsumerCount());
        } finally {
            service.completeFirstRequest();
        }
    }

    @Test
    public void timedOutRequestRemovesLateWebhookWhenUnused() throws Exception {
        TestWebhookService service = new TestWebhookService(true);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service, 0);

        try {
            assertThrows(RachioCloudWebhookRegistry.CloudWebhookException.class, () -> registry.acquire("bridge-1"));
            service.completeFirstRequest();

            assertTrue(service.removeCalled.await(1, TimeUnit.SECONDS));
        } finally {
            service.completeFirstRequest();
            service.removalGate.complete(Boolean.TRUE);
        }
    }

    private static class TestWebhookService implements WebhookService {
        private final AtomicInteger requestCount = new AtomicInteger();
        private final CountDownLatch firstRequest = new CountDownLatch(1);
        private final CountDownLatch secondRequest = new CountDownLatch(1);
        private final CountDownLatch removeCalled = new CountDownLatch(1);
        private final CompletableFuture<Boolean> removalGate = new CompletableFuture<>();
        private final CompletableFuture<Webhook> firstRequestGate = new CompletableFuture<>();
        private final boolean deferFirstRequest;

        private TestWebhookService() {
            this(false);
        }

        private TestWebhookService(boolean deferFirstRequest) {
            this.deferFirstRequest = deferFirstRequest;
        }

        @Override
        public CompletableFuture<Webhook> requestWebhook(String path) {
            int request = requestCount.incrementAndGet();
            if (request == 1) {
                firstRequest.countDown();
                if (deferFirstRequest) {
                    return firstRequestGate;
                }
            }
            if (request == 2) {
                secondRequest.countDown();
            }
            try {
                return CompletableFuture.completedFuture(webhook(request));
            } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        @Override
        public CompletableFuture<Void> removeWebhook(String path) {
            removeCalled.countDown();
            return removalGate.thenAccept(ignored -> {
            });
        }

        private void completeFirstRequest() {
            try {
                firstRequestGate.complete(webhook(1));
            } catch (Exception e) {
                firstRequestGate.completeExceptionally(e);
            }
        }

        private Webhook webhook(int request) throws Exception {
            return new Webhook(URI.create("https://example.test/webhook/" + request).toURL(),
                    Instant.now().plusSeconds(3600));
        }
    }

    private static boolean awaitTimedWaiting(AtomicReference<@Nullable Thread> firstWorker,
            AtomicReference<@Nullable Thread> secondWorker) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (System.nanoTime() < deadline) {
            @Nullable
            Thread first = firstWorker.get();
            @Nullable
            Thread second = secondWorker.get();
            if (first != null && second != null && first.getState() == Thread.State.TIMED_WAITING
                    && second.getState() == Thread.State.TIMED_WAITING) {
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }
}
