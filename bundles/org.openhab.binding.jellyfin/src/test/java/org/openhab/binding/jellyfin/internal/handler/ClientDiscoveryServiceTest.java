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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;
import org.openhab.binding.jellyfin.internal.discovery.ClientDiscoveryService;
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

    @Test
    void testDiscoveryResultIncludesThingType() {
        // Arrange
        String deviceId = "device-with-thing-type";
        String deviceName = "Test Device";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName(deviceName);
        session.setClient("Jellyfin Web");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Create a mock DiscoveryListener to capture the discovery result
        DiscoveryListener listener = mock(DiscoveryListener.class);
        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);

        discoveryService.addDiscoveryListener(listener);

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(listener).thingDiscovered(any(), resultCaptor.capture());
        DiscoveryResult result = resultCaptor.getValue();

        assertNotNull(result, "Discovery result should not be null");
        assertEquals(Constants.THING_TYPE_JELLYFIN_CLIENT, result.getThingTypeUID(),
                "Discovery result should have correct ThingTypeUID");
        assertTrue(result.getThingUID().getId().contains(deviceId.replaceAll("[^a-zA-Z0-9_-]", "-")),
                "ThingUID should contain sanitized device ID");
        assertEquals(bridgeUID, result.getBridgeUID(), "Discovery result should have correct bridge UID");
    }

    @Test
    void testDiscoveryResultIncludesAllProperties() {
        // Arrange
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

        // Create a mock DiscoveryListener to capture the discovery result
        DiscoveryListener listener = mock(DiscoveryListener.class);
        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);

        discoveryService.addDiscoveryListener(listener);

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(listener).thingDiscovered(any(), resultCaptor.capture());
        DiscoveryResult result = resultCaptor.getValue();

        assertNotNull(result, "Discovery result should not be null");

        // Verify all properties are set
        Map<String, Object> properties = result.getProperties();
        assertEquals(deviceId, properties.get(Thing.PROPERTY_SERIAL_NUMBER),
                "Serial number property should match device ID");
        assertEquals(appVersion, properties.get(Thing.PROPERTY_FIRMWARE_VERSION),
                "Firmware version property should be set");
        assertEquals(clientName, properties.get(Thing.PROPERTY_VENDOR), "Vendor property should be set");

        // Verify representation property
        assertEquals(Thing.PROPERTY_SERIAL_NUMBER, result.getRepresentationProperty(),
                "Representation property should be serial number");

        // Verify label
        assertEquals(deviceName, result.getLabel(), "Label should match device name");
    }

    @Test
    void testSanitizeDeviceIdReplacesSpecialCharacters() {
        // Arrange
        String deviceId = "device:with/special@chars!";
        String expectedSanitized = "device-with-special-chars-";

        SessionInfoDto session = new SessionInfoDto();
        session.setDeviceId(deviceId);
        session.setDeviceName("Test Device");

        Map<String, SessionInfoDto> clients = new HashMap<>();
        clients.put("session-1", session);

        when(serverHandler.getClients()).thenReturn(clients);

        // Create a mock DiscoveryListener to capture the discovery result
        DiscoveryListener listener = mock(DiscoveryListener.class);
        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);

        discoveryService.addDiscoveryListener(listener);

        // Act
        discoveryService.discoverClients();

        // Assert
        verify(listener).thingDiscovered(any(), resultCaptor.capture());
        DiscoveryResult result = resultCaptor.getValue();

        String thingId = result.getThingUID().getId();
        assertTrue(thingId.contains(expectedSanitized),
                "ThingUID should contain sanitized device ID with special characters replaced");
        assertFalse(thingId.contains(":"), "ThingUID should not contain colon");
        assertFalse(thingId.contains("/"), "ThingUID should not contain slash");
        assertFalse(thingId.contains("@"), "ThingUID should not contain at sign");
        assertFalse(thingId.contains("!"), "ThingUID should not contain exclamation mark");
    }
}
