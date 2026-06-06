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
import java.util.Locale;
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
     * Returns the {@code manage_link} tool schema. Single CRUD entry point for item-channel
     * link management; dispatches on {@code action}.
     *
     * @return the tool definition
     */
    public McpSchema.Tool getManageLinkTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of("type", "string", "description",
                "get: list matching links; create: wire an item to a channel; delete: remove an item-channel link.",
                "enum", List.of("get", "create", "delete")));
        props.put("itemName", Map.of("type", "string", "description",
                "Item name. get: optional exact-match filter; create/delete: required."));
        props.put("channelUID", Map.of("type", "string", "description",
                "Channel UID (e.g. hue:0210:1:bulb1:color). get: optional prefix-match filter (pass a thing UID to "
                        + "see all its links); create/delete: required."));
        props.put("configuration", Map.of("type", "object", "description",
                "create only: optional link configuration properties (profile, etc.)."));

        return McpSchema.Tool.builder().name("manage_link").description("""
                List, create, or delete item-channel links. Action-dispatched: \
                action='get' returns matching links (filter by itemName exact and/or channelUID prefix; \
                use get_thing_details to find available channel UIDs); \
                action='create' wires an item to a channel (requires itemName + channelUID); \
                action='delete' removes the link (requires itemName + channelUID).""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("action"), null, null, null)).build();
    }

    /**
     * Handles a {@code manage_link} call. Dispatches to get / create / delete based on
     * the {@code action} argument.
     *
     * @param request the tool call request
     * @return the result for the dispatched action
     */
    public CallToolResult handleManageLink(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String action = getStringArg(args, "action");
        if (action == null || action.isBlank()) {
            return errorResult("'action' is required (one of: get, create, delete).");
        }
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "get" -> getLinks(args);
            case "create" -> createLink(args);
            case "delete" -> deleteLink(args);
            default -> errorResult("Invalid action '" + action + "'. Use one of: get, create, delete.");
        };
    }

    private CallToolResult getLinks(Map<String, Object> args) {
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

    private CallToolResult createLink(Map<String, Object> args) {
        String itemName = getStringArg(args, "itemName");
        String channelUIDStr = getStringArg(args, "channelUID");
        if (itemName == null || itemName.isBlank() || channelUIDStr == null || channelUIDStr.isBlank()) {
            return errorResult("'itemName' and 'channelUID' are required when action='create'.");
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
        result.put("action", "create");
        result.put("itemName", itemName);
        result.put("channelUID", channelUIDStr);
        return textResult(jsonMapper, result);
    }

    private CallToolResult deleteLink(Map<String, Object> args) {
        String itemName = getStringArg(args, "itemName");
        String channelUIDStr = getStringArg(args, "channelUID");
        if (itemName == null || itemName.isBlank() || channelUIDStr == null || channelUIDStr.isBlank()) {
            return errorResult("'itemName' and 'channelUID' are required when action='delete'.");
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
        result.put("action", "delete");
        result.put("itemName", itemName);
        result.put("channelUID", channelUIDStr);
        return textResult(jsonMapper, result);
    }
}
