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
package org.openhab.io.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.transport.mdns.MDNSClient;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.storage.StorageService;
import org.openhab.core.test.storage.VolatileStorage;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import io.github.hapjava.server.impl.HomekitRoot;
import io.github.hapjava.server.impl.HomekitServer;

/**
 * Tests that the HomeKit bridge starts once the network is available, see
 * <a href="https://github.com/openhab/openhab-addons/issues/13399">#13399</a>.
 *
 * @author Eugen Freiter - Initial contribution
 * @author Leo Siepel - Verify asynchronous startup and recovery
 */
@NonNullByDefault
class HomekitImplTest {
    private static final String ADDRESS = "192.0.2.10";
    private static final String NEW_ADDRESS = "192.0.2.11";

    private final StorageService storageService = mock(StorageService.class);
    private final NetworkAddressService networkAddressService = mock(NetworkAddressService.class);
    private final HomekitRoot bridge = mock(HomekitRoot.class);
    private final Map<String, Object> config = new HashMap<>();
    private final List<@Nullable Object> serverAddresses = new ArrayList<>();
    private final List<CidrAddress> interfaceAddresses = new ArrayList<>();
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final Queue<Runnable> callbacks = new ArrayDeque<>();
    private final List<ScheduledAction> scheduledActions = new ArrayList<>();
    private final ReadyService readyService = mock(ReadyService.class);
    private final ItemRegistry itemRegistry = mock(ItemRegistry.class);
    private final MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);

    private record ScheduledAction(Runnable task, ScheduledFuture<?> future) {
    }

    private @Nullable MockedStatic<FrameworkUtil> frameworkUtil;
    private @Nullable MockedStatic<NetUtil> netUtil;
    private @Nullable MockedStatic<ThreadPoolManager> threadPools;
    private @Nullable HomekitImpl homekit;
    private boolean failCreateBridge;

    @BeforeEach
    void setUp() {
        config.put("setupId", "TEST");
        when(storageService.getStorage(anyString())).thenAnswer(invocation -> new VolatileStorage<>());
        when(bridge.startAsync()).thenReturn(CompletableFuture.completedFuture(null));
        doAnswer(invocation -> {
            callbacks.add(invocation.getArgument(0));
            return null;
        }).when(scheduler).execute(any(Runnable.class));
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            ScheduledFuture<?> future = mock(ScheduledFuture.class);
            if (invocation.getArgument(2) == TimeUnit.SECONDS) {
                scheduledActions.add(new ScheduledAction(invocation.getArgument(0), future));
            }
            return future;
        });
        MockedStatic<ThreadPoolManager> threadPools = mockStatic(ThreadPoolManager.class);
        threadPools.when(() -> ThreadPoolManager.getScheduledPool(anyString())).thenReturn(scheduler);
        this.threadPools = threadPools;

        Bundle bundle = mock(Bundle.class);
        when(bundle.getVersion()).thenReturn(new Version(5, 3, 0));
        MockedStatic<FrameworkUtil> frameworkUtil = mockStatic(FrameworkUtil.class);
        frameworkUtil.when(() -> FrameworkUtil.getBundle(any(Class.class))).thenReturn(bundle);
        this.frameworkUtil = frameworkUtil;

        MockedStatic<NetUtil> netUtil = mockStatic(NetUtil.class);
        netUtil.when(NetUtil::getAllInterfaceAddresses).thenAnswer(invocation -> List.copyOf(interfaceAddresses));
        this.netUtil = netUtil;
    }

    @AfterEach
    void tearDown() {
        HomekitImpl homekit = this.homekit;
        if (homekit != null) {
            homekit.deactivate();
        }
        MockedStatic<FrameworkUtil> frameworkUtil = this.frameworkUtil;
        if (frameworkUtil != null) {
            frameworkUtil.close();
        }
        MockedStatic<NetUtil> netUtil = this.netUtil;
        if (netUtil != null) {
            netUtil.close();
        }
        MockedStatic<ThreadPoolManager> threadPools = this.threadPools;
        if (threadPools != null) {
            threadPools.close();
        }
    }

    @Test
    void failedStartIsRetriedOnNetworkChange() throws Exception {
        config.put("networkInterface", ADDRESS);
        failCreateBridge = true;
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            assertEquals(0, homekit.getInstanceCount());
            assertEquals(1, servers.constructed().size());
            verify(servers.constructed().get(0)).stop();

            failCreateBridge = false;
            homekit.onChanged(List.of(cidr(ADDRESS)), List.of());
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(2, servers.constructed().size());
            verify(bridge).startAsync();
        }
    }

    @Test
    void doesNotStartWithoutNetworkAddress() throws Exception {
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            assertEquals(0, homekit.getInstanceCount());
            assertTrue(servers.constructed().isEmpty());

            // the primary address stays unknown, so the address that came up is used
            homekit.onChanged(List.of(cidr("127.0.0.1"), cidr(ADDRESS)), List.of());
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(List.of(InetAddress.getByName(ADDRESS)), serverAddresses);
        }
    }

    @Test
    void startsOnCurrentAddressWithoutPrimaryAddress() throws Exception {
        interfaceAddresses.add(cidr(ADDRESS));
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(List.of(InetAddress.getByName(ADDRESS)), serverAddresses);
        }
    }

    @Test
    void restartsAfterSettingsChangeWithoutPrimaryAddress() throws Exception {
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            interfaceAddresses.add(cidr(ADDRESS));
            homekit.onChanged(List.of(cidr(ADDRESS)), List.of());
            runCallbacks();
            assertEquals(1, homekit.getInstanceCount());

            Map<String, Object> changedConfig = new HashMap<>(config);
            changedConfig.put("name", "openHAB 2");
            homekit.modified(changedConfig);
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(List.of(InetAddress.getByName(ADDRESS), InetAddress.getByName(ADDRESS)), serverAddresses);
            verify(servers.constructed().get(0)).stop();
        }
    }

    @Test
    void restartsOnNewAddressWhenAddressChanges() throws Exception {
        when(networkAddressService.getPrimaryIpv4HostAddress()).thenReturn(ADDRESS);
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            assertEquals(1, homekit.getInstanceCount());

            when(networkAddressService.getPrimaryIpv4HostAddress()).thenReturn(NEW_ADDRESS);
            homekit.onChanged(List.of(cidr(NEW_ADDRESS)), List.of(cidr(ADDRESS)));
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(List.of(InetAddress.getByName(ADDRESS), InetAddress.getByName(NEW_ADDRESS)), serverAddresses);
            verify(servers.constructed().get(0)).stop();
        }
    }

    @Test
    void instanceIsNotPublishedBeforeStartupCompletes() throws Exception {
        config.put("networkInterface", ADDRESS);
        CompletableFuture<@Nullable Void> startup = pendingStartup();
        when(bridge.startAsync()).thenReturn(startup);
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            assertEquals(0, homekit.getInstanceCount());
            homekit.onChanged(List.of(cidr(ADDRESS)), List.of());
            assertEquals(1, servers.constructed().size());

            startup.complete(null);
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            verify(scheduledActions.getFirst().future()).cancel(false);
        }
    }

    @Test
    void asynchronousFailureIsRetriedWithoutAnotherNetworkEvent() throws Exception {
        config.put("networkInterface", ADDRESS);
        CompletableFuture<@Nullable Void> startup = pendingStartup();
        when(bridge.startAsync()).thenReturn(startup).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            startup.completeExceptionally(new BindException("Address not available"));
            runCallbacks();

            assertEquals(0, homekit.getInstanceCount());
            verify(servers.constructed().getFirst()).stop();
            verify(bridge).stop();

            scheduledActions.getLast().task().run();
            runCallbacks();

            assertEquals(2, servers.constructed().size());
            assertEquals(1, homekit.getInstanceCount());
        }
    }

    @Test
    void networkChangeCancelsScheduledRetry() throws Exception {
        config.put("networkInterface", ADDRESS);
        CompletableFuture<@Nullable Void> startup = pendingStartup();
        when(bridge.startAsync()).thenReturn(startup).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            startup.completeExceptionally(new BindException("Address not available"));
            runCallbacks();
            ScheduledAction retry = scheduledActions.getLast();

            homekit.onChanged(List.of(cidr(ADDRESS)), List.of());
            runCallbacks();
            retry.task().run();

            verify(retry.future()).cancel(false);
            assertEquals(2, servers.constructed().size());
            assertEquals(1, homekit.getInstanceCount());
        }
    }

    @Test
    void timedOutStartupIsStoppedAndRetried() throws Exception {
        config.put("networkInterface", ADDRESS);
        when(bridge.startAsync()).thenReturn(pendingStartup()).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            scheduledActions.getFirst().task().run();

            assertEquals(0, homekit.getInstanceCount());
            verify(bridge).stop();
            verify(servers.constructed().getFirst()).stop();

            scheduledActions.getLast().task().run();
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
        }
    }

    @Test
    void lateCompletionAfterDeactivationCannotPublishOrRetry() throws Exception {
        config.put("networkInterface", ADDRESS);
        CompletableFuture<@Nullable Void> startup = pendingStartup();
        when(bridge.startAsync()).thenReturn(startup);
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            homekit.deactivate();
            startup.complete(null);
            runCallbacks();
            homekit.onReadyMarkerAdded(new ReadyMarker("startlevel", "100"));

            assertEquals(0, homekit.getInstanceCount());
            assertEquals(1, servers.constructed().size());
            assertEquals(1, scheduledActions.size());
            verify(bridge, times(2)).stop();
            verify(readyService).unregisterTracker(homekit);
        }
    }

    @Test
    void staleRetryCannotRestartDeactivatedIntegration() throws Exception {
        config.put("networkInterface", ADDRESS);
        CompletableFuture<@Nullable Void> startup = pendingStartup();
        when(bridge.startAsync()).thenReturn(startup);
        try (MockedConstruction<HomekitServer> servers = mockServers()) {
            HomekitImpl homekit = startHomekit();
            startup.completeExceptionally(new BindException("Address not available"));
            runCallbacks();
            ScheduledAction retry = scheduledActions.getLast();
            homekit.deactivate();
            retry.task().run();

            assertEquals(0, homekit.getInstanceCount());
            assertEquals(1, servers.constructed().size());
            verify(retry.future()).cancel(false);
        }
    }

    @Test
    void lateCompletionCannotReplaceNewConfiguration() throws Exception {
        config.put("networkInterface", ADDRESS);
        CompletableFuture<@Nullable Void> oldStartup = pendingStartup();
        when(bridge.startAsync()).thenReturn(oldStartup);
        HomekitRoot replacement = mock(HomekitRoot.class);
        when(replacement.startAsync()).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedConstruction<HomekitServer> servers = mockServers(bridge, replacement)) {
            HomekitImpl homekit = startHomekit();
            Map<String, Object> changedConfig = new HashMap<>(config);
            changedConfig.put("name", "replacement bridge");
            homekit.modified(changedConfig);
            runCallbacks();
            oldStartup.complete(null);
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(2, servers.constructed().size());
            verify(bridge, times(2)).stop();
            verify(replacement, never()).stop();
        }
    }

    @Test
    void multipleInstancesArePublishedOnlyAfterAllHaveStarted() throws Exception {
        config.put("networkInterface", ADDRESS);
        config.put("instances", 2);
        CompletableFuture<@Nullable Void> first = pendingStartup();
        CompletableFuture<@Nullable Void> second = pendingStartup();
        when(bridge.startAsync()).thenReturn(first);
        HomekitRoot other = mock(HomekitRoot.class);
        when(other.startAsync()).thenReturn(second);
        try (MockedConstruction<HomekitServer> servers = mockServers(bridge, other)) {
            HomekitImpl homekit = startHomekit();
            second.complete(null);
            runCallbacks();
            assertEquals(0, homekit.getInstanceCount());

            first.complete(null);
            runCallbacks();

            assertEquals(2, homekit.getInstanceCount());
            assertEquals(2, servers.constructed().size());
        }
    }

    @Test
    void failureStopsOtherPendingInstancesAndIgnoresLateSuccess() throws Exception {
        config.put("networkInterface", ADDRESS);
        config.put("instances", 2);
        CompletableFuture<@Nullable Void> first = pendingStartup();
        CompletableFuture<@Nullable Void> second = pendingStartup();
        when(bridge.startAsync()).thenReturn(first);
        HomekitRoot other = mock(HomekitRoot.class);
        when(other.startAsync()).thenReturn(second);
        try (MockedConstruction<HomekitServer> servers = mockServers(bridge, other)) {
            HomekitImpl homekit = startHomekit();
            first.completeExceptionally(new IOException("advertising failed"));
            runCallbacks();
            second.complete(null);
            runCallbacks();

            assertEquals(0, homekit.getInstanceCount());
            verify(servers.constructed().get(0)).stop();
            verify(servers.constructed().get(1), times(2)).stop();
            verify(other, times(2)).stop();
            verify(itemRegistry, atLeastOnce()).removeRegistryChangeListener(any());
        }
    }

    @Test
    void addingAnInstanceDoesNotStopRunningInstancesWhenItFails() throws Exception {
        config.put("networkInterface", ADDRESS);
        HomekitRoot added = mock(HomekitRoot.class);
        CompletableFuture<@Nullable Void> startup = pendingStartup();
        when(added.startAsync()).thenReturn(startup);
        HomekitRoot retry = mock(HomekitRoot.class);
        when(retry.startAsync()).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedConstruction<HomekitServer> servers = mockServers(bridge, added, retry)) {
            HomekitImpl homekit = startHomekit();
            assertEquals(1, homekit.getInstanceCount());
            Map<String, Object> changedConfig = new HashMap<>(config);
            changedConfig.put("instances", 2);
            homekit.modified(changedConfig);
            startup.completeExceptionally(new BindException("Address not available"));
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            verify(bridge, never()).stop();

            scheduledActions.getLast().task().run();
            runCallbacks();

            assertEquals(2, homekit.getInstanceCount());
            assertEquals(3, servers.constructed().size());
            verify(bridge, never()).stop();
        }
    }

    @Test
    void reducingInstancesDuringStartupDiscardsOldCompletions() throws Exception {
        config.put("networkInterface", ADDRESS);
        config.put("instances", 2);
        CompletableFuture<@Nullable Void> first = pendingStartup();
        CompletableFuture<@Nullable Void> second = pendingStartup();
        when(bridge.startAsync()).thenReturn(first);
        HomekitRoot other = mock(HomekitRoot.class);
        when(other.startAsync()).thenReturn(second);
        HomekitRoot replacement = mock(HomekitRoot.class);
        when(replacement.startAsync()).thenReturn(CompletableFuture.completedFuture(null));
        try (MockedConstruction<HomekitServer> servers = mockServers(bridge, other, replacement)) {
            HomekitImpl homekit = startHomekit();
            Map<String, Object> changedConfig = new HashMap<>(config);
            changedConfig.put("instances", 1);
            homekit.modified(changedConfig);
            runCallbacks();

            first.complete(null);
            second.complete(null);
            runCallbacks();

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(3, servers.constructed().size());
            verify(bridge, times(2)).stop();
            verify(other, times(2)).stop();
            verify(replacement, never()).stop();
        }
    }

    private MockedConstruction<HomekitServer> mockServers(HomekitRoot... roots) {
        return mockConstruction(HomekitServer.class, (server, context) -> {
            serverAddresses.add(context.arguments().get(0));
            HomekitRoot root = roots.length == 0 ? bridge : roots[context.getCount() - 1];
            when(server.createBridge(any(), anyString(), anyInt(), anyString(), anyString(), anyString(), anyString(),
                    anyString())).thenAnswer(invocation -> {
                        if (failCreateBridge) {
                            throw new IOException("Can't assign requested address");
                        }
                        return root;
                    });
        });
    }

    private HomekitImpl startHomekit() throws IOException {
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class);
        when(configAdmin.getConfiguration(anyString())).thenReturn(mock(Configuration.class));
        HomekitImpl homekit = new HomekitImpl(storageService, itemRegistry, networkAddressService, metadataRegistry,
                configAdmin, mock(MDNSClient.class), readyService, config);
        this.homekit = homekit;
        homekit.onReadyMarkerAdded(new ReadyMarker("startlevel", "100"));
        runCallbacks();
        return homekit;
    }

    private void runCallbacks() {
        Runnable callback;
        while ((callback = callbacks.poll()) != null) {
            callback.run();
        }
    }

    private static CompletableFuture<@Nullable Void> pendingStartup() {
        // Bound a regression that incorrectly waits synchronously for a bind to complete.
        return new CompletableFuture<@Nullable Void>().orTimeout(30, TimeUnit.SECONDS);
    }

    private static CidrAddress cidr(String address) throws IOException {
        return new CidrAddress(InetAddress.getByName(address), (short) 24);
    }
}
