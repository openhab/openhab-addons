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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.OpenHAB;
import org.openhab.core.automation.Rule;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link SystemTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class SystemToolsTest {

    @Mock
    @Nullable
    ItemRegistry itemRegistry;

    @Mock
    @Nullable
    ThingRegistry thingRegistry;

    @Mock
    @Nullable
    RuleRegistry ruleRegistry;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    private @Nullable SystemTools systemTools;

    @BeforeEach
    void setUp() {
        systemTools = new SystemTools(Objects.requireNonNull(itemRegistry), Objects.requireNonNull(thingRegistry),
                Objects.requireNonNull(ruleRegistry), jsonMapper);
    }

    private SystemTools tools() {
        SystemTools t = systemTools;
        assertNotNull(t);
        return t;
    }

    private Item mockItem(String name, String label, String type, State state, Set<String> tags, List<String> groups) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        lenient().when(item.getType()).thenReturn(type);
        lenient().when(item.getState()).thenReturn(state);
        lenient().when(item.getTags()).thenReturn(tags);
        lenient().when(item.getGroupNames()).thenReturn(groups);
        lenient().when(item.getAcceptedCommandTypes()).thenReturn(List.of());
        for (String tag : tags) {
            lenient().when(item.hasTag(tag)).thenReturn(true);
        }
        return item;
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

    @Test
    @SuppressWarnings("unchecked")
    void getSystemInfoReturnsVersion() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());
        when(Objects.requireNonNull(ruleRegistry).getAll()).thenReturn(List.of());

        try (MockedStatic<OpenHAB> openHABMock = mockStatic(OpenHAB.class)) {
            openHABMock.when(OpenHAB::getVersion).thenReturn("5.2.0");

            CallToolResult result = tools().handleGetSystemInfo(createRequest(Map.of()));
            assertSuccess(result);

            Map<String, Object> parsed = parseResult(result);
            assertEquals("5.2.0", parsed.get("version"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSystemInfoReturnsItemCount() throws Exception {
        Item item1 = mockItem("Item1", "Item 1", "Switch", OnOffType.ON, Set.of(), List.of());
        Item item2 = mockItem("Item2", "Item 2", "String", OnOffType.OFF, Set.of(), List.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(item1, item2));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());
        when(Objects.requireNonNull(ruleRegistry).getAll()).thenReturn(List.of());

        try (MockedStatic<OpenHAB> openHABMock = mockStatic(OpenHAB.class)) {
            openHABMock.when(OpenHAB::getVersion).thenReturn("5.2.0");

            CallToolResult result = tools().handleGetSystemInfo(createRequest(Map.of()));
            Map<String, Object> parsed = parseResult(result);
            assertEquals(2, parsed.get("itemCount"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSystemInfoReturnsThingAndRuleCounts() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());
        Thing thing1 = mockThing("thing1", "Thing 1", ThingStatus.ONLINE, "hue");
        Thing thing2 = mockThing("thing2", "Thing 2", ThingStatus.OFFLINE, "zwave");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(thing1, thing2));
        Rule rule = mock(Rule.class);
        when(Objects.requireNonNull(ruleRegistry).getAll()).thenReturn(List.of(rule));

        try (MockedStatic<OpenHAB> openHABMock = mockStatic(OpenHAB.class)) {
            openHABMock.when(OpenHAB::getVersion).thenReturn("5.2.0");

            CallToolResult result = tools().handleGetSystemInfo(createRequest(Map.of()));
            Map<String, Object> parsed = parseResult(result);
            assertEquals(2, parsed.get("thingCount"));
            assertEquals(1, parsed.get("ruleCount"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSystemInfoReturnsBindingList() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());
        Thing thing1 = mockThing("thing1", "Thing 1", ThingStatus.ONLINE, "hue");
        Thing thing2 = mockThing("thing2", "Thing 2", ThingStatus.ONLINE, "zwave");
        Thing thing3 = mockThing("thing3", "Thing 3", ThingStatus.ONLINE, "hue");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(thing1, thing2, thing3));
        when(Objects.requireNonNull(ruleRegistry).getAll()).thenReturn(List.of());

        try (MockedStatic<OpenHAB> openHABMock = mockStatic(OpenHAB.class)) {
            openHABMock.when(OpenHAB::getVersion).thenReturn("5.2.0");

            CallToolResult result = tools().handleGetSystemInfo(createRequest(Map.of()));
            Map<String, Object> parsed = parseResult(result);
            List<String> bindings = (List<String>) parsed.get("installedBindings");
            assertNotNull(bindings);
            assertEquals(2, bindings.size());
            assertTrue(bindings.contains("hue"));
            assertTrue(bindings.contains("zwave"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusCountsOpenContacts() throws Exception {
        Item openContact = mockItem("FrontDoor", "Front Door", "Contact", OpenClosedType.OPEN, Set.of(), List.of());
        Item closedContact = mockItem("BackDoor", "Back Door", "Contact", OpenClosedType.CLOSED, Set.of(), List.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(openContact, closedContact));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> security = (Map<String, Object>) parsed.get("security");
        assertNotNull(security);
        assertEquals(2, security.get("totalContactSensors"));
        assertEquals(1, security.get("openCount"));
        List<Map<String, String>> openItems = (List<Map<String, String>>) security.get("openItems");
        assertNotNull(openItems);
        assertEquals(1, openItems.size());
        assertEquals("FrontDoor", openItems.get(0).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusCountsActiveLightsByTag() throws Exception {
        Item lightOn = mockItem("Kitchen_Light", "Kitchen Light", "Switch", OnOffType.ON, Set.of("Light"), List.of());
        Item lightOff = mockItem("Living_Light", "Living Light", "Dimmer", new PercentType(0), Set.of("Lightbulb"),
                List.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(lightOn, lightOff));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> lighting = (Map<String, Object>) parsed.get("lighting");
        assertNotNull(lighting);
        assertEquals(2, lighting.get("totalLights"));
        assertEquals(1, lighting.get("onCount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusCountsActiveLightsByNameHeuristic() throws Exception {
        Item lampOn = mockItem("desk_lamp", "Desk Lamp", "Switch", OnOffType.ON, Set.of(), List.of("gRoom"));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(lampOn));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> lighting = (Map<String, Object>) parsed.get("lighting");
        assertNotNull(lighting);
        assertEquals(1, lighting.get("totalLights"));
        assertEquals(1, lighting.get("onCount"));
        List<Map<String, Object>> activeLights = (List<Map<String, Object>>) lighting.get("activeLights");
        assertNotNull(activeLights);
        assertEquals("gRoom", activeLights.get(0).get("location"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusCountsDimmerAsActiveWhenNonZero() throws Exception {
        Item dimmer = mockItem("Bedroom_Light", "Bedroom Light", "Dimmer", new PercentType(50), Set.of("Light"),
                List.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(dimmer));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> lighting = (Map<String, Object>) parsed.get("lighting");
        assertNotNull(lighting);
        assertEquals(1, lighting.get("onCount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusReturnsTemperatureItems() throws Exception {
        Item tempByTag = mockItem("Sensor_Temp", "Outside Temp", "Number", new DecimalType(22), Set.of("Temperature"),
                List.of());
        Item tempByName = mockItem("indoor_temperature", "Indoor", "Number", new DecimalType(21), Set.of(), List.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(tempByTag, tempByName));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> climate = (Map<String, Object>) parsed.get("climate");
        assertNotNull(climate);
        List<Map<String, String>> temps = (List<Map<String, String>>) climate.get("temperatures");
        assertNotNull(temps);
        assertEquals(2, temps.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusReturnsEnergyItemsByTag() throws Exception {
        Item energyItem = mockItem("Solar_Energy", "Solar", "Number", new DecimalType(3500), Set.of("Energy"),
                List.of());
        Item powerItem = mockItem("Grid_Power", "Grid", "Number", new DecimalType(1200), Set.of("Power"), List.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(energyItem, powerItem));
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> energy = (Map<String, Object>) parsed.get("energy");
        assertNotNull(energy);
        List<Map<String, String>> items = (List<Map<String, String>>) energy.get("items");
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusReturnsOnlineOfflineCounts() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());
        Thing online1 = mockThing("t1", "Thing 1", ThingStatus.ONLINE, "hue");
        Thing online2 = mockThing("t2", "Thing 2", ThingStatus.ONLINE, "hue");
        Thing offline = mockThing("t3", "Broken Thing", ThingStatus.OFFLINE, "zwave");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(online1, online2, offline));

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> devices = (Map<String, Object>) parsed.get("devices");
        assertNotNull(devices);
        assertEquals(3, devices.get("totalThings"));
        assertEquals(2, devices.get("online"));
        assertEquals(1, devices.get("offline"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusListsOfflineThings() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());
        Thing offline = mockThing("t1", "Broken Thing", ThingStatus.OFFLINE, "zwave");
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of(offline));

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        Map<String, Object> devices = (Map<String, Object>) parsed.get("devices");
        assertNotNull(devices);
        List<Map<String, String>> offlineThings = (List<Map<String, String>>) devices.get("offlineThings");
        assertNotNull(offlineThings);
        assertEquals(1, offlineThings.size());
        assertTrue(offlineThings.get(0).get("uid").contains("t1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void homeStatusReturnsAllSections() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());
        when(Objects.requireNonNull(thingRegistry).getAll()).thenReturn(List.of());

        CallToolResult result = tools().handleGetHomeStatus(createRequest(Map.of()));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertNotNull(parsed.get("security"));
        assertNotNull(parsed.get("lighting"));
        assertNotNull(parsed.get("climate"));
        assertNotNull(parsed.get("energy"));
        assertNotNull(parsed.get("devices"));
    }
}
