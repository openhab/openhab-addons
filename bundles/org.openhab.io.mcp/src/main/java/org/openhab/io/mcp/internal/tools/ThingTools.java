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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.Item;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.io.mcp.internal.util.ItemStateFormatter;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for thing status and details.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ThingTools {

    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry linkRegistry;
    private final McpJsonMapper jsonMapper;

    /**
     * Creates a new {@code ThingTools} instance.
     *
     * @param thingRegistry the registry used to look up things
     * @param linkRegistry the registry used to resolve item-channel links
     * @param jsonMapper the JSON mapper for serializing tool results
     */
    public ThingTools(ThingRegistry thingRegistry, ItemChannelLinkRegistry linkRegistry, McpJsonMapper jsonMapper) {
        this.thingRegistry = thingRegistry;
        this.linkRegistry = linkRegistry;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns the {@code get_things} tool schema.
     * This tool lists things with their online/offline status, with optional filtering by status or binding.
     *
     * @return the MCP tool definition for listing things
     */
    public McpSchema.Tool getThingsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("status",
                Map.of("type", "string", "description", "Filter by status: ONLINE, OFFLINE, UNKNOWN, INITIALIZING"));
        properties.put("bindingId",
                Map.of("type", "string", "description", "Filter by binding ID (e.g., 'hue', 'zwave', 'mqtt')"));
        properties.put("limit", Map.of("type", "integer", "description", "Maximum results to return (default: 50)"));
        properties.put("offset", Map.of("type", "integer", "description", "Number of results to skip (default: 0)"));

        return McpSchema.Tool.builder().name("get_things").description(
                "List things (device connections) with their online/offline status. Filter by status or binding type.")
                .inputSchema(new McpSchema.JsonSchema("object", properties, List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_things} call.
     * Retrieves a paginated list of things, optionally filtered by status or binding ID.
     *
     * @param request the incoming tool call request containing filter and pagination arguments
     * @return a result containing the matching things and pagination metadata
     */
    public CallToolResult handleGetThings(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String statusFilter = getStringArg(args, "status");
        String bindingFilter = getStringArg(args, "bindingId");
        int limit = getIntArg(args, "limit", 50);
        int offset = getIntArg(args, "offset", 0);

        List<Map<String, Object>> things = new ArrayList<>();
        for (Thing thing : thingRegistry.getAll()) {
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            if (statusFilter != null && !statusInfo.getStatus().name().equalsIgnoreCase(statusFilter)) {
                continue;
            }
            if (bindingFilter != null && !thing.getThingTypeUID().getBindingId().equalsIgnoreCase(bindingFilter)) {
                continue;
            }
            things.add(buildThingSummary(thing));
        }

        int total = things.size();
        List<Map<String, Object>> page = things.subList(Math.min(offset, total), Math.min(offset + limit, total));

        Map<String, Object> response = new HashMap<>();
        response.put("things", page);
        response.put("total", total);
        response.put("offset", offset);
        response.put("limit", limit);
        return textResult(jsonMapper, response);
    }

    /**
     * Returns the {@code get_thing_details} tool schema.
     * This tool retrieves detailed information about a specific thing including channels, linked items,
     * configuration, and properties.
     *
     * @return the MCP tool definition for getting thing details
     */
    public McpSchema.Tool getThingDetailsTool() {
        return McpSchema.Tool.builder().name("get_thing_details").description(
                "Get detailed information about a specific thing including its channels, linked items, configuration, and properties.")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("thingUID",
                                Map.of("type", "string", "description",
                                        "The unique identifier of the thing (e.g., 'hue:0210:bridge:lamp1')")),
                        List.of("thingUID"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code get_thing_details} call.
     * Looks up a thing by UID and returns its full details including channels, linked items,
     * configuration, and properties.
     *
     * @param request the incoming tool call request containing the {@code thingUID} argument
     * @return a result containing the thing details, or an error if not found
     */
    public CallToolResult handleGetThingDetails(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String thingUID = getStringArg(args, "thingUID");
        if (thingUID == null) {
            return errorResult("Parameter 'thingUID' is required.");
        }

        Thing thing = thingRegistry.get(new ThingUID(thingUID));
        if (thing == null) {
            return errorResult("Thing '" + thingUID + "' not found.");
        }

        Map<String, Object> details = buildThingSummary(thing);
        details.put("configuration", thing.getConfiguration().getProperties());
        details.put("properties", thing.getProperties());

        List<Map<String, Object>> channels = new ArrayList<>();
        for (Channel channel : thing.getChannels()) {
            Map<String, Object> channelMap = new HashMap<>();
            channelMap.put("uid", channel.getUID().toString());
            channelMap.put("id", channel.getUID().getId());
            channelMap.put("label", Objects.requireNonNullElse(channel.getLabel(), channel.getUID().getId()));
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            channelMap.put("type", channelTypeUID != null ? channelTypeUID.toString() : "");
            channelMap.put("itemType", Objects.requireNonNullElse(channel.getAcceptedItemType(), ""));

            Set<Item> linkedItems = linkRegistry.getLinkedItems(channel.getUID());
            List<Map<String, String>> items = new ArrayList<>();
            for (Item item : linkedItems) {
                items.add(Map.of("name", item.getName(), "state", ItemStateFormatter.formatState(item.getState())));
            }
            channelMap.put("linkedItems", items);
            channels.add(channelMap);
        }
        details.put("channels", channels);

        return textResult(jsonMapper, details);
    }

    private Map<String, Object> buildThingSummary(Thing thing) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("uid", thing.getUID().toString());
        summary.put("label", Objects.requireNonNullElse(thing.getLabel(), ""));
        summary.put("thingType", thing.getThingTypeUID().toString());
        summary.put("binding", thing.getThingTypeUID().getBindingId());

        ThingStatusInfo statusInfo = thing.getStatusInfo();
        summary.put("status", statusInfo.getStatus().name());
        if (statusInfo.getStatusDetail() != ThingStatusDetail.NONE) {
            summary.put("statusDetail", statusInfo.getStatusDetail().name());
        }
        String desc = statusInfo.getDescription();
        if (desc != null && !desc.isEmpty()) {
            summary.put("statusDescription", desc);
        }

        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID != null) {
            summary.put("bridgeUID", bridgeUID.toString());
        }
        return summary;
    }
}
