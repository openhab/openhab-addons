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
import org.openhab.core.thing.Bridge;
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

    private Thing setChildBridgeUID(Thing child, String bridgeUid) {
        lenient().when(child.getBridgeUID()).thenReturn(new ThingUID(bridgeUid));
        return child;
    }

    private Bridge mockBridge(String uid, String label, ThingStatus status, String bindingId, List<Thing> children) {
        Bridge bridge = mock(Bridge.class);
        ThingUID thingUID = new ThingUID(bindingId + ":type:" + uid);
        ThingTypeUID thingTypeUID = new ThingTypeUID(bindingId, "type");
        lenient().when(bridge.getUID()).thenReturn(thingUID);
        lenient().when(bridge.getLabel()).thenReturn(label);
        lenient().when(bridge.getThingTypeUID()).thenReturn(thingTypeUID);
        lenient().when(bridge.getStatus()).thenReturn(status);
        lenient().when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(status, ThingStatusDetail.NONE, null));
        lenient().when(bridge.getChannels()).thenReturn(List.of());
        lenient().when(bridge.getConfiguration()).thenReturn(new Configuration());
        lenient().when(bridge.getProperties()).thenReturn(Map.of());
        lenient().when(bridge.getThings()).thenReturn(children);
        return bridge;
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

    @Test
    @SuppressWarnings("unchecked")
    void getThingDetailsForBridgeIncludesChildren() throws Exception {
        Thing child1 = mockThing("sensor1", "Temperature Sensor", ThingStatus.ONLINE, "modbus");
        Thing child2 = mockThing("sensor2", "Humidity Sensor", ThingStatus.OFFLINE, "modbus");
        Bridge bridge = mockBridge("bridge1", "Modbus TCP Bridge", ThingStatus.ONLINE, "modbus",
                List.of(child1, child2));

        ThingRegistry reg = Objects.requireNonNull(thingRegistry);
        when(reg.get(bridge.getUID())).thenReturn(bridge);
        when(reg.get(child1.getUID())).thenReturn(child1);
        when(reg.get(child2.getUID())).thenReturn(child2);

        CallToolResult result = tools()
                .handleGetThingDetails(createRequest(Map.of("thingUID", bridge.getUID().toString())));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("isBridge"));

        List<Map<String, Object>> children = (List<Map<String, Object>>) parsed.get("childThings");
        assertNotNull(children);
        assertEquals(2, children.size());
        assertEquals("modbus:type:sensor1", children.get(0).get("uid"));
        assertEquals("modbus:type:sensor2", children.get(1).get("uid"));
    }

    @Test
    void getThingDetailsForNonBridgeHasNoChildThings() throws Exception {
        Thing thing = mockThing("device1", "Simple Device", ThingStatus.ONLINE, "hue");
        when(Objects.requireNonNull(thingRegistry).get(thing.getUID())).thenReturn(thing);

        CallToolResult result = tools()
                .handleGetThingDetails(createRequest(Map.of("thingUID", thing.getUID().toString())));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertNull(parsed.get("isBridge"));
        assertNull(parsed.get("childThings"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsListIndicatesBridge() throws Exception {
        Bridge bridge = mockBridge("bridge1", "My Bridge", ThingStatus.ONLINE, "mqtt", List.of());
        Thing device = mockThing("device1", "My Device", ThingStatus.ONLINE, "mqtt");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(bridge, device));

        CallToolResult result = tools().handleGetThings(createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(2, things.size());
        assertEquals(true, things.get(0).get("isBridge"));
        assertNull(things.get(1).get("isBridge"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsWithBindingFilterIncludesChildThings() throws Exception {
        Thing child1 = mockThing("sensor1", "Sensor 1", ThingStatus.ONLINE, "modbus");
        child1 = setChildBridgeUID(child1, "modbus:type:bridge1");
        Thing child2 = mockThing("sensor2", "Sensor 2", ThingStatus.OFFLINE, "modbus");
        child2 = setChildBridgeUID(child2, "modbus:type:bridge1");
        Bridge bridge = mockBridge("bridge1", "Modbus Bridge", ThingStatus.ONLINE, "modbus", List.of(child1, child2));
        ThingRegistry reg = Objects.requireNonNull(thingRegistry);
        when(reg.getAll()).thenReturn(List.of(bridge, child1, child2));
        when(reg.get(child1.getUID())).thenReturn(child1);
        when(reg.get(child2.getUID())).thenReturn(child2);

        CallToolResult result = tools().handleGetThings(createRequest(Map.of("bindingId", "modbus")));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(1, things.size());
        assertEquals(true, things.get(0).get("isBridge"));
        List<Map<String, Object>> children = (List<Map<String, Object>>) things.get(0).get("childThings");
        assertNotNull(children);
        assertEquals(2, children.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsWithBindingFilterIncludesCrossBindingChildren() throws Exception {
        Thing crossChild = mockThing("nvr1", "NVR", ThingStatus.ONLINE, "unifiprotect");
        crossChild = setChildBridgeUID(crossChild, "unifi:type:controller1");
        Bridge controller = mockBridge("controller1", "Controller", ThingStatus.ONLINE, "unifi", List.of(crossChild));
        ThingRegistry reg = Objects.requireNonNull(thingRegistry);
        when(reg.getAll()).thenReturn(List.of(controller, crossChild));
        when(reg.get(crossChild.getUID())).thenReturn(crossChild);

        CallToolResult result = tools().handleGetThings(createRequest(Map.of("bindingId", "unifi")));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(1, things.size());
        List<Map<String, Object>> children = (List<Map<String, Object>>) things.get(0).get("childThings");
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("unifiprotect", children.get(0).get("binding"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getThingsWithoutBindingFilterDoesNotNestChildren() throws Exception {
        Thing child = mockThing("sensor1", "Sensor 1", ThingStatus.ONLINE, "modbus");
        Bridge bridge = mockBridge("bridge1", "Modbus Bridge", ThingStatus.ONLINE, "modbus", List.of(child));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(bridge, child));

        CallToolResult result = tools().handleGetThings(createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> things = (List<Map<String, Object>>) parsed.get("things");
        assertNotNull(things);
        assertEquals(2, things.size());
        assertNull(things.get(0).get("childThings"));
    }

    @Test
    void getThingDetailsForBridgeWithNoChildrenOmitsChildThings() throws Exception {
        Bridge bridge = mockBridge("bridge1", "Empty Bridge", ThingStatus.ONLINE, "hue", List.of());
        when(Objects.requireNonNull(thingRegistry).get(bridge.getUID())).thenReturn(bridge);

        CallToolResult result = tools()
                .handleGetThingDetails(createRequest(Map.of("thingUID", bridge.getUID().toString())));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("isBridge"));
        assertNull(parsed.get("childThings"));
    }
}
