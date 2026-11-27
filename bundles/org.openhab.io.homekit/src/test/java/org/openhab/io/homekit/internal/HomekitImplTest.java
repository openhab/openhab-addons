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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
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

    private @Nullable MockedStatic<FrameworkUtil> frameworkUtil;
    private @Nullable MockedStatic<NetUtil> netUtil;
    private @Nullable HomekitImpl homekit;
    private boolean failCreateBridge;

    @BeforeEach
    void setUp() {
        when(storageService.getStorage(anyString())).thenAnswer(invocation -> new VolatileStorage<>());

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

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(2, servers.constructed().size());
            verify(bridge).start();
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
            assertEquals(1, homekit.getInstanceCount());

            Map<String, Object> changedConfig = new HashMap<>(config);
            changedConfig.put("name", "openHAB 2");
            homekit.modified(changedConfig);

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

            assertEquals(1, homekit.getInstanceCount());
            assertEquals(List.of(InetAddress.getByName(ADDRESS), InetAddress.getByName(NEW_ADDRESS)), serverAddresses);
            verify(servers.constructed().get(0)).stop();
        }
    }

    private MockedConstruction<HomekitServer> mockServers() {
        return mockConstruction(HomekitServer.class, (server, context) -> {
            serverAddresses.add(context.arguments().get(0));
            when(server.createBridge(any(), anyString(), anyInt(), anyString(), anyString(), anyString(), anyString(),
                    anyString())).thenAnswer(invocation -> {
                        if (failCreateBridge) {
                            throw new IOException("Can't assign requested address");
                        }
                        return bridge;
                    });
        });
    }

    private HomekitImpl startHomekit() throws IOException {
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class);
        when(configAdmin.getConfiguration(anyString())).thenReturn(mock(Configuration.class));
        HomekitImpl homekit = new HomekitImpl(storageService, mock(ItemRegistry.class), networkAddressService,
                mock(MetadataRegistry.class), configAdmin, mock(MDNSClient.class), mock(ReadyService.class), config);
        this.homekit = homekit;
        homekit.onReadyMarkerAdded(new ReadyMarker("startlevel", "100"));
        return homekit;
    }

    private static CidrAddress cidr(String address) throws IOException {
        return new CidrAddress(InetAddress.getByName(address), (short) 24);
    }
}
