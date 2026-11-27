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
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookException;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;

/**
 * Tests shared Cloud registrations, independently owned acquisitions and operation-specific remote cleanup.
 *
 * @author Kovacs Istvan - Initial contribution
 * @author Kovacs Istvan - Cloud webhook recovery regression tests
 * @author Kovacs Istvan - Registration isolation and ownership regression tests
 */
@NonNullByDefault
public class RachioCloudWebhookRegistryTest {

    @Test
    public void concurrentAcquiresSharePendingRequest() throws Exception {
        TestWebhookService service = new TestWebhookService(1);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicReference<@Nullable Thread> firstWorker = new AtomicReference<>();
        AtomicReference<@Nullable Thread> secondWorker = new AtomicReference<>();
        try {
            Future<CloudWebhookLease> first = executor.submit(() -> {
                firstWorker.set(Thread.currentThread());
                return registry.acquire("bridge");
            });
            assertTrue(service.firstRequest.await(5, TimeUnit.SECONDS));
            Future<CloudWebhookLease> second = executor.submit(() -> {
                secondWorker.set(Thread.currentThread());
                return registry.acquire("bridge");
            });
            assertTrue(awaitTimedWaiting(firstWorker, secondWorker));
            service.completeRequest(1);
            CloudWebhookLease firstLease = first.get(5, TimeUnit.SECONDS);
            CloudWebhookLease secondLease = second.get(5, TimeUnit.SECONDS);
            assertEquals(1, service.requests.size());
            assertEquals(1, registry.activeConsumerCount());
            assertEquals(firstLease.url(), secondLease.url());
            assertEquals(firstLease.generation(), secondLease.generation());
            assertNotEquals(firstLease.consumerLease(), secondLease.consumerLease());
            registry.release("bridge", firstLease.consumerLease());
            assertEquals(0, service.removals.size());
            registry.release("bridge", secondLease.consumerLease());
            assertEquals(List.of(service.requestPath(1)), service.removedPaths());
        } finally {
            service.completeAll();
            stop(executor);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "bridge-1", "bridge-2" })
    public void interruptedAcquirePreservesOtherWaiter(String secondConsumer) throws Exception {
        TestWebhookService service = new TestWebhookService(1);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicReference<@Nullable Thread> firstWorker = new AtomicReference<>();
        AtomicReference<@Nullable Thread> secondWorker = new AtomicReference<>();
        try {
            Future<CloudWebhookLease> first = executor.submit(() -> {
                firstWorker.set(Thread.currentThread());
                return registry.acquire("bridge-1");
            });
            assertTrue(service.firstRequest.await(5, TimeUnit.SECONDS));
            Future<CloudWebhookLease> second = executor.submit(() -> {
                secondWorker.set(Thread.currentThread());
                return registry.acquire(secondConsumer);
            });
            assertTrue(awaitTimedWaiting(firstWorker, secondWorker));
            interrupt(firstWorker);
            ExecutionException failure = assertThrows(ExecutionException.class, () -> first.get(5, TimeUnit.SECONDS));
            assertInstanceOf(InterruptedException.class, failure.getCause());
            assertEquals(0, service.removals.size());
            service.completeRequest(1);
            CloudWebhookLease lease = second.get(5, TimeUnit.SECONDS);
            assertEquals(1, service.requests.size());
            assertEquals(1, registry.activeConsumerCount());
            assertTrue(service.livePaths.contains(service.requestPath(1)));
            registry.release(secondConsumer, lease.consumerLease());
        } finally {
            service.completeAll();
            stop(executor);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void abandonedLastWaiterDoesNotBlockReplacement(boolean timeOut) throws Exception {
        TestWebhookService service = new TestWebhookService(1);
        service.deferRemovals = true;
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service, timeOut ? 0 : 5000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<@Nullable Thread> worker = new AtomicReference<>();
        try {
            Future<CloudWebhookLease> abandoned = executor.submit(() -> {
                worker.set(Thread.currentThread());
                return registry.acquire("old");
            });
            assertTrue(service.firstRequest.await(5, TimeUnit.SECONDS));
            if (!timeOut) {
                assertTrue(awaitTimedWaiting(worker));
                interrupt(worker);
            }
            ExecutionException failure = assertThrows(ExecutionException.class,
                    () -> abandoned.get(5, TimeUnit.SECONDS));
            if (timeOut) {
                assertInstanceOf(CloudWebhookException.class, failure.getCause());
            } else {
                assertInstanceOf(InterruptedException.class, failure.getCause());
            }

            CloudWebhookLease replacement = registry.acquire("current");
            String currentPath = service.requestPath(2);
            assertNotEquals(service.requestPath(1), currentPath);
            assertTrue(service.livePaths.contains(currentPath));
            assertEquals(1, registry.activeConsumerCount());

            service.completeRequest(1);
            service.completeRemovals();
            assertTrue(service.livePaths.contains(currentPath));
            assertFalse(service.livePaths.contains(service.requestPath(1)));
            assertTrue(service.removedPaths().stream().allMatch(service.requestPath(1)::equals));
            registry.release("current", replacement.consumerLease());
        } finally {
            service.completeAll();
            stop(executor);
        }
    }

    @Test
    public void unfinishedOldProviderDoesNotBlockReplacement() throws Exception {
        TestWebhookService oldService = new TestWebhookService(1);
        TestWebhookService replacement = new TestWebhookService();
        AtomicReference<WebhookService> provider = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(provider::get, 0, 0);
        try {
            assertThrows(CloudWebhookException.class, () -> registry.acquire("bridge"));
            provider.set(replacement);
            registry.onProviderChanged(oldService, replacement);
            CloudWebhookLease current = registry.acquire("bridge");
            assertEquals(1, replacement.requests.size());
            assertEquals(1, registry.activeConsumerCount());
            oldService.completeRequest(1);
            assertTrue(replacement.livePaths.contains(replacement.requestPath(1)));
            assertFalse(oldService.livePaths.contains(oldService.requestPath(1)));
            registry.release("bridge", current.consumerLease());
        } finally {
            oldService.completeAll();
            replacement.completeAll();
        }
    }

    @Test
    public void pendingOldRemovalDoesNotDelayFreshRegistration() throws Exception {
        TestWebhookService service = new TestWebhookService();
        service.deferRemovals = true;
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service, 0, 0);
        try {
            CloudWebhookLease previous = registry.acquire("bridge");
            registry.release("bridge", previous.consumerLease());
            assertFalse(service.removals.getFirst().result().isDone());
            CloudWebhookLease current = registry.acquire("bridge");
            String currentPath = service.requestPath(2);
            assertNotEquals(service.requestPath(1), currentPath);
            service.completeRemovals();
            assertTrue(service.livePaths.contains(currentPath));
            assertFalse(service.livePaths.contains(service.requestPath(1)));
            registry.release("bridge", current.consumerLease());
        } finally {
            service.completeAll();
        }
    }

    @Test
    public void failedOldRemovalStillAllowsFreshRegistration() throws Exception {
        TestWebhookService service = new TestWebhookService();
        service.deferRemovals = true;
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        try {
            CloudWebhookLease previous = registry.acquire("bridge");
            registry.release("bridge", previous.consumerLease());
            service.removals.getFirst().gate().completeExceptionally(new IllegalStateException("controlled failure"));
            CloudWebhookLease current = registry.acquire("bridge");
            assertNotEquals(previous.url(), current.url());
            assertTrue(service.livePaths.contains(service.requestPath(2)));
            registry.release("bridge", current.consumerLease());
        } finally {
            service.completeAll();
        }
    }

    @Test
    public void lastOldConsumerReleaseCannotRemovePendingRefresh() throws Exception {
        TestWebhookService service = new TestWebhookService(2);
        service.deferRemovals = true;
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CloudWebhookLease first = registry.acquire("first");
            CloudWebhookLease second = registry.acquire("second");
            registry.invalidateCachedWebhook(first.generation());
            registry.release("first", first.consumerLease());
            Future<CloudWebhookLease> refresh = executor.submit(() -> registry.acquire("first"));
            assertTrue(service.secondRequest.await(5, TimeUnit.SECONDS));
            registry.release("second", second.consumerLease());
            assertEquals(List.of(service.requestPath(1)), service.removedPaths());

            service.completeRequest(2);
            CloudWebhookLease replacement = refresh.get(5, TimeUnit.SECONDS);
            service.completeRemovals();
            assertEquals(1, registry.activeConsumerCount());
            assertTrue(service.livePaths.contains(service.requestPath(2)));
            assertFalse(service.livePaths.contains(service.requestPath(1)));
            assertEquals(replacement.url(), "https://example.test" + service.requestPath(2));
            registry.release("first", replacement.consumerLease());
        } finally {
            service.completeAll();
            stop(executor);
        }
    }

