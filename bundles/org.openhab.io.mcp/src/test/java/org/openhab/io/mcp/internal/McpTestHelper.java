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
package org.openhab.io.mcp.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Shared test helpers for MCP binding unit tests.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class McpTestHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static McpJsonMapper newJsonMapper() {
        return new JacksonMcpJsonMapper(new ObjectMapper());
    }

    public static McpSchema.CallToolRequest createRequest(Map<String, Object> args) {
        return new McpSchema.CallToolRequest("test", args);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseResult(CallToolResult result) throws Exception {
        String text = extractText(result);
        return MAPPER.readValue(text, new TypeReference<Map<String, Object>>() {
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseResultList(CallToolResult result) throws Exception {
        String text = extractText(result);
        return MAPPER.readValue(text, new TypeReference<List<Object>>() {
        });
    }

    public static String extractText(CallToolResult result) {
        assertNotNull(result.content());
        assertFalse(result.content().isEmpty(), "Result content should not be empty");
        McpSchema.Content first = result.content().get(0);
        assertInstanceOf(McpSchema.TextContent.class, first);
        return ((McpSchema.TextContent) first).text();
    }

    public static void assertSuccess(CallToolResult result) {
        assertTrue(result.isError() == null || !result.isError(),
                "Expected success but got error: " + extractText(result));
    }

    public static void assertError(CallToolResult result) {
        assertNotNull(result.isError());
        assertTrue(result.isError(), "Expected error result");
    }

    public static void assertErrorContains(CallToolResult result, String substring) {
        assertError(result);
        String text = extractText(result);
        assertTrue(text.contains(substring), "Expected error containing '" + substring + "' but got: " + text);
    }

    public static Item createMockItem(String name, String label, String type, State state, Set<String> tags,
            List<String> groups) {
        Item item = mock(Item.class);
        when(item.getName()).thenReturn(name);
        when(item.getLabel()).thenReturn(label);
        when(item.getType()).thenReturn(type);
        when(item.getState()).thenReturn(state);
        when(item.getTags()).thenReturn(tags);
        when(item.getGroupNames()).thenReturn(groups);
        when(item.getAcceptedCommandTypes()).thenReturn(List.of());
        return item;
    }

    public static GroupItem createMockGroupItem(String name, String label, Set<Item> members) {
        GroupItem item = mock(GroupItem.class);
        when(item.getName()).thenReturn(name);
        when(item.getLabel()).thenReturn(label);
        when(item.getType()).thenReturn("Group");
        when(item.getState()).thenReturn(UnDefType.NULL);
        when(item.getTags()).thenReturn(Set.of());
        when(item.getGroupNames()).thenReturn(List.of());
        when(item.getAcceptedCommandTypes()).thenReturn(List.of());
        when(item.getMembers()).thenReturn(members);
        return item;
    }

    public static Thing createMockThing(String uid, String label, ThingStatus status, String bindingId) {
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
}
