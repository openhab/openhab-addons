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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.AbstractEvent;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemBuilder;
import org.openhab.core.items.ItemBuilderFactory;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.io.mcp.internal.util.FuzzyItemMatcher;
import org.openhab.io.mcp.internal.util.ItemStateFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tools for item retrieval, search, control, and CRUD.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ItemTools {

    private final Logger logger = LoggerFactory.getLogger(ItemTools.class);

    private static final double MIN_SCORE = 0.65;
    static final String MCP_EVENT_SOURCE = "org.openhab.io.mcp";

    private final ItemRegistry itemRegistry;
    private final ItemBuilderFactory itemBuilderFactory;
    private final EventPublisher eventPublisher;
    private final McpJsonMapper jsonMapper;
    private final FuzzyItemMatcher fuzzyMatcher;
    private final Function<String, @Nullable String> usernameForSession;

    /**
     * Constructs a new {@code ItemTools} instance with the required openHAB services.
     *
     * @param itemRegistry the registry used to look up and manage items
     * @param itemBuilderFactory factory for creating new item builders
     * @param metadataRegistry the metadata registry used for fuzzy matching, may be {@code null}
     * @param eventPublisher the event publisher for sending commands and state updates
     * @param usernameForSession resolves the authenticated username for an MCP session id, used as the
     *            actor in the source string of published events
     * @param jsonMapper the JSON mapper for serializing tool results
     */
    public ItemTools(ItemRegistry itemRegistry, ItemBuilderFactory itemBuilderFactory,
            @Nullable MetadataRegistry metadataRegistry, EventPublisher eventPublisher,
            Function<String, @Nullable String> usernameForSession, McpJsonMapper jsonMapper) {
        this.itemRegistry = itemRegistry;
        this.itemBuilderFactory = itemBuilderFactory;
        this.eventPublisher = eventPublisher;
        this.usernameForSession = usernameForSession;
        this.jsonMapper = jsonMapper;
        this.fuzzyMatcher = new FuzzyItemMatcher(metadataRegistry);
    }

    /**
     * Returns the {@code get_item} tool schema.
     * Retrieves the current state and details of one or more items by their exact names.
     *
     * @return the MCP tool definition for {@code get_item}
     */
    public McpSchema.Tool getItemTool() {
        return McpSchema.Tool.builder().name("get_item")
                .description("Get the current state and details of one or more items by their exact item names.")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("itemNames",
                                Map.of("type", "array", "items", Map.of("type", "string"), "description",
                                        "List of exact item names to retrieve")),
                        List.of("itemNames"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code get_item} call.
     * Looks up the requested items by name and returns their details, or an error with suggestions
     * if none are found.
     *
     * @param request the incoming tool call request containing the item names
     * @return the result containing item details or an error message
     */
    public CallToolResult handleGetItem(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        List<String> itemNames = getStringListArg(args, "itemNames");
        if (itemNames == null || itemNames.isEmpty()) {
            return errorResult("Parameter 'itemNames' is required and must be a non-empty array.");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (String name : itemNames) {
            try {
                Item item = itemRegistry.getItem(name);
                results.add(buildItemMap(item));
            } catch (ItemNotFoundException e) {
                notFound.add(name);
            }
        }

        if (!notFound.isEmpty() && results.isEmpty()) {
            StringBuilder msg = new StringBuilder("Items not found: " + String.join(", ", notFound));
            appendSuggestions(msg, notFound);
            return errorResult(msg.toString());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", results);
        if (!notFound.isEmpty()) {
            response.put("notFound", notFound);
        }
        return textResult(jsonMapper, response);
    }

    /**
     * Returns the {@code search_items} tool schema.
     * Searches for items by name, label, synonyms, type, tags, or group membership using fuzzy matching.
     *
     * @return the MCP tool definition for {@code search_items}
     */
    public McpSchema.Tool getSearchItemsTool() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("query", Map.of("type", "string", "description",
                "Free-text query matched against item names, labels, and synonyms using fuzzy token matching. Tolerates typos and word reordering (e.g. 'kichen lite' finds kitchen lights)."));
        properties.put("type", Map.of("type", "string", "description",
                "Filter by item type: Switch, Dimmer, Color, Number, String, Contact, Rollershutter, Player, DateTime, Image, Location, Group"));
        properties.put("tags", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Filter items that have ALL of these tags"));
        properties.put("group", Map.of("type", "string", "description", "Filter items that are members of this group"));
        properties.put("recursive", Map.of("type", "boolean", "description",
                "When filtering by group, include items in nested subgroups (default: true)"));
        properties.put("limit", Map.of("type", "integer", "description", "Maximum results to return (default: 50)"));
        properties.put("offset", Map.of("type", "integer", "description", "Number of results to skip (default: 0)"));

        return McpSchema.Tool.builder().name("search_items").description(
                "Search for items by name, label, synonyms, type, tags, or group membership. Returns matching items ranked by relevance with their current state. Use this when you need to find specific items or don't know the exact item name.")
                .inputSchema(new McpSchema.JsonSchema("object", properties, List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code search_items} call.
     * Filters and ranks items using fuzzy matching and optional type/tag/group filters, returning
     * a paginated list of results.
     *
     * @param request the incoming tool call request containing search parameters
     * @return the result containing matched items with pagination metadata
     */
    public CallToolResult handleSearchItems(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String query = getStringArg(args, "query");
        String type = getStringArg(args, "type");
        List<String> tags = getStringListArg(args, "tags");
        String group = getStringArg(args, "group");
        boolean recursive = getBooleanArg(args, "recursive", true);
        int limit = getIntArg(args, "limit", 50);
        int offset = getIntArg(args, "offset", 0);

        Iterable<Item> source;
        if (group != null) {
            try {
                Item groupItem = itemRegistry.getItem(group);
                if (groupItem instanceof GroupItem gi) {
                    source = recursive ? gi.getAllMembers() : gi.getMembers();
                } else {
                    return errorResult("'" + group + "' is not a group item.");
                }
            } catch (ItemNotFoundException e) {
                return errorResult("Group '" + group + "' not found.");
            }
        } else {
            source = itemRegistry.getItems();
        }

        String effectiveQuery = query != null && !query.isBlank() ? query : null;
        // Compute fuzzy score once per item; items that pass non-query filters but
        // score below MIN_SCORE are dropped. When there is no query, everyone scores 1.0.
        List<ScoredItem> scored = new ArrayList<>();
        for (Item item : source) {
            if (!matchesNonQueryFilters(item, type, tags)) {
                continue;
            }
            double s = 1.0;
            if (effectiveQuery != null) {
                s = fuzzyMatcher.score(item, effectiveQuery);
                if (s < MIN_SCORE) {
                    continue;
                }
            }
            scored.add(new ScoredItem(item, s));
        }
        // Higher score first; tie-break by shorter name so canonical items out-rank long
        // derived ones like F1_DansOffice_Light_Up_Dimming_Edge.
        scored.sort((a, b) -> {
            int c = Double.compare(b.score, a.score);
            return c != 0 ? c : Integer.compare(a.item.getName().length(), b.item.getName().length());
        });

        List<Item> matchingItems = new ArrayList<>(scored.size());
        for (ScoredItem si : scored) {
            matchingItems.add(si.item);
        }

        int total = matchingItems.size();
        List<Item> page = matchingItems.subList(Math.min(offset, total), Math.min(offset + limit, total));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Item item : page) {
            items.add(buildItemSummary(item));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("total", total);
        response.put("offset", offset);
        response.put("limit", limit);
        return textResult(jsonMapper, response);
    }

    /**
     * Returns the {@code set_item} tool schema. Single entry point for changing an item's value;
     * dispatches on {@code action} to either send a command through the binding (controls the
     * device) or post a state-update event directly (no binding involvement).
     *
     * @return the MCP tool definition for {@code set_item}
     */
    public McpSchema.Tool getSetItemTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of("type", "string", "description",
                "command: send a command through the binding to control the device; "
                        + "state: post a state-update event for the item directly — the same kind of event a "
                        + "binding publishes after observing a device. The binding is not asked to do anything; "
                        + "you're declaring what the item's state is, not requesting that openHAB change it. "
                        + "Most device-control intents should use 'command'.",
                "enum", List.of("command", "state")));
        props.put("itemName", Map.of("type", "string", "description", "The exact name of the item."));
        props.put("value", Map.of("type", "string", "description",
                """
                        The value to send (a command if action='command', a state if action='state'). \
                        Accepted values by item type:
                          - Switch: ON, OFF
                          - Dimmer: ON, OFF, INCREASE, DECREASE, or 0-100 (percent)
                          - Color: ON, OFF, INCREASE, DECREASE, 0-100 (brightness), or "H,S,B" triple \
                        (e.g. "120,100,50" — H is 0-360, S/B are 0-100)
                          - Rollershutter: UP, DOWN, STOP, MOVE, or 0-100 (percent position)
                          - Number: any decimal (e.g. "42", "21.5", "-3.14")
                          - Number:<dimension> (e.g. Number:Temperature, Number:Power, Number:Length): value with unit \
                        (e.g. "21.5 °C", "70 °F", "1024 W", "1.8 m"); a plain number is interpreted in the item's configured unit
                          - String: any text value
                          - DateTime: ISO-8601 datetime (e.g. "2026-01-15T10:30:00" or with offset "2026-01-15T10:30:00-05:00")
                          - Contact: OPEN, CLOSED — Contact items don't accept commands, so use action='state' for these
                          - Player: PLAY, PAUSE, NEXT, PREVIOUS, REWIND, FASTFORWARD
                          - Location: "lat,lon" or "lat,lon,alt" (e.g. "37.7749,-122.4194" or "37.7749,-122.4194,30")
                          - Group: forwards to members based on the group's base type
                        All item types also accept REFRESH as a command (asks the binding to re-poll the device), \
                        and NULL or UNDEF as a state (clears the value). Use get_item first if you're not sure of an item's type."""));

        return McpSchema.Tool.builder().name("set_item").description("""
                Change an item's value. Action-dispatched: \
                action='command' sends a command through the binding to control the physical device — \
                use this for normal device control (turn on/off, dim, open/close, set values). \
                action='state' posts a state-update event for the item directly, the same kind of event a binding \
                publishes after observing a device. The binding is not asked to do anything — you're declaring what \
                the item's state is, not requesting that openHAB change it. Most device-control intents should use \
                'command'. See the 'value' field for the full list of accepted values per item type.""").inputSchema(
                new McpSchema.JsonSchema("object", props, List.of("action", "itemName", "value"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code set_item} call. Dispatches to command-send / state-update based on
     * {@code action}.
     *
     * @param exchange the MCP server exchange — used to resolve the authenticated user for event attribution
     * @param request the incoming tool call request
     * @return the result for the dispatched action
     */
    public CallToolResult handleSetItem(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String action = getStringArg(args, "action");
        String itemName = getStringArg(args, "itemName");
        String value = getStringArg(args, "value");
        if (action == null || action.isBlank()) {
            return errorResult("'action' is required (one of: command, state).");
        }
        if (itemName == null || itemName.isBlank() || value == null || value.isBlank()) {
            return errorResult("'itemName' and 'value' are required.");
        }
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "command" -> sendCommand(exchange, itemName, value);
            case "state" -> updateState(exchange, itemName, value);
            default -> errorResult("Invalid action '" + action + "'. Use one of: command, state.");
        };
    }

    private CallToolResult sendCommand(McpSyncServerExchange exchange, String itemName, String commandStr) {
        Item item;
        try {
            item = itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            StringBuilder msg = new StringBuilder("Item '" + itemName + "' not found.");
            appendSuggestions(msg, List.of(itemName));
            return errorResult(msg.toString());
        }

        // Tolerate "30%" / "50 %" — LLMs often mirror back the formatted state verbatim.
        String parseableCommand = commandStr.endsWith("%") ? commandStr.substring(0, commandStr.length() - 1).trim()
                : commandStr;
        Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), parseableCommand);
        if (command == null) {
            List<String> accepted = item.getAcceptedCommandTypes().stream().map(Class::getSimpleName).toList();
            return errorResult("Cannot parse command '" + commandStr + "' for item '" + itemName
                    + "'. Accepted command types: " + String.join(", ", accepted));
        }

        String previousState = ItemStateFormatter.formatState(item.getState());
        String source = AbstractEvent.buildSource(MCP_EVENT_SOURCE, usernameForSession.apply(exchange.sessionId()));
        eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command, source));
        logger.debug("Sent command '{}' to item '{}' (source={})", command, itemName, source);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("action", "command");
        result.put("itemName", itemName);
        result.put("command", command.toString());
        result.put("previousState", previousState);
        return textResult(jsonMapper, result);
    }

    private CallToolResult updateState(McpSyncServerExchange exchange, String itemName, String stateStr) {
        Item item;
        try {
            item = itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return errorResult("Item '" + itemName + "' not found.");
        }

        State state = TypeParser.parseState(item.getAcceptedDataTypes(), stateStr);
        if (state == null) {
            return errorResult("Cannot parse state '" + stateStr + "' for item '" + itemName + "'.");
        }

        String previousState = ItemStateFormatter.formatState(item.getState());
        String source = AbstractEvent.buildSource(MCP_EVENT_SOURCE, usernameForSession.apply(exchange.sessionId()));
        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, state, source));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("action", "state");
        result.put("itemName", itemName);
        result.put("state", stateStr);
        result.put("previousState", previousState);
        return textResult(jsonMapper, result);
    }

    /**
     * Returns the {@code manage_item} tool schema. Single CRUD entry point for item
     * lifecycle management; dispatches on {@code action}.
     *
     * @return the MCP tool definition for {@code manage_item}
     */
    public McpSchema.Tool getManageItemTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of("type", "string", "description",
                "create: add a new item; update: modify labels/tags/groups of an existing item (cannot change type); delete: remove an item and its links.",
                "enum", List.of("create", "update", "delete")));
        props.put("name", Map.of("type", "string", "description",
                "Item name. For create: follow Location_Equipment_Point (e.g. Kitchen_Light_Brightness)."));
        props.put("type", Map.of("type", "string", "description",
                "create only: item type — Switch, Dimmer, Color, Contact, DateTime, Number, Number:<dimension>, "
                        + "Player, Rollershutter, String, Image, Location, Group. Item type is immutable; "
                        + "to change it, delete and recreate."));
        props.put("label",
                Map.of("type", "string", "description", "create/update: human-readable label (omit to keep current)."));
        props.put("category", Map.of("type", "string", "description",
                "create/update: icon category like light, switch, door (omit to keep current)."));
        props.put("tags", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "create/update: semantic or custom tags. Replaces existing tags on update."));
        props.put("groupNames", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "create/update: groups this item belongs to. Replaces existing memberships on update."));
        props.put("groupType", Map.of("type", "string", "description",
                "create only, for Group items: the base item type of members (e.g. Switch, Number)."));
        props.put("groupFunction", Map.of("type", "string", "description",
                "create only, for Group items: aggregation function (AND, OR, NAND, NOR, AVG, SUM, MIN, MAX, COUNT, LATEST, EARLIEST, EQUALITY)."));

        return McpSchema.Tool.builder().name("manage_item").description("""
                Create, update, or delete an item. Action-dispatched: \
                action='create' requires name and type; \
                action='update' requires name (and any fields to change — label, category, tags, groupNames); \
                action='delete' requires only name (links are removed automatically).""")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("action", "name"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code manage_item} call. Dispatches to create / update / delete based on the
     * {@code action} argument.
     *
     * @param request the incoming tool call request
     * @return the result for the dispatched action
     */
    public CallToolResult handleManageItem(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String action = getStringArg(args, "action");
        String name = getStringArg(args, "name");
        if (action == null || action.isBlank()) {
            return errorResult("'action' is required (one of: create, update, delete).");
        }
        if (name == null || name.isBlank()) {
            return errorResult("'name' is required.");
        }
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "create" -> createItem(name, args);
            case "update" -> updateItem(name, args);
            case "delete" -> deleteItem(name);
            default -> errorResult("Invalid action '" + action + "'. Use one of: create, update, delete.");
        };
    }

    private CallToolResult createItem(String name, Map<String, Object> args) {
        String type = getStringArg(args, "type");
        if (type == null || type.isBlank()) {
            return errorResult("'type' is required when action='create'.");
        }

        try {
            itemRegistry.getItem(name);
            return errorResult("Item '" + name + "' already exists. Use action='update' to modify it.");
        } catch (ItemNotFoundException e) {
            // expected
        }

        try {
            ItemBuilder builder = itemBuilderFactory.newItemBuilder(type, name);
            String label = getStringArg(args, "label");
            if (label != null) {
                builder.withLabel(label);
            }
            String category = getStringArg(args, "category");
            if (category != null) {
                builder.withCategory(category);
            }
            List<String> tags = getStringListArg(args, "tags");
            if (tags != null) {
                builder.withTags(new HashSet<>(tags));
            }
            List<String> groupNames = getStringListArg(args, "groupNames");
            if (groupNames != null) {
                builder.withGroups(groupNames);
            }
            Item item = builder.build();
            itemRegistry.add(item);
            logger.debug("Created item '{}' of type '{}'", name, type);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("action", "create");
            result.put("name", name);
            result.put("type", type);
            result.put("label", label != null ? label : "");
            return textResult(jsonMapper, result);
        } catch (Exception e) {
            return errorResult("Failed to create item '" + name + "': " + e.getMessage());
        }
    }

    private CallToolResult updateItem(String name, Map<String, Object> args) {
        Item existing;
        try {
            existing = itemRegistry.getItem(name);
        } catch (ItemNotFoundException e) {
            return errorResult("Item '" + name + "' not found.");
        }

        try {
            ItemBuilder builder = itemBuilderFactory.newItemBuilder(existing);
            String label = getStringArg(args, "label");
            if (label != null) {
                builder.withLabel(label);
            }
            String category = getStringArg(args, "category");
            if (category != null) {
                builder.withCategory(category);
            }
            List<String> tags = getStringListArg(args, "tags");
            if (tags != null) {
                builder.withTags(new HashSet<>(tags));
            }
            List<String> groupNames = getStringListArg(args, "groupNames");
            if (groupNames != null) {
                builder.withGroups(groupNames);
            }
            Item updated = builder.build();
            itemRegistry.update(updated);
            logger.debug("Updated item '{}'", name);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("action", "update");
            result.put("name", name);
            result.put("label", Objects.requireNonNullElse(updated.getLabel(), ""));
            result.put("tags", updated.getTags());
            result.put("groupNames", updated.getGroupNames());
            return textResult(jsonMapper, result);
        } catch (Exception e) {
            return errorResult("Failed to update item '" + name + "': " + e.getMessage());
        }
    }

    private CallToolResult deleteItem(String name) {
        Item removed = itemRegistry.remove(name);
        if (removed == null) {
            return errorResult("Item '" + name + "' not found.");
        }
        logger.debug("Deleted item '{}'", name);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("action", "delete");
        result.put("name", name);
        return textResult(jsonMapper, result);
    }

    private Map<String, Object> buildItemSummary(Item item) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("name", item.getName());
        summary.put("label", Objects.requireNonNullElse(item.getLabel(), ""));
        summary.put("type", item.getType());
        summary.put("state", ItemStateFormatter.formatState(item.getState()));
        if (!item.getTags().isEmpty()) {
            summary.put("tags", item.getTags());
        }
        return summary;
    }

    private record ScoredItem(Item item, double score) {
    }

    private boolean matchesNonQueryFilters(Item item, @Nullable String type, @Nullable List<String> tags) {
        if (type != null && !type.isEmpty()) {
            if (!item.getType().equalsIgnoreCase(type)) {
                return false;
            }
        }
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                if (!item.hasTag(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void appendSuggestions(StringBuilder msg, List<String> missingNames) {
        List<String> suggestions = new ArrayList<>();
        for (String missing : missingNames) {
            String lowerMissing = missing.toLowerCase(Locale.ROOT);
            for (Item item : itemRegistry.getItems()) {
                if (item.getName().toLowerCase(Locale.ROOT).contains(lowerMissing)
                        || lowerMissing.contains(item.getName().toLowerCase(Locale.ROOT))) {
                    suggestions.add(item.getName());
                    if (suggestions.size() >= 5) {
                        break;
                    }
                }
            }
        }
        if (!suggestions.isEmpty()) {
            msg.append(" Similar items: ").append(String.join(", ", suggestions));
        }
    }
}
