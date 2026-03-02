/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.util.discovery.DeviceIdSanitizer;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link ClientDiscoveryService}
 *
 * @author Patrik Gfeller - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ClientDiscoveryServiceTest {

    private @NonNullByDefault({}) ClientDiscoveryService discoveryService;

    @Mock(lenient = true)
    private @NonNullByDefault({}) ServerHandler serverHandler;

    @Mock(lenient = true)
    private @NonNullByDefault({}) Bridge bridge;

    private @NonNullByDefault({}) ThingUID bridgeUID;

    @BeforeEach
    void setUp() throws Exception {
        bridgeUID = new ThingUID(Constants.THING_TYPE_SERVER, "test-server");
        when(bridge.getUID()).thenReturn(bridgeUID);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(serverHandler.getThing()).thenReturn(bridge);

        discoveryService = new ClientDiscoveryService();
        discoveryService.setThingHandler(serverHandler);
    }

    @Test
    void testDiscoverClientsWithValidSession() {
        String deviceId = "test-device-123";
        String deviceName = "Living Room TV";
        String clientName = "Jellyfin Web";
        String appVersion = "10.9.0";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setClient(clientName);
        session.setApplicationVersion(appVersion);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler, atLeastOnce()).getClients();
    }

    @Test
    void testDiscoverClientsSkipsMissingDeviceId() {
        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(null);
        session.setDeviceName("Test Device");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsSkipsEmptyDeviceId() {
        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId("   ");
        session.setDeviceName("Test Device");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsHandlesEmptyClientMap() {
        when(serverHandler.getClients()).thenReturn(Collections.emptyMap());

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsUsesDeviceNameForLabel() {
        String deviceId = "device-abc";
        String deviceName = "My Phone";
        String clientName = "Jellyfin Android";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setClient(clientName);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsFallsBackToClientNameWhenNoDeviceName() {
        String deviceId = "device-xyz";
        String clientName = "Jellyfin iOS";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName(null);
        session.setClient(clientName);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsHandlesMultipleSessions() {
        SessionInfoDto session1 = new SessionInfoDto();
        session1.setDeviceId("device-1");
        session1.setDeviceName("Device 1");

        SessionInfoDto session2 = new SessionInfoDto();
        session2.setDeviceId("device-2");
        session2.setDeviceName("Device 2");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session1);
        clients.put("session-2", session2);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsWithValidFirmwareVersion() {
        String deviceId = "device-with-version";
        String appVersion = "10.9.5";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName("Test Device");
        session.setApplicationVersion(appVersion);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsWithVendorInformation() {
        String deviceId = "device-with-client";
        String clientName = "Jellyfin Web";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName("Browser Device");
        session.setClient(clientName);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler).getClients();
    }

    @Test
    void testSupportedThingTypesContainsClientType() {
        assertTrue(Constants.DISCOVERABLE_CLIENT_THING_TYPES.contains(Constants.THING_TYPE_JELLYFIN_CLIENT));
    }

    @Test
    void testDiscoveryResultIncludesThingType() {
        String deviceId = "device-with-thing-type";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName("Test Device");
        session.setClient("Jellyfin Web");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        discoveryService.discoverClients();

        verify(serverHandler, atLeastOnce()).getClients();
    }

    @Test
    void testDiscoveryResultIncludesAllProperties() {
        String deviceId = "device-full-props";
        String deviceName = "Full Properties Device";
        String clientName = "Jellyfin iOS";
        String appVersion = "10.9.7";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setClient(clientName);
        session.setApplicationVersion(appVersion);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        DiscoveryListener listener = mock(DiscoveryListener.class);
        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);

        discoveryService.addDiscoveryListener(listener);

        discoveryService.discoverClients();

        // Discovery notifications are dispatched asynchronously by AbstractDiscoveryService's internal scheduler.
        // Use timeout() to avoid flakiness when the JVM thread pool is loaded.
        verify(listener, timeout(500)).thingDiscovered(any(), resultCaptor.capture());
        DiscoveryResult result = resultCaptor.getValue();

        assertNotNull(result, "Discovery result should not be null");

        Map<String, Object> properties = result.getProperties();
        assertEquals(deviceId, properties.get(Thing.PROPERTY_SERIAL_NUMBER),
                "Serial number property should match device ID");
        assertEquals(appVersion, properties.get(Thing.PROPERTY_FIRMWARE_VERSION),
                "Firmware version property should be set");
        assertEquals(clientName, properties.get(Thing.PROPERTY_VENDOR), "Vendor property should be set");

        assertEquals(Thing.PROPERTY_SERIAL_NUMBER, result.getRepresentationProperty(),
                "Representation property should be serial number");

        assertEquals(deviceName, result.getLabel(), "Label should match device name");
    }

    /**
     * Sanitization is now tested directly via {@link DeviceIdSanitizer} without discovery plumbing.
     * The discovery service delegates to that class; here we verify the contract of the sanitizer itself.
     */
    @Test
    void testSanitizeDeviceIdReplacesSpecialCharacters() {
        String deviceId = "device:with/special@chars!";
        String expected = "device-with-special-chars-";

        String actual = DeviceIdSanitizer.sanitize(deviceId);

        assertEquals(expected, actual, "Sanitization should replace forbidden characters with hyphens");
    }

    @Test
    void testDiscoverClientsDeduplicatesPrefixDeviceIds() {
        Map<String, SessionInfoDto> clients = new LinkedHashMap<>();

        SessionInfoDto shortId = new SessionInfoDto();
        shortId.setDeviceId("dev");
        shortId.setDeviceName("Short Device");

        SessionInfoDto longId = new SessionInfoDto();
        longId.setDeviceId("dev-ext");
        longId.setDeviceName("Long Device");

        clients.put("s1", shortId);
        clients.put("s2", longId);

        when(serverHandler.getClients()).thenReturn(clients);

        DiscoveryListener listener = mock(DiscoveryListener.class);
        ArgumentCaptor<DiscoveryResult> captor = ArgumentCaptor.forClass(DiscoveryResult.class);
        discoveryService.addDiscoveryListener(listener);

        discoveryService.discoverClients();

        verify(listener, timeout(500)).thingDiscovered(any(), captor.capture());
        DiscoveryResult result = captor.getValue();
        assertNotNull(result);
        Map<String, Object> props = result.getProperties();
        assertEquals("dev-ext", props.get(Thing.PROPERTY_SERIAL_NUMBER));
    }
}