    @Test
    public void sameConsumerAcquisitionsHaveIndependentOwnership() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease first = registry.acquire("bridge");
        CloudWebhookLease second = registry.acquire("bridge");
        assertEquals(first.url(), second.url());
        assertNotEquals(first.consumerLease(), second.consumerLease());
        registry.release("bridge", first.consumerLease());
        registry.release("bridge", first.consumerLease());
        assertEquals(1, registry.activeConsumerCount());
        assertTrue(service.livePaths.contains(service.requestPath(1)));
        assertEquals(0, service.removals.size());
        registry.release("bridge", second.consumerLease());
        assertEquals(0, registry.activeConsumerCount());
        assertEquals(List.of(service.requestPath(1)), service.removedPaths());
    }

    @Test
    public void staleConsumerLeaseCannotReleaseReplacement() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease previous = registry.acquire("bridge");
        registry.invalidateCachedWebhook(previous.generation());
        CloudWebhookLease current = registry.acquire("bridge");
        registry.release("bridge", previous.consumerLease());
        assertEquals(1, registry.activeConsumerCount());
        assertEquals(List.of(service.requestPath(1)), service.removedPaths());
        assertTrue(service.livePaths.contains(service.requestPath(2)));
        registry.release("bridge", current.consumerLease());
        assertEquals(0, registry.activeConsumerCount());
    }

    @Test
    public void otherConsumerKeepsRetiredRegistrationUntilReleased() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease first = registry.acquire("first");
        CloudWebhookLease second = registry.acquire("second");
        registry.invalidateCachedWebhook(first.generation());
        CloudWebhookLease replacement = registry.acquire("first");
        registry.release("first", first.consumerLease());
        assertTrue(service.livePaths.contains(service.requestPath(1)));
        assertTrue(service.livePaths.contains(service.requestPath(2)));
        registry.release("second", second.consumerLease());
        assertFalse(service.livePaths.contains(service.requestPath(1)));
        assertTrue(service.livePaths.contains(service.requestPath(2)));
        assertEquals(1, registry.activeConsumerCount());
        registry.release("first", replacement.consumerLease());
    }

    @Test
    public void staleInvalidationDoesNotRetireReplacement() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease old = registry.acquire("first");
        registry.invalidateCachedWebhook(old.generation());
        CloudWebhookLease replacement = registry.acquire("first");
        registry.invalidateCachedWebhook(old.generation());
        CloudWebhookLease other = registry.acquire("second");
        assertEquals(2, service.requests.size());
        assertEquals(replacement.url(), other.url());
        assertEquals(replacement.generation(), other.generation());
        registry.release("first", old.consumerLease());
        registry.release("first", replacement.consumerLease());
        assertTrue(service.livePaths.contains(service.requestPath(2)));
        registry.release("second", other.consumerLease());
    }

    @Test
    public void providerReplacementInvalidatesOldTokensOnly() throws Exception {
        TestWebhookService oldService = new TestWebhookService();
        TestWebhookService replacement = new TestWebhookService();
        AtomicReference<WebhookService> provider = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(provider::get);
        CloudWebhookLease previous = registry.acquire("bridge");
        provider.set(replacement);
        registry.onProviderChanged(oldService, replacement);
        assertFalse(registry.isLeaseActive("bridge", previous.consumerLease()));
        CloudWebhookLease current = registry.acquire("bridge");
        assertTrue(registry.isLeaseActive("bridge", current.consumerLease()));
        assertFalse(registry.isLeaseActive("other", current.consumerLease()));
        registry.release("bridge", previous.consumerLease());
        assertEquals(1, registry.activeConsumerCount());
        assertTrue(replacement.livePaths.contains(replacement.requestPath(1)));
        registry.release("bridge", current.consumerLease());
        oldService.completeAll();
        replacement.completeAll();
    }

    @Test
    public void providerRemovalFailsPendingWaitersPromptly() throws Exception {
        TestWebhookService service = new TestWebhookService(1);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<CloudWebhookLease> acquire = executor.submit(() -> registry.acquire("bridge"));
            assertTrue(service.firstRequest.await(5, TimeUnit.SECONDS));
            registry.onProviderChanged(service, null);
            ExecutionException failure = assertThrows(ExecutionException.class, () -> acquire.get(5, TimeUnit.SECONDS));
            assertInstanceOf(CloudWebhookException.class, failure.getCause());
            assertThrows(CloudWebhookException.class, () -> registry.acquire("bridge"));
            service.completeRequest(1);
            assertFalse(service.livePaths.contains(service.requestPath(1)));
            assertEquals(0, registry.activeConsumerCount());
        } finally {
            service.completeAll();
            stop(executor);
        }
    }

    @Test
    public void returningProviderDoesNotAdoptItsOldPendingOperation() throws Exception {
        TestWebhookService firstProvider = new TestWebhookService(1);
        TestWebhookService intermediate = new TestWebhookService();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> firstProvider);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<CloudWebhookLease> previous = executor.submit(() -> registry.acquire("bridge"));
            assertTrue(firstProvider.firstRequest.await(5, TimeUnit.SECONDS));
            registry.onProviderChanged(firstProvider, intermediate);
            assertThrows(ExecutionException.class, () -> previous.get(5, TimeUnit.SECONDS));
            CloudWebhookLease middle = registry.acquire("bridge");
            registry.onProviderChanged(intermediate, firstProvider);
            CloudWebhookLease current = registry.acquire("bridge");
            firstProvider.completeRequest(1);
            registry.release("bridge", middle.consumerLease());
            assertEquals(2, firstProvider.requests.size());
            assertFalse(firstProvider.livePaths.contains(firstProvider.requestPath(1)));
            assertTrue(firstProvider.livePaths.contains(firstProvider.requestPath(2)));
            assertTrue(registry.isLeaseActive("bridge", current.consumerLease()));
            registry.release("bridge", current.consumerLease());
        } finally {
            firstProvider.completeAll();
            intermediate.completeAll();
            stop(executor);
        }
    }

    @Test
    public void failedRequestFutureDoesNotPoisonSubsequentAcquire() throws Exception {
        TestWebhookService service = new TestWebhookService(1);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<CloudWebhookLease> failed = executor.submit(() -> registry.acquire("bridge"));
            assertTrue(service.firstRequest.await(5, TimeUnit.SECONDS));
            service.requests.getFirst().future().completeExceptionally(new IllegalStateException("controlled failure"));
            assertThrows(ExecutionException.class, () -> failed.get(5, TimeUnit.SECONDS));
            CloudWebhookLease current = registry.acquire("bridge");
            assertEquals(1, registry.activeConsumerCount());
            assertTrue(service.livePaths.contains(service.requestPath(2)));
            registry.release("bridge", current.consumerLease());
        } finally {
            service.completeAll();
            stop(executor);
        }
    }

    @Test
    public void abandonedRefreshCleansOnlyItsPathWhileOldConsumersRemainActive() throws Exception {
        TestWebhookService service = new TestWebhookService(2);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service, 0);
        CloudWebhookLease first = registry.acquire("first");
        CloudWebhookLease second = registry.acquire("second");
        registry.invalidateCachedWebhook(first.generation());
        try {
            assertThrows(CloudWebhookException.class, () -> registry.acquire("first"));
            service.completeRequest(2);
            assertTrue(service.livePaths.contains(service.requestPath(1)));
            assertFalse(service.livePaths.contains(service.requestPath(2)));
            assertTrue(service.removedPaths().stream().allMatch(service.requestPath(2)::equals));
            assertEquals(2, registry.activeConsumerCount());
            registry.release("first", first.consumerLease());
            assertTrue(service.livePaths.contains(service.requestPath(1)));
            registry.release("second", second.consumerLease());
            assertFalse(service.livePaths.contains(service.requestPath(1)));
        } finally {
            service.completeAll();
        }
    }

    @Test
    public void recreatedRegistryUsesIndependentRemotePath() throws Exception {
        TestWebhookService service = new TestWebhookService();
        RachioCloudWebhookRegistry firstRegistry = new RachioCloudWebhookRegistry(() -> service);
        RachioCloudWebhookRegistry secondRegistry = new RachioCloudWebhookRegistry(() -> service);
        CloudWebhookLease first = firstRegistry.acquire("bridge");
        CloudWebhookLease second = secondRegistry.acquire("bridge");
        String firstPath = service.requestPath(1);
        String secondPath = service.requestPath(2);
        assertTrue(firstPath.matches(SERVLET_WEBHOOK_PATH + "/cloud/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}"));
        assertNotEquals(firstPath, secondPath);
        firstRegistry.release("bridge", first.consumerLease());
        assertFalse(service.livePaths.contains(firstPath));
        assertTrue(service.livePaths.contains(secondPath));
        secondRegistry.release("bridge", second.consumerLease());
    }

    @Test
    public void cleanupDeadlineDoesNotCompleteProviderOwnedFuture() throws Exception {
        TestWebhookService service = new TestWebhookService();
        service.deferRemovals = true;
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service, 0, 0);
        try {
            CloudWebhookLease first = registry.acquire("bridge");
            registry.release("bridge", first.consumerLease());
            CloudWebhookLease current = registry.acquire("bridge");
            assertFalse(service.removals.getFirst().result().isDone());
            service.completeRemovals();
            assertTrue(service.livePaths.contains(service.requestPath(2)));
            registry.release("bridge", current.consumerLease());
        } finally {
            service.completeAll();
        }
    }

    private static final class TestWebhookService implements WebhookService {
        private final List<Request> requests = new CopyOnWriteArrayList<>();
        private final List<Removal> removals = new CopyOnWriteArrayList<>();
        private final Set<String> livePaths = ConcurrentHashMap.newKeySet();
        private final Set<Integer> deferredRequests;
        private final CountDownLatch firstRequest = new CountDownLatch(1);
        private final CountDownLatch secondRequest = new CountDownLatch(1);
        private final AtomicInteger requestCount = new AtomicInteger();
        private volatile boolean deferRemovals;

        private TestWebhookService(Integer... deferredRequests) {
            this.deferredRequests = Set.of(deferredRequests);
        }

        @Override
        public CompletableFuture<Webhook> requestWebhook(String path) {
            int number = requestCount.incrementAndGet();
            requests.add(new Request(path, new CompletableFuture<>()));
            if (number == 1) {
                firstRequest.countDown();
            }
            if (number == 2) {
                secondRequest.countDown();
            }
            if (!deferredRequests.contains(number)) {
                completeRequest(number);
            }
            return requests.get(number - 1).future();
        }

        @Override
        public CompletableFuture<Void> removeWebhook(String path) {
            assertTrue(requests.stream().anyMatch(request -> request.path().equals(path)),
                    "Cleanup must name the exact path that was requested");
            CompletableFuture<Boolean> gate = new CompletableFuture<>();
            CompletableFuture<Void> result = gate.thenAccept(ignored -> livePaths.remove(path));
            removals.add(new Removal(path, gate, result));
            if (!deferRemovals) {
                gate.complete(Boolean.TRUE);
            }
            return result;
        }

        private String requestPath(int number) {
            return requests.get(number - 1).path();
        }

        private List<String> removedPaths() {
            return removals.stream().map(Removal::path).toList();
        }

        private void completeRequest(int number) {
            Request request = requests.get(number - 1);
            if (!request.future().isDone()) {
                livePaths.add(request.path());
                try {
                    request.future().complete(new Webhook(URI.create("https://example.test" + request.path()).toURL(),
                            Instant.now().plusSeconds(86400)));
                } catch (Exception e) {
                    request.future().completeExceptionally(e);
                }
            }
        }

        private void completeRemovals() {
            removals.forEach(removal -> removal.gate().complete(Boolean.TRUE));
        }

        private void completeAll() {
            for (int number = 1; number <= requests.size(); number++) {
                completeRequest(number);
            }
            completeRemovals();
        }

        private record Request(String path, CompletableFuture<Webhook> future) {
        }

        private record Removal(String path, CompletableFuture<Boolean> gate, CompletableFuture<Void> result) {
        }
    }

    @SafeVarargs
    private static boolean awaitTimedWaiting(AtomicReference<@Nullable Thread>... workers) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            boolean allWaiting = true;
            for (AtomicReference<@Nullable Thread> worker : workers) {
                Thread thread = worker.get();
                if (thread == null || thread.getState() != Thread.State.TIMED_WAITING) {
                    allWaiting = false;
                    break;
                }
            }
            if (allWaiting) {
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }

    private static void interrupt(AtomicReference<@Nullable Thread> worker) {
        Thread thread = worker.get();
        assertNotNull(thread);
        thread.interrupt();
    }

    private static void stop(ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }
}
