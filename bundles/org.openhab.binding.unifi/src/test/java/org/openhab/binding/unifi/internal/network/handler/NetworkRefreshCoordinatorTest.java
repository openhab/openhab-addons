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
package org.openhab.binding.unifi.internal.network.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.unifi.internal.UniFiBindingConstants.THING_TYPE_CONTROLLER;
import static org.openhab.binding.unifi.internal.network.UniFiBindingConstants.THING_TYPE_WIRELESS_CLIENT;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.unifi.internal.api.UniFiSession;
import org.openhab.binding.unifi.internal.handler.UniFiControllerBridgeHandler;
import org.openhab.binding.unifi.internal.network.api.UniFiController;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests that the Network refresh loop follows the lifecycle of the controller bridge handler it polls through, so
 * that a removed and re-created or re-initialized bridge does not leave the Network things without updates.
 *
 * @author Mikhail Obodnikov - Initial contribution
 */
@NonNullByDefault
public class NetworkRefreshCoordinatorTest {

    private static final AtomicInteger BRIDGE_COUNTER = new AtomicInteger();

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

    // The coordinator registry is static, so every test uses a bridge UID of its own.
    private ThingUID bridgeUID = new ThingUID(THING_TYPE_CONTROLLER, "unused");

    @BeforeEach
    public void setUp() {
        bridgeUID = new ThingUID(THING_TYPE_CONTROLLER, "test" + BRIDGE_COUNTER.incrementAndGet());
    }

    @Test
    public void disposeDetachesEvenWhenBridgeWasAlreadyRemovedFromRegistry() {
        BridgeFixture bridge = new BridgeFixture(bridgeUID);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge.bridge);
        UniFiClientThingHandler child = newClientHandler();
        child.initialize();

        // When the bridge Thing is removed, the framework disposes its children after the bridge has already left
        // the Thing registry, so the child can no longer look its bridge up.
        when(callback.getBridge(bridgeUID)).thenReturn(null);
        child.dispose();

        verify(bridge.refreshJob).cancel(true);
    }

    @Test
    public void attachToNewBridgeHandlerReplacesCoordinatorBoundToPreviousOne() {
        BridgeFixture oldBridge = new BridgeFixture(bridgeUID);
        // This subscriber is never detached, like a child that was disposed without reaching the coordinator.
        NetworkRefreshCoordinator stale = NetworkRefreshCoordinator.attach(oldBridge.handler,
                mock(UniFiBaseThingHandler.class));

        BridgeFixture newBridge = new BridgeFixture(bridgeUID);
        NetworkRefreshCoordinator current = NetworkRefreshCoordinator.attach(newBridge.handler,
                mock(UniFiBaseThingHandler.class));

        assertNotSame(stale, current);
        verify(oldBridge.refreshJob).cancel(true);
        assertNotNull(newBridge.refreshTask());
    }

    @Test
    public void controllerIsRebuiltWhenBridgeProvidesNewSession() {
        BridgeFixture bridge = new BridgeFixture(bridgeUID);
        bridge.login(unreachableHttpClient());
        NetworkRefreshCoordinator coordinator = NetworkRefreshCoordinator.attach(bridge.handler,
                mock(UniFiBaseThingHandler.class));
        Runnable refresh = bridge.refreshTask();

        refresh.run();
        UniFiController first = coordinator.getController();
        refresh.run();
        assertNotNull(first);
        assertSame(first, coordinator.getController());

        // The bridge was re-initialized on the same handler instance, e.g. after a configuration change: it stopped
        // its previous HTTP client and logged in again with a new client and session.
        bridge.login(unreachableHttpClient());
        refresh.run();

        assertNotSame(first, coordinator.getController());
    }

    @Test
    public void bridgeConfigurationChangeIsAppliedAfterReinitialization() {
        BridgeFixture bridge = new BridgeFixture(bridgeUID);
        HttpClient firstClient = bridge.login(unreachableHttpClient());
        NetworkRefreshCoordinator.attach(bridge.handler, mock(UniFiBaseThingHandler.class));
        Runnable refresh = bridge.refreshTask();

        refresh.run();
        assertEquals("https://console/proxy/network/api/self/sites", requestedUrl(firstClient));

        bridge.bridge.getConfiguration().put("unifios", false);
        HttpClient secondClient = bridge.login(unreachableHttpClient());
        refresh.run();

        assertEquals("https://console/api/self/sites", requestedUrl(secondClient));
    }

    @Test
    public void failureToBuildControllerDoesNotEscapeRefreshTask() {
        BridgeFixture bridge = new BridgeFixture(bridgeUID);
        when(bridge.handler.getSession()).thenReturn(mock(UniFiSession.class));
        // The bridge is being disposed: its session is still set but its HTTP client is already gone.
        when(bridge.handler.getHttpClient())
                .thenThrow(new IllegalStateException("HTTP client requested before bridge initialization"));
        NetworkRefreshCoordinator coordinator = NetworkRefreshCoordinator.attach(bridge.handler,
                mock(UniFiBaseThingHandler.class));

        // An exception escaping the task would silently cancel all later runs of the fixed-delay schedule.
        assertDoesNotThrow(bridge.refreshTask()::run);
        assertNotNull(coordinator.getLastError());
    }

    private UniFiClientThingHandler newClientHandler() {
        Thing thing = ThingBuilder
                .create(THING_TYPE_WIRELESS_CLIENT, new ThingUID(THING_TYPE_WIRELESS_CLIENT, bridgeUID, "phone"))
                .withBridge(bridgeUID).withConfiguration(new Configuration(Map.of("cid", "aa:bb:cc:dd:ee:ff"))).build();
        UniFiClientThingHandler handler = new UniFiClientThingHandler(thing);
        handler.setCallback(callback);
        return handler;
    }

    private static HttpClient unreachableHttpClient() {
        HttpClient client = mock(HttpClient.class);
        when(client.newRequest(anyString())).thenThrow(new IllegalStateException("No network in unit tests"));
        return client;
    }

    private static String requestedUrl(HttpClient client) {
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(client).newRequest(url.capture());
        return url.getValue();
    }

    /**
     * A controller bridge whose handler is a mock that captures the refresh job instead of running it.
     */
    private static class BridgeFixture {
        final Bridge bridge;
        final UniFiControllerBridgeHandler handler = mock(UniFiControllerBridgeHandler.class);
        final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        final ScheduledFuture<?> refreshJob = mock(ScheduledFuture.class);

        BridgeFixture(ThingUID bridgeUID) {
            bridge = BridgeBuilder.create(THING_TYPE_CONTROLLER, bridgeUID)
                    .withConfiguration(new Configuration(Map.of("unifios", true))).build();
            bridge.setHandler(handler);
            bridge.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
            when(handler.getThing()).thenReturn(bridge);
            when(handler.getScheduler()).thenReturn(scheduler);
            doReturn(refreshJob).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                    any(TimeUnit.class));
        }

        /**
         * Simulates a successful login of the bridge: a new session backed by the given HTTP client.
         */
        HttpClient login(HttpClient client) {
            UniFiSession session = mock(UniFiSession.class);
            when(session.getBaseUrl()).thenReturn("https://console");
            when(handler.getSession()).thenReturn(session);
            when(handler.getHttpClient()).thenReturn(client);
            return client;
        }

        Runnable refreshTask() {
            ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).scheduleWithFixedDelay(task.capture(), anyLong(), anyLong(), any(TimeUnit.class));
            return task.getValue();
        }
    }
}
