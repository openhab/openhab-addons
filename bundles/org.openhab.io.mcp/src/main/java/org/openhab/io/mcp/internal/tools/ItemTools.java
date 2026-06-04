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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private final ItemRegistry itemRegistry;
    private final ItemBuilderFactory itemBuilderFactory;
    private final EventPublisher eventPublisher;
    private final McpJsonMapper jsonMapper;
    private final FuzzyItemMatcher fuzzyMatcher;

    /**
     * Constructs a new {@code ItemTools} instance with the required openHAB services.
     *
     * @param itemRegistry the registry used to look up and manage items
     * @param itemBuilderFactory factory for creating new item builders
     * @param metadataRegistry the metadata registry used for fuzzy matching, may be {@code null}
     * @param eventPublisher the event publisher for sending commands and state updates
     * @param jsonMapper the JSON mapper for serializing tool results
     */
    public ItemTools(ItemRegistry itemRegistry, ItemBuilderFactory itemBuilderFactory,
            @Nullable MetadataRegistry metadataRegistry, EventPublisher eventPublisher, McpJsonMapper jsonMapper) {
        this.itemRegistry = itemRegistry;
        this.itemBuilderFactory = itemBuilderFactory;
        this.eventPublisher = eventPublisher;
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
     * Returns the {@code send_command} tool schema.
     * Sends a command to control a device through its item (e.g., ON/OFF, dimmer levels, HSB values).
     *
     * @return the MCP tool definition for {@code send_command}
     */
    public McpSchema.Tool getSendCommandTool() {
        return McpSchema.Tool.builder().name("send_command").description(
                "Send a command to control a device. Common commands: ON/OFF for switches, 0-100 for dimmers, UP/DOWN/STOP for rollershutters, HSB values for colors, numeric values for thermostats. Use get_item first to check the item type if unsure.")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of("itemName",
                        Map.of("type", "string", "description", "The exact name of the item to command"), "command",
                        Map.of("type", "string", "description", "The command string (e.g., ON, OFF, 50, UP, 21.5)")),
                        List.of("itemName", "command"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code send_command} call.
     * Parses the command string, validates it against the item's accepted types, and publishes
     * the command event.
     *
     * @param request the incoming tool call request containing the item name and command
     * @return the result indicating success with the previous state, or an error message
     */
    public CallToolResult handleSendCommand(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String itemName = getStringArg(args, "itemName");
        String commandStr = getStringArg(args, "command");

        if (itemName == null || commandStr == null) {
            return errorResult("Both 'itemName' and 'command' are required.");
        }

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
        eventPublisher.post(ItemEventFactory.createCommandEvent(itemName, command, "org.openhab.io.mcp"));
        logger.debug("Sent command '{}' to item '{}'", command, itemName);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("itemName", itemName);
        result.put("command", command.toString());
        result.put("previousState", previousState);
        return textResult(jsonMapper, result);
    }

    /**
     * Returns the {@code update_state} tool schema.
     * Updates the state of an item directly without sending a command to the physical device.
     *
     * @return the MCP tool definition for {@code update_state}
     */
    public McpSchema.Tool getUpdateStateTool() {
        return McpSchema.Tool.builder().name("update_state").description(
                "Update the state of an item directly without sending a command to the device. Use this for virtual items or sensor values, not for controlling physical devices (use send_command for that).")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("itemName",
                                Map.of("type", "string", "description", "The exact name of the item to update"),
                                "state", Map.of("type", "string", "description", "The new state value")),
                        List.of("itemName", "state"), null, null, null))
                .build();
    }

    /**
     * Handles an {@code update_state} call.
     * Parses the state string, validates it against the item's accepted data types, and publishes
     * the state update event.
     *
     * @param request the incoming tool call request containing the item name and new state
     * @return the result indicating success, or an error message
     */
    public CallToolResult handleUpdateState(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String itemName = getStringArg(args, "itemName");
        String stateStr = getStringArg(args, "state");

        if (itemName == null || stateStr == null) {
            return errorResult("Both 'itemName' and 'state' are required.");
        }

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

        eventPublisher.post(ItemEventFactory.createStateEvent(itemName, state, "org.openhab.io.mcp"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("itemName", itemName);
        result.put("state", stateStr);
        return textResult(jsonMapper, result);
    }

    /**
     * Returns the {@code create_item} tool schema.
     * Creates a new openHAB item with the specified name, type, label, tags, and group memberships.
     *
     * @return the MCP tool definition for {@code create_item}
     */
    public McpSchema.Tool getCreateItemTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", Map.of("type", "string", "description", "Unique item name (e.g. Kitchen_Light)"));
        props.put("type",
                Map.of("type", "string", "description",
                        "Item type: Switch, Dimmer, Color, Contact, DateTime, Number, Number:<dimension>, "
                                + "Player, Rollershutter, String, Image, Location, Group"));
        props.put("label", Map.of("type", "string", "description", "Human-readable label"));
        props.put("category", Map.of("type", "string", "description", "Icon category (e.g. light, switch, door)"));
        props.put("tags",
                Map.of("type", "array", "items", Map.of("type", "string"), "description", "Semantic or custom tags"));
        props.put("groupNames", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Groups this item belongs to"));
        props.put("groupType", Map.of("type", "string", "description",
                "For Group items only: the base item type of members (e.g. Switch, Number)"));
        props.put("groupFunction", Map.of("type", "string", "description",
                "For Group items only: aggregation function (AND, OR, NAND, NOR, AVG, SUM, MIN, MAX, COUNT, LATEST, EARLIEST, EQUALITY)"));

        return McpSchema.Tool.builder().name("create_item")
                .description("Create a new openHAB item. Use this when setting up new devices or "
                        + "organizing the item model. Item names should follow the convention "
                        + "Location_Equipment_Point (e.g. Kitchen_Light_Brightness).")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("name", "type"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code create_item} call.
     * Validates that the item does not already exist, builds it from the provided arguments,
     * and adds it to the item registry.
     *
     * @param request the incoming tool call request containing item properties
     * @return the result indicating success with the created item details, or an error message
     */
    public CallToolResult handleCreateItem(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String name = getStringArg(args, "name");
        String type = getStringArg(args, "type");
        if (name == null || name.isBlank() || type == null || type.isBlank()) {
            return errorResult("'name' and 'type' are required.");
        }

        try {
            itemRegistry.getItem(name);
            return errorResult("Item '" + name + "' already exists. Use update_item to modify it.");
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
            result.put("name", name);
            result.put("type", type);
            result.put("label", label != null ? label : "");
            return textResult(jsonMapper, result);
        } catch (Exception e) {
            return errorResult("Failed to create item '" + name + "': " + e.getMessage());
        }
    }

    /**
     * Returns the {@code update_item} tool schema.
     * Updates an existing item's label, category, tags, or group memberships without changing its type.
     *
     * @return the MCP tool definition for {@code update_item}
     */
    public McpSchema.Tool getUpdateItemTool() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", Map.of("type", "string", "description", "Name of the item to update"));
        props.put("label", Map.of("type", "string", "description", "New label (omit to keep current)"));
        props.put("category", Map.of("type", "string", "description", "New icon category (omit to keep current)"));
        props.put("tags", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Replace all tags (omit to keep current)"));
        props.put("groupNames", Map.of("type", "array", "items", Map.of("type", "string"), "description",
                "Replace all group memberships (omit to keep current)"));

        return McpSchema.Tool.builder().name("update_item")
                .description("Update an existing item's label, category, tags, or group memberships. "
                        + "Cannot change the item type — delete and recreate the item for that.")
                .inputSchema(new McpSchema.JsonSchema("object", props, List.of("name"), null, null, null)).build();
    }

    /**
     * Handles an {@code update_item} call.
     * Looks up the existing item and applies the requested property changes via the item builder.
     *
     * @param request the incoming tool call request containing the item name and updated properties
     * @return the result indicating success with the updated item details, or an error message
     */
    public CallToolResult handleUpdateItem(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String name = getStringArg(args, "name");
        if (name == null || name.isBlank()) {
            return errorResult("'name' is required.");
        }

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
            result.put("name", name);
            result.put("label", Objects.requireNonNullElse(updated.getLabel(), ""));
            result.put("tags", updated.getTags());
            result.put("groupNames", updated.getGroupNames());
            return textResult(jsonMapper, result);
        } catch (Exception e) {
            return errorResult("Failed to update item '" + name + "': " + e.getMessage());
        }
    }

    /**
     * Returns the {@code delete_item} tool schema.
     * Permanently removes an item and its associated links from openHAB.
     *
     * @return the MCP tool definition for {@code delete_item}
     */
    public McpSchema.Tool getDeleteItemTool() {
        return McpSchema.Tool.builder().name("delete_item")
                .description("Permanently remove an item from openHAB. Associated links will also be removed.")
                .inputSchema(new McpSchema.JsonSchema("object",
                        Map.of("name", Map.of("type", "string", "description", "Name of the item to delete")),
                        List.of("name"), null, null, null))
                .build();
    }

    /**
     * Handles a {@code delete_item} call.
     * Removes the named item from the item registry if it exists.
     *
     * @param request the incoming tool call request containing the item name to delete
     * @return the result indicating success, or an error if the item was not found
     */
    public CallToolResult handleDeleteItem(McpSchema.CallToolRequest request) {
        Map<String, Object> args = request.arguments();
        String name = getStringArg(args, "name");
        if (name == null || name.isBlank()) {
            return errorResult("'name' is required.");
        }

        Item removed = itemRegistry.remove(name);
        if (removed == null) {
            return errorResult("Item '" + name + "' not found.");
        }
        logger.debug("Deleted item '{}'", name);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
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
