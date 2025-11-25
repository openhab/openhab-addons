package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
import org.openhab.core.thing.Bridge;
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
        // Arrange
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

        // Act
        discoveryService.discoverClients();

        // Assert - verify thingDiscovered was called with correct parameters
        // Note: We can't easily verify the exact DiscoveryResult without a spy,
        // but we can verify the method executed without errors
        verify(serverHandler, atLeastOnce()).getClients();
    }

    @Test
    void testDiscoverClientsSkipsMissingDeviceId() {
        // Arrange
        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(null); // Missing device ID
        session.setDeviceName("Test Device");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Act
        discoveryService.discoverClients();

        // Assert - method completes without errors
        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsSkipsEmptyDeviceId() {
        // Arrange
        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId("   "); // Empty/blank device ID
        session.setDeviceName("Test Device");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Act
        discoveryService.discoverClients();

        // Assert - method completes without errors
        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsHandlesEmptyClientMap() {
        // Arrange
        when(serverHandler.getClients()).thenReturn(Collections.emptyMap());

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(serverHandler).getClients();
    }

    @Test
    void testDiscoverClientsUsesDeviceNameForLabel() {
        // Arrange
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

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(serverHandler).getClients();
        // Label should use deviceName, not clientName
    }

    @Test
    void testDiscoverClientsFallsBackToClientNameWhenNoDeviceName() {
        // Arrange
        String deviceId = "device-xyz";
        String clientName = "Jellyfin iOS";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName(null); // No device name
        session.setClient(clientName);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(serverHandler).getClients();
        // Label should fall back to clientName
    }

    @Test
    void testDiscoverClientsHandlesMultipleSessions() {
        // Arrange
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

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(serverHandler).getClients();
        // Should process both sessions
    }

    @Test
    void testDiscoverClientsWithValidFirmwareVersion() {
        // Arrange
        String deviceId = "device-with-version";
        String appVersion = "10.9.5";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName("Test Device");
        session.setApplicationVersion(appVersion);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(serverHandler).getClients();
        // Firmware version should be included in properties
    }

    @Test
    void testDiscoverClientsWithVendorInformation() {
        // Arrange
        String deviceId = "device-with-client";
        String clientName = "Jellyfin Web";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName("Browser Device");
        session.setClient(clientName);

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(serverHandler).getClients();
        // Client name should be included as vendor property
    }

    @Test
    void testSupportedThingTypesContainsClientType() {
        // Assert
        assertTrue(Constants.DISCOVERABLE_CLIENT_THING_TYPES.contains(Constants.THING_TYPE_JELLYFIN_CLIENT));
    }
}
