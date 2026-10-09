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
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.ScanListener;

/**
 * Manual discovery, stable results and cancellation across bounded concurrent scans.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Timeout(20)
class ApplianceDiscoveryServiceTest {
    private static final String IDENTITY = "12345678-1234-5678-9abc-123456789abc";
    private final ScheduledExecutorService control = executor(1);
    private final ScheduledExecutorService workers = executor(8);
    private final List<DiscoveryResult> results = new CopyOnWriteArrayList<>();
    private @Nullable ApplianceDiscoveryService service;

    private static ScheduledExecutorService executor(int threads) {
        return Executors.newScheduledThreadPool(threads, task -> {
            Thread thread = new Thread(task, "appliance-discovery-test");
            thread.setDaemon(true);
            return thread;
        });
    }

    @AfterEach
    void dispose() throws Exception {
        ApplianceDiscoveryService current = service;
        if (current != null) {
            current.deactivate();
        } else {
            control.shutdown();
        }
        workers.shutdownNow();
        assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(control.awaitTermination(5, TimeUnit.SECONDS));
    }

    private ApplianceDiscoveryService create(Map<String, Object> properties, ApplianceDiscoveryService.Probe probe)
            throws Exception {
        return create(properties, probe, ApplianceDiscoveryService.SCAN_TIMEOUT_SECONDS);
    }

    private ApplianceDiscoveryService create(Map<String, Object> properties, ApplianceDiscoveryService.Probe probe,
            int timeoutSeconds) throws Exception {
        var current = new ApplianceDiscoveryService(control, workers, probe) {
            @Override
            public int getScanTimeout() {
                return timeoutSeconds;
            }
        };
        service = current;
        var listener = mock(DiscoveryListener.class);
        doAnswer(invocation -> {
            results.add(invocation.getArgument(1));
            return null;
        }).when(listener).thingDiscovered(eq(current), any());
        current.addDiscoveryListener(listener);
        current.activate(properties);
        drain();
        return current;
    }

    private void drain() throws Exception {
        control.submit(() -> {
        }).get(5, TimeUnit.SECONDS);
    }

    private static Discovery.Descriptor descriptor(String identity) {
        return new Discovery.Descriptor(identity, "Samsung Appliance", 49155);
    }

    @Test
    void configurationNeverStartsBackgroundScanningAndInvalidConfigurationDoesNotProbe() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        var current = create(Map.of("discoverySubnet", "10.0.0.50/32", "background", true), (host, timeout) -> {
            calls.incrementAndGet();
            return descriptor(IDENTITY);
        });
        assertFalse(current.isBackgroundDiscoveryEnabled());
        assertEquals(0, calls.get());
        for (Map<String, Object> configuration : List.of(Map.<String, Object> of(),
                Map.<String, Object> of("discoverySubnet", ""),
                Map.<String, Object> of("discoverySubnet", "10.0.0.0/23"),
                Map.<String, Object> of("discoverySubnet", 42))) {
            current.modified(configuration);
            Completion completion = new Completion();
            current.startScan(completion);
            assertTrue(completion.finished.get(5, TimeUnit.SECONDS));
            assertEquals(0, calls.get());
        }
        assertTrue(results.isEmpty());
    }

    @Test
    void publishesStableUuidPropertiesWithoutCredentialsOnRepeatedScans() throws Exception {
        var current = create(Map.of("discoverySubnet", "10.0.0.50/32"), (host, timeout) -> {
            assertEquals(3, timeout);
            assertEquals("10.0.0.50", host.getHostAddress());
            return descriptor(IDENTITY);
        });
        for (int i = 0; i < 2; i++) {
            Completion completion = new Completion();
            current.startScan(completion);
            assertTrue(completion.finished.get(5, TimeUnit.SECONDS));
            drain();
        }
        assertEquals(2, results.size());
        assertEquals(1, results.stream().map(DiscoveryResult::getThingUID).distinct().count());
        DiscoveryResult result = results.getFirst();
        assertEquals("smartthings:appliance:" + IDENTITY, result.getThingUID().toString());
        assertEquals("deviceId", result.getRepresentationProperty());
        assertEquals(Map.of("host", "10.0.0.50", "port", 49155, "deviceId", IDENTITY), result.getProperties());
        assertEquals("Samsung Appliance", result.getLabel());
    }

    @Test
    void excludesConflictingIdentitiesAndContinuesAfterCommunicationFailures() throws Exception {
        var current = create(Map.of("discoverySubnet", "10.0.0.0/29"), (host, timeout) -> {
            return switch (host.getHostAddress()) {
                case "10.0.0.1", "10.0.0.2" -> descriptor(IDENTITY);
                case "10.0.0.3" -> descriptor("87654321-4321-6789-abcd-987654321abc");
                default -> throw new IOException("No supported appliance");
            };
        });
        Completion completion = new Completion();
        current.startScan(completion);
        assertTrue(completion.finished.get(5, TimeUnit.SECONDS));
        drain();
        assertEquals(1, results.size());
        assertEquals("10.0.0.3", results.getFirst().getProperties().get("host"));
    }

    @Test
    void cancellationInterruptsProbesAndDoesNotPublishLateResults() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        var current = create(Map.of("discoverySubnet", "10.0.0.50/32"), (host, timeout) -> {
            entered.countDown();
            try {
                new CountDownLatch(1).await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.countDown();
                Thread.currentThread().interrupt();
            }
            return descriptor(IDENTITY);
        });
        Completion completion = new Completion();
        current.startScan(completion);
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        current.abortScan();
        assertFalse(completion.finished.get(5, TimeUnit.SECONDS));
        assertTrue(interrupted.await(5, TimeUnit.SECONDS));
        drain();
        assertTrue(results.isEmpty());
    }

    @Test
    void boundsActualConcurrencyAcrossRescansAndRecoversAfterCancelledWorkersExit() throws Exception {
        CountDownLatch entered = new CountDownLatch(4);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger concurrent = new AtomicInteger();
        AtomicInteger maximum = new AtomicInteger();
        AtomicInteger calls = new AtomicInteger();
        var current = create(Map.of("discoverySubnet", "10.0.0.0/29"), (host, timeout) -> {
            maximum.accumulateAndGet(concurrent.incrementAndGet(), Math::max);
            try {
                if (calls.incrementAndGet() <= 4) {
                    entered.countDown();
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                    while (release.getCount() != 0 && System.nanoTime() < deadline) {
                        try {
                            release.await(1, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            // Simulate a cancelled request which has not yet finished unwinding.
                        }
                    }
                }
                return descriptor(
                        host.getHostAddress().endsWith(".1") ? IDENTITY : "87654321-4321-6789-abcd-987654321abc");
            } finally {
                concurrent.decrementAndGet();
            }
        });
        try {
            Completion oldScan = new Completion();
            current.startScan(oldScan);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            Completion newScan = new Completion();
            current.startScan(newScan);
            assertFalse(oldScan.finished.get(5, TimeUnit.SECONDS));
            assertTrue(results.isEmpty());
            release.countDown();
            assertTrue(newScan.finished.get(5, TimeUnit.SECONDS));
            drain();
            assertEquals(4, maximum.get());
            assertEquals(1, results.size());
            assertEquals("10.0.0.1", results.getFirst().getProperties().get("host"));
        } finally {
            release.countDown();
        }
    }

    @Test
    void configurationChangeAndDeactivationCancelAnActiveScan() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        var current = create(Map.of("discoverySubnet", "10.0.0.50/32"), (host, timeout) -> {
            if (calls.incrementAndGet() == 1) {
                entered.countDown();
                try {
                    new CountDownLatch(1).await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return descriptor(IDENTITY);
        });
        Completion oldScan = new Completion();
        current.startScan(oldScan);
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        current.modified(Map.of("discoverySubnet", "10.0.0.51/32"));
        assertFalse(oldScan.finished.get(5, TimeUnit.SECONDS));
        Completion newScan = new Completion();
        current.startScan(newScan);
        assertTrue(newScan.finished.get(5, TimeUnit.SECONDS));
        drain();
        assertEquals(1, results.size());
        assertEquals("10.0.0.51", results.getFirst().getProperties().get("host"));
        current.deactivate();
        assertTrue(control.awaitTermination(5, TimeUnit.SECONDS));
        current.startScan(new Completion());
        assertEquals(2, calls.get());
    }

    @Test
    void timeoutCancelsOutstandingProbesWithoutPublishingPartialResults() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        var current = create(Map.of("discoverySubnet", "10.0.0.50/32"), (host, timeout) -> {
            entered.countDown();
            try {
                new CountDownLatch(1).await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.countDown();
                Thread.currentThread().interrupt();
            }
            return descriptor(IDENTITY);
        }, 1);
        Completion completion = new Completion();
        current.startScan(completion);
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        assertTrue(completion.finished.get(5, TimeUnit.SECONDS));
        assertTrue(interrupted.await(5, TimeUnit.SECONDS));
        drain();
        assertTrue(results.isEmpty());
    }

    @Test
    void deactivationInterruptsActiveProbesAndShutsDownOwnedScheduling() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        var current = create(Map.of("discoverySubnet", "10.0.0.50/32"), (host, timeout) -> {
            entered.countDown();
            try {
                new CountDownLatch(1).await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.countDown();
                Thread.currentThread().interrupt();
            }
            return descriptor(IDENTITY);
        });
        Completion completion = new Completion();
        current.startScan(completion);
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        current.deactivate();
        assertFalse(completion.finished.get(5, TimeUnit.SECONDS));
        assertTrue(interrupted.await(5, TimeUnit.SECONDS));
        assertTrue(control.awaitTermination(5, TimeUnit.SECONDS));
        current.startScan(new Completion());
        assertTrue(results.isEmpty());
    }

    private static final class Completion implements ScanListener {
        final CompletableFuture<Boolean> finished = new CompletableFuture<>();

        @Override
        public void onFinished() {
            finished.complete(true);
        }

        @Override
        public void onErrorOccurred(@Nullable Exception exception) {
            finished.complete(false);
        }
    }
}
