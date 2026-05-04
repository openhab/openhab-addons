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
package org.openhab.io.mcp.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link SemanticModelBuilder}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class SemanticModelBuilderTest {

    @Mock
    @Nullable
    ItemRegistry itemRegistry;

    @Mock
    @Nullable
    MetadataRegistry metadataRegistry;

    private @Nullable SemanticModelBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SemanticModelBuilder(Objects.requireNonNull(itemRegistry), metadataRegistry);
    }

    private SemanticModelBuilder builder() {
        SemanticModelBuilder b = builder;
        assertNotNull(b);
        return b;
    }

    private GroupItem mockLocationGroup(String name, String label, Set<Item> members) {
        GroupItem group = mock(GroupItem.class);
        lenient().when(group.getName()).thenReturn(name);
        lenient().when(group.getLabel()).thenReturn(label);
        lenient().when(group.getType()).thenReturn("Group");
        lenient().when(group.getState()).thenReturn(UnDefType.NULL);
        lenient().when(group.getTags()).thenReturn(Set.of("Location"));
        lenient().when(group.getGroupNames()).thenReturn(List.of());
        lenient().when(group.getMembers()).thenReturn(members);
        return group;
    }

    private GroupItem mockEquipmentGroup(String name, String label, Set<Item> members) {
        GroupItem group = mock(GroupItem.class);
        lenient().when(group.getName()).thenReturn(name);
        lenient().when(group.getLabel()).thenReturn(label);
        lenient().when(group.getType()).thenReturn("Group");
        lenient().when(group.getState()).thenReturn(UnDefType.NULL);
        lenient().when(group.getTags()).thenReturn(Set.of("Equipment"));
        lenient().when(group.getGroupNames()).thenReturn(List.of());
        lenient().when(group.getMembers()).thenReturn(members);
        return group;
    }

    private Item mockPointItem(String name, String label, String itemType, State state) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        lenient().when(item.getType()).thenReturn(itemType);
        lenient().when(item.getState()).thenReturn(state);
        lenient().when(item.getTags()).thenReturn(Set.of("Point"));
        lenient().when(item.getGroupNames()).thenReturn(List.of());
        return item;
    }

    private Item mockPlainItem(String name, String label, String itemType, State state) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        lenient().when(item.getType()).thenReturn(itemType);
        lenient().when(item.getState()).thenReturn(state);
        lenient().when(item.getTags()).thenReturn(Set.of());
        lenient().when(item.getGroupNames()).thenReturn(List.of());
        return item;
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEmptyRegistry() {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());

        Map<String, Object> model = builder().buildModel(false);

        assertNotNull(model);
        List<Object> locations = (List<Object>) model.get("locations");
        assertNotNull(locations);
        assertTrue(locations.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSingleLocation() {
        GroupItem room = mockLocationGroup("Kitchen", "Kitchen", Set.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room));

        Map<String, Object> model = builder().buildModel(false);

        List<Map<String, Object>> locations = (List<Map<String, Object>>) model.get("locations");
        assertNotNull(locations);
        assertEquals(1, locations.size());
        assertEquals("Kitchen", locations.get(0).get("name"));
        assertEquals("Kitchen", locations.get(0).get("label"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLocationWithEquipment() {
        Item point = mockPointItem("Kitchen_Light_Switch", "Switch", "Switch", OnOffType.ON);
        GroupItem equipment = mockEquipmentGroup("Kitchen_Light", "Kitchen Light", Set.of(point));
        GroupItem room = mockLocationGroup("Kitchen", "Kitchen", Set.of(equipment));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room, equipment, point));

        Map<String, Object> model = builder().buildModel(false);

        List<Map<String, Object>> locations = (List<Map<String, Object>>) model.get("locations");
        assertNotNull(locations);
        assertEquals(1, locations.size());
        List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) locations.get(0).get("equipment");
        assertNotNull(equipmentList);
        assertEquals(1, equipmentList.size());
        assertEquals("Kitchen_Light", equipmentList.get(0).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEquipmentWithPoints() {
        Item dimmer = mockPointItem("Kitchen_Light_Dimmer", "Brightness", "Dimmer", new PercentType(75));
        GroupItem equipment = mockEquipmentGroup("Kitchen_Light", "Kitchen Light", Set.of(dimmer));
        GroupItem room = mockLocationGroup("Kitchen", "Kitchen", Set.of(equipment));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room, equipment, dimmer));

        Map<String, Object> model = builder().buildModel(false);

        List<Map<String, Object>> locations = (List<Map<String, Object>>) model.get("locations");
        assertNotNull(locations);
        assertEquals(1, locations.size());
        List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) locations.get(0).get("equipment");
        assertNotNull(equipmentList);
        assertEquals(1, equipmentList.size());
        List<Map<String, Object>> points = (List<Map<String, Object>>) equipmentList.get(0).get("points");
        assertNotNull(points);
        assertEquals(1, points.size());
        assertEquals("Kitchen_Light_Dimmer", points.get(0).get("itemName"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNestedSubLocations() {
        GroupItem childRoom = mockLocationGroup("Kitchen_Pantry", "Pantry", Set.of());
        GroupItem parentRoom = mockLocationGroup("Kitchen", "Kitchen", Set.of(childRoom));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(parentRoom, childRoom));

        Map<String, Object> model = builder().buildModel(false);

        List<Map<String, Object>> locations = (List<Map<String, Object>>) model.get("locations");
        assertNotNull(locations);
        assertFalse(locations.isEmpty());
        // Both locations should be represented somewhere in the model (either nested or flat,
        // depending on iteration order of the internal Set)
        boolean foundKitchen = false;
        boolean foundPantry = false;
        for (Map<String, Object> loc : locations) {
            if ("Kitchen".equals(loc.get("name"))) {
                foundKitchen = true;
            }
            if ("Kitchen_Pantry".equals(loc.get("name"))) {
                foundPantry = true;
            }
            Object subLocs = loc.get("subLocations");
            if (subLocs instanceof List<?> subList) {
                for (Object sub : subList) {
                    if (sub instanceof Map<?, ?> subMap && "Kitchen_Pantry".equals(subMap.get("name"))) {
                        foundPantry = true;
                    }
                }
            }
        }
        assertTrue(foundKitchen, "Kitchen should be in locations");
        assertTrue(foundPantry, "Pantry should be in locations or nested under Kitchen");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testLoosePointUnderLocation() {
        Item point = mockPointItem("Kitchen_Temp", "Temperature", "Number", new DecimalType(21.5));
        GroupItem room = mockLocationGroup("Kitchen", "Kitchen", Set.of(point));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room, point));

        Map<String, Object> model = builder().buildModel(false);

        List<Map<String, Object>> locations = (List<Map<String, Object>>) model.get("locations");
        assertNotNull(locations);
        assertEquals(1, locations.size());
        List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) locations.get(0).get("equipment");
        assertNotNull(equipmentList, "Loose point should appear as implicit equipment");
        assertEquals(1, equipmentList.size());
        assertEquals("Kitchen_Temp", equipmentList.get(0).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHasLocationMetadata() {
        Item sensor = mockPointItem("Outdoor_Temp", "Outdoor Temperature", "Number", new DecimalType(15));
        GroupItem patio = mockLocationGroup("Patio", "Patio", Set.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(patio, sensor));

        MetadataKey mdKey = new MetadataKey("semantics", "Outdoor_Temp");
        Metadata md = new Metadata(mdKey, "", Map.of("hasLocation", "Patio"));
        MetadataRegistry reg = Objects.requireNonNull(metadataRegistry);
        when(reg.get(any(MetadataKey.class))).thenAnswer(invocation -> {
            MetadataKey key = invocation.getArgument(0);
            if ("semantics".equals(key.getNamespace()) && "Outdoor_Temp".equals(key.getItemName())) {
                return md;
            }
            return null;
        });

        Map<String, Object> model = builder().buildModel(false);

        List<Map<String, Object>> locations = (List<Map<String, Object>>) model.get("locations");
        assertNotNull(locations);
        assertEquals(1, locations.size());
        List<Map<String, Object>> equipmentList = (List<Map<String, Object>>) locations.get(0).get("equipment");
        assertNotNull(equipmentList);
        assertEquals(1, equipmentList.size());
        assertEquals("Outdoor_Temp", equipmentList.get(0).get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIncludeUntaggedTrue() {
        GroupItem room = mockLocationGroup("Kitchen", "Kitchen", Set.of());
        Item orphan = mockPlainItem("Orphan_Item", "Orphan", "String", new StringType("test"));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room, orphan));

        Map<String, Object> model = builder().buildModel(true);

        List<Map<String, Object>> unassigned = (List<Map<String, Object>>) model.get("unassignedItems");
        assertNotNull(unassigned);
        assertTrue(unassigned.stream().anyMatch(m -> "Orphan_Item".equals(m.get("itemName"))));
    }

    @Test
    void testIncludeUntaggedFalse() {
        GroupItem room = mockLocationGroup("Kitchen", "Kitchen", Set.of());
        Item orphan = mockPlainItem("Orphan_Item", "Orphan", "String", new StringType("test"));
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room, orphan));

        Map<String, Object> model = builder().buildModel(false);

        assertNull(model.get("unassignedItems"));
    }
}
