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
import static org.mockito.ArgumentMatchers.*;
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
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link ItemTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ItemToolsTest {

    private @Mock @Nullable ItemRegistry itemRegistry;
    private @Mock @Nullable ItemBuilderFactory itemBuilderFactory;
    private @Mock @Nullable MetadataRegistry metadataRegistry;
    private @Mock @Nullable EventPublisher eventPublisher;

    private @Nullable ItemTools itemTools;
    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    @BeforeEach
    void setUp() {
        itemTools = new ItemTools(Objects.requireNonNull(itemRegistry), Objects.requireNonNull(itemBuilderFactory),
                metadataRegistry, Objects.requireNonNull(eventPublisher), jsonMapper);
    }

    private ItemTools tools() {
        return Objects.requireNonNull(itemTools);
    }

    private Item mockSwitchItem(String name, String label, State state) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        lenient().when(item.getType()).thenReturn("Switch");
        lenient().when(item.getState()).thenReturn(state);
        lenient().when(item.getTags()).thenReturn(Set.of());
        lenient().when(item.getGroupNames()).thenReturn(List.of());
        lenient().when(item.getAcceptedCommandTypes()).thenReturn(List.of(OnOffType.class));
        lenient().when(item.getAcceptedDataTypes()).thenReturn(List.of(OnOffType.class, UnDefType.class));
        lenient().when(item.hasTag(anyString())).thenReturn(false);
        return item;
    }

    private Item mockDimmerItem(String name, String label, State state, Set<String> tags) {
        Item item = mock(Item.class);
        lenient().when(item.getName()).thenReturn(name);
        lenient().when(item.getLabel()).thenReturn(label);
        lenient().when(item.getType()).thenReturn("Dimmer");
        lenient().when(item.getState()).thenReturn(state);
        lenient().when(item.getTags()).thenReturn(tags);
        lenient().when(item.getGroupNames()).thenReturn(List.of());
        lenient().when(item.getAcceptedCommandTypes()).thenReturn(List.of(OnOffType.class, PercentType.class));
        lenient().when(item.getAcceptedDataTypes())
                .thenReturn(List.of(PercentType.class, OnOffType.class, UnDefType.class));
        for (String tag : tags) {
            lenient().when(item.hasTag(tag)).thenReturn(true);
        }
        return item;
    }

    private McpSchema.CallToolRequest request(Map<String, Object> args) {
        return new McpSchema.CallToolRequest("test", args);
    }

    @Test
    void getItemFound() throws Exception {
        Item item = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        when(Objects.requireNonNull(itemRegistry).getItem("Kitchen_Light")).thenReturn(item);

        CallToolResult result = tools().handleGetItem(request(Map.of("itemNames", List.of("Kitchen_Light"))));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Kitchen_Light", items.get(0).get("name"));
    }

    @Test
    void getItemMultipleItems() throws Exception {
        Item item1 = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        Item item2 = mockSwitchItem("Living_Light", "Living Room Light", OnOffType.OFF);
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("Kitchen_Light")).thenReturn(item1);
        when(registry.getItem("Living_Light")).thenReturn(item2);

        CallToolResult result = tools()
                .handleGetItem(request(Map.of("itemNames", List.of("Kitchen_Light", "Living_Light"))));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    void getItemNotFoundWithSuggestions() throws Exception {
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("Kitchen")).thenThrow(new ItemNotFoundException("Kitchen"));
        Item suggestion = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        when(registry.getItems()).thenReturn(List.of(suggestion));

        CallToolResult result = tools().handleGetItem(request(Map.of("itemNames", List.of("Kitchen"))));

        assertError(result);
        String text = extractText(result);
        assertTrue(text.contains("not found"));
        assertTrue(text.contains("Kitchen_Light"));
    }

    @Test
    void getItemPartialNotFound() throws Exception {
        Item item1 = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("Kitchen_Light")).thenReturn(item1);
        when(registry.getItem("NoSuchItem")).thenThrow(new ItemNotFoundException("NoSuchItem"));

        CallToolResult result = tools()
                .handleGetItem(request(Map.of("itemNames", List.of("Kitchen_Light", "NoSuchItem"))));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertNotNull(items);
        assertEquals(1, items.size());
        @SuppressWarnings("unchecked")
        List<String> notFound = (List<String>) parsed.get("notFound");
        assertNotNull(notFound);
        assertTrue(notFound.contains("NoSuchItem"));
    }

    @Test
    void getItemMissingParameter() {
        CallToolResult result = tools().handleGetItem(request(Map.of()));

        assertError(result);
        assertErrorContains(result, "itemNames");
    }

    @Test
    void getItemEmptyList() {
        CallToolResult result = tools().handleGetItem(request(Map.of("itemNames", List.of())));

        assertError(result);
        assertErrorContains(result, "itemNames");
    }

    @Test
    void searchItemsNoFiltersReturnsAll() throws Exception {
        Item item1 = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        Item item2 = mockSwitchItem("Living_Light", "Living Room Light", OnOffType.OFF);
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(item1, item2));

        CallToolResult result = tools().handleSearchItems(request(Map.of()));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(2, parsed.get("total"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertEquals(2, items.size());
    }

    @Test
    void searchItemsQueryFilter() throws Exception {
        Item matching = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        Item nonMatching = mockSwitchItem("XYZ_Sensor", "XYZ Sensor", UnDefType.NULL);
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(matching, nonMatching));

        CallToolResult result = tools().handleSearchItems(request(Map.of("query", "kitchen light")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals("Kitchen_Light", items.get(0).get("name"));
    }

    @Test
    void searchItemsTypeFilter() throws Exception {
        Item switchItem = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        Item dimmerItem = mockDimmerItem("Kitchen_Dimmer", "Kitchen Dimmer", new PercentType(50), Set.of());
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(switchItem, dimmerItem));

        CallToolResult result = tools().handleSearchItems(request(Map.of("type", "Dimmer")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertEquals("Kitchen_Dimmer", items.get(0).get("name"));
    }

    @Test
    void searchItemsTagFilter() throws Exception {
        Item tagged = mockDimmerItem("Kitchen_Dimmer", "Kitchen Dimmer", new PercentType(50), Set.of("Lighting"));
        Item untagged = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(tagged, untagged));

        CallToolResult result = tools().handleSearchItems(request(Map.of("tags", List.of("Lighting"))));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertEquals("Kitchen_Dimmer", items.get(0).get("name"));
    }

    @Test
    void searchItemsGroupFilterRecursive() throws Exception {
        Item member = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        GroupItem groupItem = mock(GroupItem.class);
        lenient().when(groupItem.getName()).thenReturn("gLights");
        lenient().when(groupItem.getLabel()).thenReturn("All Lights");
        lenient().when(groupItem.getType()).thenReturn("Group");
        lenient().when(groupItem.getState()).thenReturn(UnDefType.NULL);
        lenient().when(groupItem.getTags()).thenReturn(Set.of());
        lenient().when(groupItem.getGroupNames()).thenReturn(List.of());
        lenient().when(groupItem.getAcceptedCommandTypes()).thenReturn(List.of());
        lenient().when(groupItem.getAllMembers()).thenReturn(Set.of(member));
        lenient().when(groupItem.getMembers()).thenReturn(Set.of(member));

        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("gLights")).thenReturn(groupItem);

        CallToolResult result = tools().handleSearchItems(request(Map.of("group", "gLights", "recursive", true)));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertEquals("Kitchen_Light", items.get(0).get("name"));
    }

    @Test
    void searchItemsGroupFilterNonRecursive() throws Exception {
        Item directMember = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        Item nestedMember = mockSwitchItem("Nested_Light", "Nested Light", OnOffType.OFF);

        GroupItem groupItem = mock(GroupItem.class);
        lenient().when(groupItem.getName()).thenReturn("gLights");
        lenient().when(groupItem.getType()).thenReturn("Group");
        lenient().when(groupItem.getState()).thenReturn(UnDefType.NULL);
        lenient().when(groupItem.getTags()).thenReturn(Set.of());
        lenient().when(groupItem.getGroupNames()).thenReturn(List.of());
        lenient().when(groupItem.getAcceptedCommandTypes()).thenReturn(List.of());
        lenient().when(groupItem.getMembers()).thenReturn(Set.of(directMember));
        lenient().when(groupItem.getAllMembers()).thenReturn(Set.of(directMember, nestedMember));

        when(Objects.requireNonNull(itemRegistry).getItem("gLights")).thenReturn(groupItem);

        CallToolResult result = tools().handleSearchItems(request(Map.of("group", "gLights", "recursive", false)));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
    }

    @Test
    void searchItemsGroupNotFound() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItem("gMissing")).thenThrow(new ItemNotFoundException("gMissing"));

        CallToolResult result = tools().handleSearchItems(request(Map.of("group", "gMissing")));

        assertError(result);
        assertErrorContains(result, "not found");
    }

    @Test
    void searchItemsGroupNotAGroupItem() throws Exception {
        Item notAGroup = mockSwitchItem("NotAGroup", "Not A Group", OnOffType.ON);
        when(Objects.requireNonNull(itemRegistry).getItem("NotAGroup")).thenReturn(notAGroup);

        CallToolResult result = tools().handleSearchItems(request(Map.of("group", "NotAGroup")));

        assertError(result);
        assertErrorContains(result, "not a group");
    }

    @Test
    void searchItemsPagination() throws Exception {
        Item item1 = mockSwitchItem("A_Light", "A Light", OnOffType.ON);
        Item item2 = mockSwitchItem("B_Light", "B Light", OnOffType.OFF);
        Item item3 = mockSwitchItem("C_Light", "C Light", OnOffType.ON);
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(item1, item2, item3));

        Map<String, Object> args = new HashMap<>();
        args.put("limit", 2);
        args.put("offset", 1);
        CallToolResult result = tools().handleSearchItems(request(args));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(3, parsed.get("total"));
        assertEquals(1, parsed.get("offset"));
        assertEquals(2, parsed.get("limit"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) parsed.get("items");
        assertEquals(2, items.size());
    }

    @Test
    void sendCommandSuccess() throws Exception {
        Item item = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.OFF);
        when(Objects.requireNonNull(itemRegistry).getItem("Kitchen_Light")).thenReturn(item);

        CallToolResult result = tools()
                .handleSendCommand(request(Map.of("itemName", "Kitchen_Light", "command", "ON")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("Kitchen_Light", parsed.get("itemName"));
        assertEquals("ON", parsed.get("command"));
        assertEquals("OFF", parsed.get("previousState"));
        verify(Objects.requireNonNull(eventPublisher)).post(
                argThat(event -> event.getTopic().contains("Kitchen_Light") && event.getTopic().contains("command")));
    }

    @Test
    void sendCommandItemNotFound() throws Exception {
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("NoSuchItem")).thenThrow(new ItemNotFoundException("NoSuchItem"));
        when(registry.getItems()).thenReturn(List.of());

        CallToolResult result = tools().handleSendCommand(request(Map.of("itemName", "NoSuchItem", "command", "ON")));

        assertError(result);
        assertErrorContains(result, "not found");
    }

    @Test
    void sendCommandUnparseableCommand() throws Exception {
        Item item = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.OFF);
        when(Objects.requireNonNull(itemRegistry).getItem("Kitchen_Light")).thenReturn(item);

        CallToolResult result = tools()
                .handleSendCommand(request(Map.of("itemName", "Kitchen_Light", "command", "INVALID_CMD")));

        assertError(result);
        assertErrorContains(result, "Cannot parse command");
        assertErrorContains(result, "Accepted command types");
    }

    @Test
    void sendCommandStripsPercentSuffix() throws Exception {
        Item item = mockDimmerItem("Kitchen_Dimmer", "Kitchen Dimmer", new PercentType(30), Set.of());
        when(Objects.requireNonNull(itemRegistry).getItem("Kitchen_Dimmer")).thenReturn(item);

        CallToolResult result = tools()
                .handleSendCommand(request(Map.of("itemName", "Kitchen_Dimmer", "command", "50%")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("50", parsed.get("command"));
        verify(Objects.requireNonNull(eventPublisher)).post(any());
    }

    @Test
    void sendCommandMissingParameters() {
        CallToolResult result = tools().handleSendCommand(request(Map.of()));

        assertError(result);
        assertErrorContains(result, "required");
    }

    @Test
    void updateStateSuccess() throws Exception {
        Item item = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.OFF);
        when(Objects.requireNonNull(itemRegistry).getItem("Kitchen_Light")).thenReturn(item);

        CallToolResult result = tools().handleUpdateState(request(Map.of("itemName", "Kitchen_Light", "state", "ON")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("Kitchen_Light", parsed.get("itemName"));
        assertEquals("ON", parsed.get("state"));
        verify(Objects.requireNonNull(eventPublisher)).post(any());
    }

    @Test
    void updateStateItemNotFound() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItem("NoSuchItem"))
                .thenThrow(new ItemNotFoundException("NoSuchItem"));

        CallToolResult result = tools().handleUpdateState(request(Map.of("itemName", "NoSuchItem", "state", "ON")));

        assertError(result);
        assertErrorContains(result, "not found");
    }

    @Test
    void updateStateMissingParameters() {
        CallToolResult result = tools().handleUpdateState(request(Map.of()));

        assertError(result);
        assertErrorContains(result, "required");
    }

    @Test
    void createItemSuccess() throws Exception {
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("New_Switch")).thenThrow(new ItemNotFoundException("New_Switch"));

        Item builtItem = mockSwitchItem("New_Switch", "", OnOffType.OFF);
        ItemBuilder builder = mock(ItemBuilder.class);
        when(Objects.requireNonNull(itemBuilderFactory).newItemBuilder("Switch", "New_Switch")).thenReturn(builder);
        when(builder.build()).thenReturn(builtItem);

        CallToolResult result = tools().handleCreateItem(request(Map.of("name", "New_Switch", "type", "Switch")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("New_Switch", parsed.get("name"));
        assertEquals("Switch", parsed.get("type"));
        verify(registry).add(builtItem);
    }

    @Test
    void createItemWithAllOptions() throws Exception {
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("Kitchen_Dimmer")).thenThrow(new ItemNotFoundException("Kitchen_Dimmer"));

        Item builtItem = mockDimmerItem("Kitchen_Dimmer", "Kitchen Dimmer", UnDefType.NULL, Set.of("Lighting"));
        ItemBuilder builder = mock(ItemBuilder.class);
        when(Objects.requireNonNull(itemBuilderFactory).newItemBuilder("Dimmer", "Kitchen_Dimmer")).thenReturn(builder);
        when(builder.withLabel("Kitchen Dimmer")).thenReturn(builder);
        when(builder.withCategory("light")).thenReturn(builder);
        when(builder.withTags(Set.of("Lighting"))).thenReturn(builder);
        when(builder.withGroups(List.of("gKitchen"))).thenReturn(builder);
        when(builder.build()).thenReturn(builtItem);

        Map<String, Object> args = new HashMap<>();
        args.put("name", "Kitchen_Dimmer");
        args.put("type", "Dimmer");
        args.put("label", "Kitchen Dimmer");
        args.put("category", "light");
        args.put("tags", List.of("Lighting"));
        args.put("groupNames", List.of("gKitchen"));
        CallToolResult result = tools().handleCreateItem(request(args));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("Kitchen Dimmer", parsed.get("label"));
        verify(builder).withLabel("Kitchen Dimmer");
        verify(builder).withCategory("light");
        verify(builder).withTags(Set.of("Lighting"));
        verify(builder).withGroups(List.of("gKitchen"));
    }

    @Test
    void createItemAlreadyExists() throws Exception {
        Item existing = mockSwitchItem("Existing_Item", "Existing", OnOffType.ON);
        when(Objects.requireNonNull(itemRegistry).getItem("Existing_Item")).thenReturn(existing);

        CallToolResult result = tools().handleCreateItem(request(Map.of("name", "Existing_Item", "type", "Switch")));

        assertError(result);
        assertErrorContains(result, "already exists");
    }

    @Test
    void createItemMissingParameters() {
        CallToolResult result = tools().handleCreateItem(request(Map.of()));

        assertError(result);
        assertErrorContains(result, "required");
    }

    @Test
    void updateItemSuccess() throws Exception {
        Item existing = mockSwitchItem("Kitchen_Light", "Kitchen Light", OnOffType.ON);
        ItemRegistry registry = Objects.requireNonNull(itemRegistry);
        when(registry.getItem("Kitchen_Light")).thenReturn(existing);

        Item updatedItem = mockSwitchItem("Kitchen_Light", "New Label", OnOffType.ON);
        lenient().when(updatedItem.getTags()).thenReturn(Set.of());
        lenient().when(updatedItem.getGroupNames()).thenReturn(List.of());

        ItemBuilder builder = mock(ItemBuilder.class);
        when(Objects.requireNonNull(itemBuilderFactory).newItemBuilder(existing)).thenReturn(builder);
        when(builder.withLabel("New Label")).thenReturn(builder);
        when(builder.build()).thenReturn(updatedItem);

        CallToolResult result = tools()
                .handleUpdateItem(request(Map.of("name", "Kitchen_Light", "label", "New Label")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("Kitchen_Light", parsed.get("name"));
        verify(registry).update(updatedItem);
    }

    @Test
    void updateItemNotFound() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItem("NoSuchItem"))
                .thenThrow(new ItemNotFoundException("NoSuchItem"));

        CallToolResult result = tools().handleUpdateItem(request(Map.of("name", "NoSuchItem", "label", "Label")));

        assertError(result);
        assertErrorContains(result, "not found");
    }

    @Test
    void updateItemMissingName() {
        CallToolResult result = tools().handleUpdateItem(request(Map.of()));

        assertError(result);
        assertErrorContains(result, "required");
    }

    @Test
    void deleteItemSuccess() throws Exception {
        Item removed = mockSwitchItem("Old_Item", "Old Item", OnOffType.OFF);
        when(Objects.requireNonNull(itemRegistry).remove("Old_Item")).thenReturn(removed);

        CallToolResult result = tools().handleDeleteItem(request(Map.of("name", "Old_Item")));

        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("Old_Item", parsed.get("name"));
    }

    @Test
    void deleteItemNotFound() {
        when(Objects.requireNonNull(itemRegistry).remove("NoSuchItem")).thenReturn(null);

        CallToolResult result = tools().handleDeleteItem(request(Map.of("name", "NoSuchItem")));

        assertError(result);
        assertErrorContains(result, "not found");
    }

    @Test
    void deleteItemMissingName() {
        CallToolResult result = tools().handleDeleteItem(request(Map.of()));

        assertError(result);
        assertErrorContains(result, "required");
    }
}
