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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link LinkTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class LinkToolsTest {

    @Mock
    @Nullable
    ItemChannelLinkRegistry linkRegistry;

    @Mock
    @Nullable
    ItemRegistry itemRegistry;

    @Mock
    @Nullable
    ThingRegistry thingRegistry;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    private @Nullable LinkTools linkTools;

    @BeforeEach
    void setUp() {
        linkTools = new LinkTools(Objects.requireNonNull(linkRegistry), Objects.requireNonNull(itemRegistry),
                Objects.requireNonNull(thingRegistry), jsonMapper);
    }

    private LinkTools tools() {
        LinkTools t = linkTools;
        assertNotNull(t);
        return t;
    }

    private ItemChannelLink mockLink(String itemName, String channelUIDStr) {
        ItemChannelLink link = mock(ItemChannelLink.class);
        lenient().when(link.getItemName()).thenReturn(itemName);
        lenient().when(link.getLinkedUID()).thenReturn(new ChannelUID(channelUIDStr));
        lenient().when(link.getConfiguration()).thenReturn(new Configuration());
        return link;
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLinksNoFiltersReturnsAll() throws Exception {
        ItemChannelLink link1 = mockLink("Item1", "hue:0210:bridge:lamp1:color");
        ItemChannelLink link2 = mockLink("Item2", "zwave:device:ctrl:node5:switch");
        when(Objects.requireNonNull(linkRegistry).getAll()).thenReturn(List.of(link1, link2));

        CallToolResult result = tools().handleGetLinks(createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> links = (List<Map<String, Object>>) parsed.get("links");
        assertNotNull(links);
        assertEquals(2, links.size());
        assertEquals(2, parsed.get("count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLinksFiltersByItemName() throws Exception {
        ItemChannelLink link1 = mockLink("Item1", "hue:0210:bridge:lamp1:color");
        ItemChannelLink link2 = mockLink("Item2", "zwave:device:ctrl:node5:switch");
        when(Objects.requireNonNull(linkRegistry).getAll()).thenReturn(List.of(link1, link2));

        CallToolResult result = tools().handleGetLinks(createRequest(Map.of("itemName", "Item1")));
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> links = (List<Map<String, Object>>) parsed.get("links");
        assertNotNull(links);
        assertEquals(1, links.size());
        assertEquals("Item1", links.get(0).get("itemName"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLinksFiltersByChannelPrefix() throws Exception {
        ItemChannelLink link1 = mockLink("Item1", "hue:0210:bridge:lamp1:color");
        ItemChannelLink link2 = mockLink("Item2", "zwave:device:ctrl:node5:switch");
        when(Objects.requireNonNull(linkRegistry).getAll()).thenReturn(List.of(link1, link2));

        CallToolResult result = tools().handleGetLinks(createRequest(Map.of("channelUID", "hue:")));
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> links = (List<Map<String, Object>>) parsed.get("links");
        assertNotNull(links);
        assertEquals(1, links.size());
        assertEquals("hue:0210:bridge:lamp1:color", links.get(0).get("channelUID"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createLinkSuccess() throws Exception {
        Item item = mock(Item.class);
        when(Objects.requireNonNull(itemRegistry).getItem("MyItem")).thenReturn(item);

        Channel channel = mock(Channel.class);
        ChannelUID channelUID = new ChannelUID("hue:0210:bridge:lamp1:color");
        when(Objects.requireNonNull(thingRegistry).getChannel(channelUID)).thenReturn(channel);

        Map<String, Object> args = new HashMap<>();
        args.put("itemName", "MyItem");
        args.put("channelUID", "hue:0210:bridge:lamp1:color");

        CallToolResult result = tools().handleCreateLink(createRequest(args));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
        assertEquals("MyItem", parsed.get("itemName"));
        assertEquals("hue:0210:bridge:lamp1:color", parsed.get("channelUID"));
        verify(Objects.requireNonNull(linkRegistry)).add(any(ItemChannelLink.class));
    }

    @Test
    void createLinkItemNotFound() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItem("Missing")).thenThrow(new ItemNotFoundException("Missing"));

        Map<String, Object> args = new HashMap<>();
        args.put("itemName", "Missing");
        args.put("channelUID", "hue:0210:bridge:lamp1:color");

        CallToolResult result = tools().handleCreateLink(createRequest(args));
        assertErrorContains(result, "not found");
    }

    @Test
    void createLinkChannelNotFound() throws Exception {
        Item item = mock(Item.class);
        when(Objects.requireNonNull(itemRegistry).getItem("MyItem")).thenReturn(item);

        ChannelUID channelUID = new ChannelUID("hue:0210:bridge:lamp1:missing");
        when(Objects.requireNonNull(thingRegistry).getChannel(channelUID)).thenReturn(null);

        Map<String, Object> args = new HashMap<>();
        args.put("itemName", "MyItem");
        args.put("channelUID", "hue:0210:bridge:lamp1:missing");

        CallToolResult result = tools().handleCreateLink(createRequest(args));
        assertErrorContains(result, "not found");
    }

    @Test
    void createLinkMissingParams() throws Exception {
        CallToolResult result = tools().handleCreateLink(createRequest(Map.of()));
        assertErrorContains(result, "required");
    }

    @Test
    void createLinkMissingChannelUID() throws Exception {
        CallToolResult result = tools().handleCreateLink(createRequest(Map.of("itemName", "MyItem")));
        assertErrorContains(result, "required");
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteLinkSuccess() throws Exception {
        String channelUIDStr = "hue:0210:bridge:lamp1:color";
        String linkId = ItemChannelLink.getIDFor("MyItem", new ChannelUID(channelUIDStr));
        ItemChannelLink link = mockLink("MyItem", channelUIDStr);
        when(Objects.requireNonNull(linkRegistry).remove(linkId)).thenReturn(link);

        Map<String, Object> args = new HashMap<>();
        args.put("itemName", "MyItem");
        args.put("channelUID", channelUIDStr);

        CallToolResult result = tools().handleDeleteLink(createRequest(args));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertEquals(true, parsed.get("success"));
    }

    @Test
    void deleteLinkNotFound() throws Exception {
        String channelUIDStr = "hue:0210:bridge:lamp1:color";
        String linkId = ItemChannelLink.getIDFor("MyItem", new ChannelUID(channelUIDStr));
        when(Objects.requireNonNull(linkRegistry).remove(linkId)).thenReturn(null);

        Map<String, Object> args = new HashMap<>();
        args.put("itemName", "MyItem");
        args.put("channelUID", channelUIDStr);

        CallToolResult result = tools().handleDeleteLink(createRequest(args));
        assertErrorContains(result, "not found");
    }

    @Test
    void deleteLinkMissingParams() throws Exception {
        CallToolResult result = tools().handleDeleteLink(createRequest(Map.of()));
        assertErrorContains(result, "required");
    }
}
