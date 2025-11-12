package org.openhab.binding.jellyfin.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.core.thing.ThingUID;

class ClientDiscoveryServiceTest {
    @Mock
    private ServerHandler serverHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testThingUIDGenerationForValidClient() {
        String deviceId = "abc123";
        ThingUID bridgeUID = new ThingUID(Constants.THING_TYPE_SERVER, "server1");
        String sanitizedDeviceId = deviceId.replaceAll("[^A-Za-z0-9_-]", "-");
        ThingUID clientUID = new ThingUID(Constants.THING_TYPE_JELLYFIN_CLIENT, bridgeUID, sanitizedDeviceId);
        assertEquals("jellyfin:client:server1:abc123", clientUID.toString());
    }

    @Test
    void testThingUIDSanitizationSpecialChars() {
        String deviceId = "id:with:colons";
        ThingUID bridgeUID = new ThingUID(Constants.THING_TYPE_SERVER, "server1");
        String sanitizedDeviceId = deviceId.replaceAll("[^A-Za-z0-9_-]", "-");
        ThingUID clientUID = new ThingUID(Constants.THING_TYPE_JELLYFIN_CLIENT, bridgeUID, sanitizedDeviceId);
        assertTrue(clientUID.toString().matches("jellyfin:client:server1:[a-zA-Z0-9_-]+"));
    }

    @Test
    void testOnlySupportedThingTypesDiscovered() {
        assertTrue(org.openhab.binding.jellyfin.internal.Constants.DISCOVERABLE_CLIENT_THING_TYPES
                .contains(org.openhab.binding.jellyfin.internal.Constants.THING_TYPE_JELLYFIN_CLIENT));
    }
}
