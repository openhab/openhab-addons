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
package org.openhab.binding.tuya.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceListInfo;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;

/**
 * The {@link TuyaDiscoveryServiceTest} is a test class for the {@link TuyaDiscoveryService} class
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class TuyaDiscoveryServiceTest {
    // Results are published to the listeners from the discovery scheduler, so every assertion has to wait for them.
    // Generous because a failing expectation fails the test through the latch rather than by timing out the build.
    private static final int RESULT_TIMEOUT = 60; // Seconds

    private final Gson gson = new Gson();

    private @Mock @NonNullByDefault({}) ThingRegistry thingRegistryMock;
    private @Mock @NonNullByDefault({}) ProjectHandler projectHandlerMock;
    private @Mock @NonNullByDefault({}) TuyaOpenAPI apiMock;

    private @NonNullByDefault({}) TuyaDiscoveryService discoveryService;
    private @NonNullByDefault({}) CollectingDiscoveryListener listener;

    /**
     * Collects the published results and lets a test wait until it has seen as many as it expects.
     */
    private static final class CollectingDiscoveryListener implements DiscoveryListener {
        private final Set<ThingUID> discovered = ConcurrentHashMap.newKeySet();
        private volatile CountDownLatch latch = new CountDownLatch(0);

        void expect(int results) {
            latch = new CountDownLatch(results);
        }

        boolean await() throws InterruptedException {
            return latch.await(RESULT_TIMEOUT, TimeUnit.SECONDS);
        }

        @Override
        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            discovered.add(result.getThingUID());
            latch.countDown();
        }

        @Override
        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        }

        @Override
        public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, Instant timestamp,
                @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
            return List.of();
        }
    }

    private DeviceListInfo device(String id, boolean subFlag) {
        return Objects.requireNonNull(gson.fromJson("{\"id\":\"" + id + "\",\"name\":\"" + id + "\",\"product_id\":\"p-"
                + id + "\",\"category\":\"cz\",\"local_key\":\"k\",\"sub\":" + subFlag + "}", DeviceListInfo.class));
    }

    @BeforeEach
    public void setUp() {
        when(projectHandlerMock.getApi()).thenReturn(apiMock);
        when(apiMock.isConnected()).thenReturn(true);
        // A schema request would populate the static schema database, which is irrelevant here and shared between
        // tests, so it is left pending.
        when(apiMock.getDeviceSchema(any())).thenReturn(new CompletableFuture<>());
        when(apiMock.getFactoryInformation(any())).thenReturn(CompletableFuture.completedFuture(List.of()));

        discoveryService = new TuyaDiscoveryService(thingRegistryMock);
        discoveryService.setThingHandler(projectHandlerMock);

        listener = new CollectingDiscoveryListener();
        discoveryService.addDiscoveryListener(listener);
    }

    /**
     * An account whose sub-device endpoint cannot be reached at all must still yield its devices. Every probe failing
     * leaves every device unclassified, which must not be mistaken for "every device is claimed by some gateway".
     */
    @Test
    public void everyGatewayProbeFailingStillReportsPlainDevices() throws Exception {
        // The sub flag is what makes the scan probe for gateways in the first place. The cloud sets it unreliably,
        // so a device carrying it is not necessarily a sub-device.
        List<DeviceListInfo> devices = List.of(device("aaa", true), device("bbb", false));
        // The device list is paged from 1; the scan primes itself with an empty page 0
        when(projectHandlerMock.getAllDevices(1)).thenReturn(CompletableFuture.completedFuture(devices));
        when(apiMock.getSubDevices(any())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("no")));

        listener.expect(2);
        discoveryService.startScan();

        assertTrue(listener.await(), "expected both devices to be discovered, saw " + listener.discovered);
        assertEquals(Set.of(new ThingUID("tuya:tuyaDevice:aaa"), new ThingUID("tuya:tuyaDevice:bbb")),
                listener.discovered);
    }

    /**
     * A scan continues asynchronously across several cloud requests. Once the service is disposed none of those
     * callbacks may publish a result or ask the cloud for more.
     */
    @Test
    public void disposeStopsAScanThatIsStillRunning() throws Exception {
        CompletableFuture<List<DeviceListInfo>> firstPage = new CompletableFuture<>();
        when(projectHandlerMock.getAllDevices(anyInt())).thenReturn(firstPage);

        discoveryService.startScan();
        discoveryService.dispose();

        // The device list only arrives after the project was removed
        firstPage.complete(List.of(device("aaa", false)));

        assertEquals(Set.of(), listener.discovered);
        verify(apiMock, never()).getFactoryInformation(any());
    }

    /**
     * The scan also runs on a timer, so a slow scan can still be in flight when the next one starts. The older scan
     * must not report into the newer one.
     */
    @Test
    public void aNewScanSupersedesTheRunningOne() throws Exception {
        CompletableFuture<List<DeviceListInfo>> firstScanPage = new CompletableFuture<>();
        when(projectHandlerMock.getAllDevices(anyInt())).thenReturn(firstScanPage);
        discoveryService.startScan();

        when(projectHandlerMock.getAllDevices(1))
                .thenReturn(CompletableFuture.completedFuture(List.of(device("bbb", false))));
        listener.expect(1);
        discoveryService.startScan();
        assertTrue(listener.await(), "expected the second scan to report its device");

        // The first scan only now gets its device list
        firstScanPage.complete(List.of(device("aaa", false)));

        assertEquals(Set.of(new ThingUID("tuya:tuyaDevice:bbb")), listener.discovered);
    }
}
