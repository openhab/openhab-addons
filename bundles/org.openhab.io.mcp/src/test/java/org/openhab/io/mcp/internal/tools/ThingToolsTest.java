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
package org.openhab.io.mcp.internal.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link ThingTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ThingToolsTest {

    @Mock
    @Nullable
    ThingRegistry thingRegistry;

    @Mock
    @Nullable
    ItemChannelLinkRegistry linkRegistry;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    private @Nullable ThingTools thingTools;

    @BeforeEach
    void setUp() {
        thingTools = new ThingTools(Objects.requireNonNull(thingRegistry), Objects.requireNonNull(linkRegistry),
                jsonMapper);
    }

    private ThingTools tools() {
        ThingTools t = thingTools;
        assertNotNull(t);
        return t;
    }

    private Thing mockThing(String uid, String label, ThingStatus status, String bindingId) {
        Thing thing = mock(Thing.class);
        ThingUID thingUID = new ThingUID(bindingId + ":type:" + uid);
        ThingTypeUID thingTypeUID = new ThingTypeUID(bindingId, "type");
        lenient().when(thing.getUID()).thenReturn(thingUID);
        lenient().when(thing.getLabel()).thenReturn(label);
        lenient().when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
        lenient().when(thing.getStatus()).thenReturn(status);
        lenient().when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(status, ThingStatusDetail.NONE, null));
        lenient().when(thing.getChannels()).thenReturn(List.of());
        lenient().when(thing.getConfiguration()).thenReturn(new Configuration());
        lenient().when(thing.getProperties()).thenReturn(Map.of());
        return thing;
    }

    private Item mockItem(String name, String label, String type, State state) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        lenient().when(item.getType()).thenReturn(type);
        lenient().when(item.getState()).thenReturn(state);
        lenient().when(item.getTags()).thenReturn(Set.of());
        lenient().when(item.getGroupNames()).thenReturn(List.of());
        lenient().when(item.getAcceptedCommandTypes()).thenReturn(List.of());
        return item;
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsNoFiltersReturnsAll() throws Exception {
        Thing t1 = mockThing("t1", "Thing 1", ThingStatus.ONLINE, "hue");
        Thing t2 = mockThing("t2", "Thing 2", ThingStatus.OFFLINE, "zwave");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(t1, t2));

        CallToolResult result = tools().handleGetThings(createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(2, things.size());
        assertEquals(2, parsed.get("total"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsFiltersByStatus() throws Exception {
        Thing online = mockThing("t1", "Online Thing", ThingStatus.ONLINE, "hue");
        Thing offline = mockThing("t2", "Offline Thing", ThingStatus.OFFLINE, "hue");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(online, offline));

        CallToolResult result = tools().handleGetThings(createRequest(Map.of("status", "ONLINE")));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(1, things.size());
        assertEquals("ONLINE", things.get(0).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsFiltersByOfflineStatus() throws Exception {
        Thing online = mockThing("t1", "Online Thing", ThingStatus.ONLINE, "hue");
        Thing offline = mockThing("t2", "Offline Thing", ThingStatus.OFFLINE, "hue");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(online, offline));

        CallToolResult result = tools().handleGetThings(createRequest(Map.of("status", "OFFLINE")));
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(1, things.size());
        assertEquals("OFFLINE", things.get(0).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsFiltersByBindingId() throws Exception {
        Thing hue = mockThing("t1", "Hue Light", ThingStatus.ONLINE, "hue");
        Thing zwave = mockThing("t2", "ZWave Device", ThingStatus.ONLINE, "zwave");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(hue, zwave));

        CallToolResult result = tools().handleGetThings(createRequest(Map.of("bindingId", "hue")));
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(1, things.size());
        assertEquals("hue", things.get(0).get("binding"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsPagination() throws Exception {
        Thing t1 = mockThing("t1", "Thing 1", ThingStatus.ONLINE, "hue");
        Thing t2 = mockThing("t2", "Thing 2", ThingStatus.ONLINE, "hue");
        Thing t3 = mockThing("t3", "Thing 3", ThingStatus.ONLINE, "hue");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(t1, t2, t3));

        Map<String, Object> args = new HashMap<>();
        args.put("limit", 2);
        args.put("offset", 1);
        CallToolResult result = tools().handleGetThings(createRequest(args));
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(2, things.size());
        assertEquals(3, parsed.get("total"));
        assertEquals(1, parsed.get("offset"));
        assertEquals(2, parsed.get("limit"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingDetailsReturnsChannelsConfigProperties() throws Exception {
        Thing thing = mock(Thing.class);
        ThingUID thingUID = new ThingUID("hue:0210:bridge:lamp1");
        ThingTypeUID thingTypeUID = new ThingTypeUID("hue", "0210");
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getLabel()).thenReturn("Lamp 1");
        when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
        when(thing.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        Configuration config = new Configuration();
        config.put("ipAddress", "192.168.1.100");
        when(thing.getConfiguration()).thenReturn(config);
        when(thing.getProperties()).thenReturn(Map.of("firmwareVersion", "1.2.3"));

        Channel channel = mock(Channel.class);
        ChannelUID channelUID = new ChannelUID("hue:0210:bridge:lamp1:color");
        when(channel.getUID()).thenReturn(channelUID);
        lenient().when(channel.getLabel()).thenReturn("Color");
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("hue:color"));
        when(channel.getAcceptedItemType()).thenReturn("Color");
        when(thing.getChannels()).thenReturn(List.of(channel));

        Item linkedItem = mockItem("Lamp1_Color", "Color", "Color", OnOffType.ON);
        when(Objects.requireNonNull(linkRegistry).getLinkedItems(channelUID)).thenReturn(Set.of(linkedItem));
        when(Objects.requireNonNull(thingRegistry).get(thingUID)).thenReturn(thing);

        CallToolResult result = tools()
                .handleGetThingDetails(createRequest(Map.of("thingUID", "hue:0210:bridge:lamp1")));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertEquals("hue:0210:bridge:lamp1", parsed.get("uid"));
        assertEquals("Lamp 1", parsed.get("label"));

        List<Map<String, Object>> channels = (List<Map<String, Object>>) parsed.get("channels");
        assertNotNull(channels);
        assertEquals(1, channels.size());
        assertEquals("hue:0210:bridge:lamp1:color", channels.get(0).get("uid"));

        Map<String, Object> configMap = (Map<String, Object>) parsed.get("configuration");
        assertNotNull(configMap);
        assertEquals("192.168.1.100", configMap.get("ipAddress"));

        Map<String, Object> properties = (Map<String, Object>) parsed.get("properties");
        assertNotNull(properties);
        assertEquals("1.2.3", properties.get("firmwareVersion"));
    }

    @Test
    void getThingDetailsNotFound() throws Exception {
        ThingUID thingUID = new ThingUID("hue:0210:bridge:missing");
        when(Objects.requireNonNull(thingRegistry).get(thingUID)).thenReturn(null);

        CallToolResult result = tools()
                .handleGetThingDetails(createRequest(Map.of("thingUID", "hue:0210:bridge:missing")));
        assertErrorContains(result, "not found");
    }

    @Test
    void getThingDetailsMissingParameter() throws Exception {
        CallToolResult result = tools().handleGetThingDetails(createRequest(Map.of()));
        assertErrorContains(result, "thingUID");
    }
}
