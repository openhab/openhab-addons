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

import static org.openhab.io.mcp.internal.tools.McpToolUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for managing item-channel links.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkTools {

    private final Logger logger = LoggerFactory.getLogger(LinkTools.class);

    private final ItemChannelLinkRegistry linkRegistry;
    private final ItemRegistry itemRegistry;
    private final ThingRegistry thingRegistry;
    private final McpJsonMapper jsonMapper;

    /**
     * @param linkRegistry registry for item-channel link CRUD
     * @param itemRegistry registry for validating item existence
     * @param thingRegistry registry for validating channel existence
     * @param jsonMapper MCP JSON mapper for serializing responses
     */
    public LinkTools(ItemChannelLinkRegistry linkRegistry, ItemRegistry itemRegistry, ThingRegistry thingRegistry,
            McpJsonMapper jsonMapper) {
        this.linkRegistry = linkRegistry;
        this.itemRegistry = itemRegistry;
        this.thingRegistry = thingRegistry;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns the {@code get_links} tool schema. Lists item-channel links with
     * optional filtering by item name or channel UID prefix.
     *
     * @return the tool definition
     */
    public McpSchema.Tool getLinksTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("itemName", Map.of("type", "string", "description", "Filter links for this item name (exact match)"));
        props.put("channelUID", Map.of("type", "string", "description",
                "Filter links whose channel UID starts with this value (prefix match — use a thing UID to see all its links)"));

        return McpSchema.Tool.builder().name("get_links")
                .description("List item-channel links. Filter by item name and/or channel UID prefix. "
                        + "Use this to see how items are wired to thing channels before creating or removing links.")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_links} call. Returns matching links with their item name,
     * channel UID, and any link-level configuration.
     *
     * @param request the tool call request
     * @return links matching the filter criteria
     */
    public CallToolResult handleGetLinks(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String itemName = getStringArg(args, "itemName");
        String channelPrefix = getStringArg(args, "channelUID");

        List<Map<String, Object>> results = new ArrayList<>();
        for (ItemChannelLink link : linkRegistry.getAll()) {
            if (itemName != null && !link.getItemName().equals(itemName)) {
                continue;
            }
            if (channelPrefix != null && !link.getLinkedUID().toString().startsWith(channelPrefix)) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("itemName", link.getItemName());
            entry.put("channelUID", link.getLinkedUID().toString());
            Map<String, Object> cfg = link.getConfiguration().getProperties();
            if (!cfg.isEmpty()) {
                entry.put("configuration", cfg);
            }
            results.add(entry);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("links", results);
        response.put("count", results.size());
        return textResult(jsonMapper, response);
    }

    /**
     * Returns the {@code create_link} tool schema. Wires an item to a thing channel.
     *
     * @return the tool definition
     */
    public McpSchema.Tool getCreateLinkTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("itemName", Map.of("type", "string", "description", "Name of the item to link"));
        props.put("channelUID",
                Map.of("type", "string", "description", "Full channel UID (e.g. hue:0210:1:bulb1:color)"));
        props.put("configuration",
                Map.of("type", "object", "description", "Optional link configuration properties (profile, etc.)"));

        return McpSchema.Tool.builder().name("create_link")
                .description("Create an item-channel link to wire an item to a thing's channel. "
                        + "Use get_thing_details to find available channel UIDs, and get_links to check existing wiring.")
                .inputSchema(
                        new McpSchema.JsonSchema("object", props, List.of("itemName", "channelUID"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code create_link} call. Validates that both the item and channel
     * exist before creating the link.
     *
     * @param request the tool call request
     * @return success confirmation or an error if item/channel not found
     */
    @SuppressWarnings("unchecked")
    public CallToolResult handleCreateLink(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String itemName = getStringArg(args, "itemName");
        String channelUIDStr = getStringArg(args, "channelUID");
        if (itemName == null || channelUIDStr == null) {
            return errorResult("'itemName' and 'channelUID' are required.");
        }

        try {
            itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return errorResult("Item '" + itemName + "' not found.");
        }

        ChannelUID channelUID = new ChannelUID(channelUIDStr);
        if (thingRegistry.getChannel(channelUID) == null) {
            return errorResult("Channel '" + channelUIDStr + "' not found.");
        }

        Configuration config = new Configuration();
        Object cfgObj = args.get("configuration");
        if (cfgObj instanceof Map<?, ?> cfgMap) {
            cfgMap.forEach((k, v) -> config.put(String.valueOf(k), v));
        }

        ItemChannelLink link = new ItemChannelLink(itemName, channelUID, config);
        linkRegistry.add(link);
        logger.debug("Created link {} <-> {}", itemName, channelUIDStr);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("itemName", itemName);
        result.put("channelUID", channelUIDStr);
        return textResult(jsonMapper, result);
    }

    /**
     * Returns the {@code delete_link} tool schema. Removes a specific item-channel link.
     *
     * @return the tool definition
     */
    public McpSchema.Tool getDeleteLinkTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("itemName", Map.of("type", "string", "description", "Name of the linked item"));
        props.put("channelUID", Map.of("type", "string", "description", "Full channel UID of the link to remove"));

        return McpSchema.Tool.builder().name("delete_link")
                .description("Remove an item-channel link. Use get_links to find existing links first.")
                .inputSchema(
                        new McpSchema.JsonSchema("object", props, List.of("itemName", "channelUID"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code delete_link} call. Returns an error if the link doesn't exist.
     *
     * @param request the tool call request
     * @return success confirmation or an error if link not found
     */
    public CallToolResult handleDeleteLink(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String itemName = getStringArg(args, "itemName");
        String channelUIDStr = getStringArg(args, "channelUID");
        if (itemName == null || channelUIDStr == null) {
            return errorResult("'itemName' and 'channelUID' are required.");
        }

        String linkId = ItemChannelLink.getIDFor(itemName, new ChannelUID(channelUIDStr));
        @Nullable
        ItemChannelLink removed = linkRegistry.remove(linkId);
        if (removed == null) {
            return errorResult("Link between '" + itemName + "' and '" + channelUIDStr + "' not found.");
        }
        logger.debug("Deleted link {} <-> {}", itemName, channelUIDStr);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("itemName", itemName);
        result.put("channelUID", channelUIDStr);
        return textResult(jsonMapper, result);
    }
}
