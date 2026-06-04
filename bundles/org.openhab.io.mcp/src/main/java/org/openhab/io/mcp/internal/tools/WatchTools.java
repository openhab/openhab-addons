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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.mcp.internal.util.SubscriptionManager;
import org.openhab.io.mcp.internal.util.SubscriptionManager.ChangeEvent;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tool-level wrapper around {@link SubscriptionManager} so LLMs whose clients don't
 * surface the MCP Resources API (e.g., Claude Desktop) can still "watch" items for
 * changes and retrieve the buffered event log on demand.
 *
 * Works alongside the Resources-based subscription — both mechanisms coexist.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class WatchTools {

    private final SubscriptionManager subscriptions;
    private final McpJsonMapper jsonMapper;

    /**
     * Creates a new {@code WatchTools} instance.
     *
     * @param subscriptions the subscription manager used to track item watches and buffer events
     * @param jsonMapper the JSON mapper used to serialize tool results
     */
    public WatchTools(SubscriptionManager subscriptions, McpJsonMapper jsonMapper) {
        this.subscriptions = subscriptions;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns the {@code watch_items} tool schema.
     * Defines a tool that starts watching specified items for state changes within the current session.
     *
     * @return the {@code watch_items} tool definition
     */
    public McpSchema.Tool getWatchItemsTool() {
        Map<String, Object> props = new HashMap<>();
        props.put("itemNames", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Exact item names to start watching."));
        return McpSchema.Tool.builder().name("watch_items").description("""
                Start watching the given items for state changes in this session. \
                Call get_events periodically (or when the user asks about changes) to retrieve \
                buffered change events. Use when the user says things like 'let me know if X \
                changes' or 'watch the garage door'. Watches are scoped to the current MCP session \
                and persist until unwatch_items is called or the session ends.""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("itemNames"), null, null, null)).build();
    }

    /**
     * Handles a {@code watch_items} call.
     * Registers the requested item names for watching and returns the list of added and all watched items.
     *
     * @param exchange the server exchange providing the session identity
     * @param request the tool request containing the {@code itemNames} argument
     * @return the result with added watches and the full watched-item list
     */
    public CallToolResult handleWatchItems(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        List<String> itemNames = getStringListArg(request.arguments(), "itemNames");
        if (itemNames == null || itemNames.isEmpty()) {
            return errorResult("'itemNames' is required and must be a non-empty array.");
        }
        List<String> added = subscriptions.watch(exchange.sessionId(), itemNames);
        Map<String, Object> out = new HashMap<>();
        out.put("added", added);
        out.put("allWatched", new ArrayList<>(subscriptions.watched(exchange.sessionId())));
        return textResult(jsonMapper, out);
    }

    /**
     * Returns the {@code unwatch_items} tool schema.
     * Defines a tool that stops watching specified items, or all items if none are specified.
     *
     * @return the {@code unwatch_items} tool definition
     */
    public McpSchema.Tool getUnwatchItemsTool() {
        Map<String, Object> props = new HashMap<>();
        props.put("itemNames", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Item names to stop watching. Omit to unwatch all."));
        return McpSchema.Tool.builder().name("unwatch_items")
                .description("Stop watching the given items for state changes. Omit itemNames to unwatch all.")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of(), null, null, null)).build();
    }

    /**
     * Handles an {@code unwatch_items} call.
     * Removes the specified items from the watch list, or all items if none are provided.
     *
     * @param exchange the server exchange providing the session identity
     * @param request the tool request containing the optional {@code itemNames} argument
     * @return the result with removed watches and the remaining watched-item list
     */
    public CallToolResult handleUnwatchItems(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        List<String> itemNames = getStringListArg(request.arguments(), "itemNames");
        if (itemNames == null || itemNames.isEmpty()) {
            itemNames = new ArrayList<>(subscriptions.watched(exchange.sessionId()));
        }
        List<String> removed = subscriptions.unwatch(exchange.sessionId(), itemNames);
        Map<String, Object> out = new HashMap<>();
        out.put("removed", removed);
        out.put("remaining", new ArrayList<>(subscriptions.watched(exchange.sessionId())));
        return textResult(jsonMapper, out);
    }

    /**
     * Returns the {@code get_events} tool schema.
     * Defines a tool that retrieves and drains buffered state-change events for watched items.
     *
     * @return the {@code get_events} tool definition
     */
    public McpSchema.Tool getEventsTool() {
        return McpSchema.Tool.builder().name("get_events").description("""
                Retrieve and drain the buffered state-change events for items this session is \
                watching. Returns all events captured since the previous call; subsequent calls \
                return only new events. Use when the user asks 'did anything happen' or 'what \
                changed', or call proactively after long user messages to pick up any events \
                that fired meanwhile.""")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_events} call.
     * Drains all buffered change events since the last call and returns them with the current watch list.
     *
     * @param exchange the server exchange providing the session identity
     * @param request the tool request (no arguments required)
     * @return the result containing the list of events, count, and currently watched items
     */
    public CallToolResult handleGetEvents(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        List<ChangeEvent> events = subscriptions.drainEvents(exchange.sessionId());
        List<Map<String, Object>> list = new ArrayList<>();
        for (ChangeEvent e : events) {
            Map<String, Object> ev = new HashMap<>();
            ev.put("itemName", e.itemName());
            ev.put("state", e.state());
            ev.put("oldState", e.oldState());
            ev.put("timestamp", e.timestamp().toString());
            list.add(ev);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("events", list);
        out.put("count", list.size());
        out.put("watched", new ArrayList<>(subscriptions.watched(exchange.sessionId())));
        return textResult(jsonMapper, out);
    }
}
