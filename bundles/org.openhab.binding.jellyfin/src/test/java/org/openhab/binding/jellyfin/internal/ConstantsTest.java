package org.openhab.binding.jellyfin.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for {@link Constants}
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
class ConstantsTest {

    @Test
    void testBindingIdIsCorrect() {
        // Verify the binding ID matches expected value
        ThingTypeUID serverType = Constants.THING_TYPE_SERVER;
        assertEquals("jellyfin", serverType.getBindingId());

        ThingTypeUID clientType = Constants.THING_TYPE_JELLYFIN_CLIENT;
        assertEquals("jellyfin", clientType.getBindingId());
    }

    @Test
    void testServerThingTypeUID() {
        ThingTypeUID serverType = Constants.THING_TYPE_SERVER;

        assertNotNull(serverType);
        assertEquals("jellyfin", serverType.getBindingId());
        assertEquals("server", serverType.getId());
    }

    @Test
    void testClientThingTypeUID() {
        ThingTypeUID clientType = Constants.THING_TYPE_JELLYFIN_CLIENT;

        assertNotNull(clientType);
        assertEquals("jellyfin", clientType.getBindingId());
        assertEquals("client", clientType.getId());
    }

    @Test
    void testSupportedThingTypesContainsBothTypes() {
        // Verify that both server and client thing types are supported
        assertTrue(Constants.SUPPORTED_THING_TYPES.contains(Constants.THING_TYPE_SERVER),
                "Server thing type should be in supported types");
        assertTrue(Constants.SUPPORTED_THING_TYPES.contains(Constants.THING_TYPE_JELLYFIN_CLIENT),
                "Client thing type should be in supported types");
        assertEquals(2, Constants.SUPPORTED_THING_TYPES.size(), "Should contain exactly 2 thing types");
    }

    @Test
    void testDiscoverableClientThingTypesContainsClient() {
        // Verify that client thing type is discoverable
        assertTrue(Constants.DISCOVERABLE_CLIENT_THING_TYPES.contains(Constants.THING_TYPE_JELLYFIN_CLIENT),
                "Client thing type should be discoverable");
        assertEquals(1, Constants.DISCOVERABLE_CLIENT_THING_TYPES.size(), "Should contain exactly 1 discoverable type");
    }

    @Test
    void testDiscoverableClientThingTypesDoesNotContainServer() {
        // Verify that server thing type is NOT in discoverable client types
        assertFalse(Constants.DISCOVERABLE_CLIENT_THING_TYPES.contains(Constants.THING_TYPE_SERVER),
                "Server thing type should not be in discoverable client types");
    }

    @Test
    void testDiscoveryResultTTL() {
        // Verify the TTL constant is reasonable (10 minutes)
        assertEquals(600, Constants.DISCOVERY_RESULT_TTL_SEC);
    }

    @Test
    void testChannelIdConstants() {
        // Verify critical channel IDs are defined
        assertNotNull(Constants.SEND_NOTIFICATION_CHANNEL);
        assertNotNull(Constants.MEDIA_CONTROL_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_ID_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_NAME_CHANNEL);

        // Verify channel IDs follow naming convention
        assertEquals("send-notification", Constants.SEND_NOTIFICATION_CHANNEL);
        assertEquals("media-control", Constants.MEDIA_CONTROL_CHANNEL);
        assertEquals("playing-item-percentage", Constants.PLAYING_ITEM_PERCENTAGE_CHANNEL);
    }

    @Test
    void testPlayCommandChannels() {
        // Verify play command channels are defined
        assertNotNull(Constants.PLAY_BY_TERMS_CHANNEL);
        assertNotNull(Constants.PLAY_NEXT_BY_TERMS_CHANNEL);
        assertNotNull(Constants.PLAY_LAST_BY_TERMS_CHANNEL);
        assertNotNull(Constants.PLAY_BY_ID_CHANNEL);
        assertNotNull(Constants.PLAY_NEXT_BY_ID_CHANNEL);
        assertNotNull(Constants.PLAY_LAST_BY_ID_CHANNEL);

        assertEquals("play-by-terms", Constants.PLAY_BY_TERMS_CHANNEL);
        assertEquals("play-by-id", Constants.PLAY_BY_ID_CHANNEL);
    }

    @Test
    void testBrowseCommandChannels() {
        // Verify browse command channels are defined
        assertNotNull(Constants.BROWSE_ITEM_BY_TERMS_CHANNEL);
        assertNotNull(Constants.BROWSE_ITEM_BY_ID_CHANNEL);

        assertEquals("browse-by-terms", Constants.BROWSE_ITEM_BY_TERMS_CHANNEL);
        assertEquals("browse-by-id", Constants.BROWSE_ITEM_BY_ID_CHANNEL);
    }

    @Test
    void testPlayingItemChannels() {
        // Verify all playing item channels are defined
        assertNotNull(Constants.PLAYING_ITEM_SERIES_NAME_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_SEASON_NAME_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_SEASON_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_EPISODE_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_GENRES_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_TYPE_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_SECOND_CHANNEL);
        assertNotNull(Constants.PLAYING_ITEM_TOTAL_SECOND_CHANNEL);
    }

    @Test
    void testServerPropertiesConstants() {
        // Verify server properties are defined
        assertEquals("apiVersion", Constants.ServerProperties.API_VERSION);
        assertEquals("uri", Constants.ServerProperties.SERVER_URI);
        assertEquals("Server Version", Constants.ServerProperties.SERVER_VERSION);
    }

    @Test
    void testThingTypesAreImmutable() {
        // Verify that supported thing types set is immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            Constants.SUPPORTED_THING_TYPES.add(new ThingTypeUID("test", "invalid"));
        }, "SUPPORTED_THING_TYPES should be immutable");

        assertThrows(UnsupportedOperationException.class, () -> {
            Constants.DISCOVERABLE_CLIENT_THING_TYPES.add(new ThingTypeUID("test", "invalid"));
        }, "DISCOVERABLE_CLIENT_THING_TYPES should be immutable");
    }
}
