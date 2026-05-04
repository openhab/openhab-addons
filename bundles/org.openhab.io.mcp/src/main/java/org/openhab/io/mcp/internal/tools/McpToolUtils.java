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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.io.mcp.internal.util.ItemStateFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Shared utility methods for MCP tool implementations.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class McpToolUtils {

    private static final ObjectMapper JACKSON = new ObjectMapper();

    /**
     * Returns the shared thread-safe Jackson ObjectMapper instance.
     */
    public static ObjectMapper jackson() {
        return JACKSON;
    }

    /**
     * Serializes the given data object to JSON and wraps it in a successful
     * {@link CallToolResult} with a single {@link McpSchema.TextContent} entry.
     *
     * @param jsonMapper the MCP JSON mapper to use for serialization
     * @param data the object to serialize (Map, List, POJO, etc.)
     * @return a successful tool result containing the JSON text
     */
    public static CallToolResult textResult(McpJsonMapper jsonMapper, Object data) {
        try {
            String json = jsonMapper.writeValueAsString(data);
            return CallToolResult.builder().content(List.of(new McpSchema.TextContent(json))).build();
        } catch (Exception e) {
            return errorResult("Failed to serialize response: " + e.getMessage());
        }
    }

    /**
     * Creates an error {@link CallToolResult} with the given message. The result
     * has {@code isError=true} so MCP clients can distinguish it from a successful
     * response.
     *
     * @param message human-readable error description
     * @return an error tool result
     */
    public static CallToolResult errorResult(String message) {
        return CallToolResult.builder().content(List.of(new McpSchema.TextContent(message))).isError(true).build();
    }

    /**
     * Extracts a string argument from the tool call arguments map.
     *
     * @param args the arguments map from the tool call request
     * @param key the argument name to look up
     * @return the value as a string, or {@code null} if the key is absent
     */
    public static @Nullable String getStringArg(Map<String, Object> args, String key) {
        Object val = args.get(key);
        return val != null ? val.toString() : null;
    }

    /**
     * Extracts an integer argument from the tool call arguments map.
     *
     * @param args the arguments map from the tool call request
     * @param key the argument name to look up
     * @param defaultValue value to return if the key is absent or not a number
     * @return the integer value, or {@code defaultValue} if missing
     */
    public static int getIntArg(Map<String, Object> args, String key, int defaultValue) {
        Object val = args.get(key);
        if (val instanceof Number n) {
            return n.intValue();
        }
        return defaultValue;
    }

    /**
     * Extracts a boolean argument from the tool call arguments map.
     *
     * @param args the arguments map from the tool call request
     * @param key the argument name to look up
     * @param defaultValue value to return if the key is absent or not a boolean
     * @return the boolean value, or {@code defaultValue} if missing
     */
    public static boolean getBooleanArg(Map<String, Object> args, String key, boolean defaultValue) {
        Object val = args.get(key);
        if (val instanceof Boolean b) {
            return b;
        }
        return defaultValue;
    }

    /**
     * Extracts a boolean argument that may be intentionally absent. Unlike
     * {@link #getBooleanArg}, returns {@code null} when the key is missing so
     * callers can distinguish "not provided" from "false".
     *
     * @param args the arguments map from the tool call request
     * @param key the argument name to look up
     * @return the boolean value, or {@code null} if absent
     */
    public static @Nullable Boolean getNullableBooleanArg(Map<String, Object> args, String key) {
        Object val = args.get(key);
        if (val instanceof Boolean b) {
            return b;
        }
        return null;
    }

    /**
     * Extracts a list of strings from the tool call arguments map, with safe type checking.
     *
     * @param args the arguments map from the tool call request
     * @param key the argument name to look up
     * @return the list of strings, or {@code null} if the key is absent or not a list
     */
    @SuppressWarnings("unchecked")
    public static @Nullable List<String> getStringListArg(Map<String, Object> args, String key) {
        Object val = args.get(key);
        if (val instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s) {
                    result.add(s);
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Extracts a map of string to object from the tool call arguments map, with safe type checking.
     *
     * @param args the arguments map from the tool call request
     * @param key the argument name to look up
     * @return the map, or {@code null} if the key is absent or not a map
     */
    @SuppressWarnings("unchecked")
    public static @Nullable Map<String, Object> getObjectMapArg(Map<String, Object> args, String key) {
        Object val = args.get(key);
        if (val instanceof Map<?, ?>) {
            return (Map<String, Object>) val;
        }
        return null;
    }

    /**
     * Builds a standard item detail map suitable for JSON serialization. Used by
     * both tool responses and MCP resource reads to ensure a consistent item
     * representation across the API.
     *
     * @param item the openHAB item to serialize
     * @return a map containing name, label, type, state, tags, groups,
     *         acceptedCommands, and (for groups) memberCount
     */
    public static Map<String, Object> buildItemMap(Item item) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("name", item.getName());
        details.put("label", Objects.requireNonNullElse(item.getLabel(), ""));
        details.put("type", item.getType());
        details.put("state", ItemStateFormatter.formatState(item.getState()));
        details.put("tags", item.getTags());
        details.put("groups", item.getGroupNames());
        details.put("acceptedCommands", item.getAcceptedCommandTypes().stream().map(Class::getSimpleName).toList());
        if (item instanceof GroupItem gi) {
            details.put("memberCount", gi.getMembers().size());
        }
        return details;
    }

    /**
     * Truncates a string to the given maximum length, appending an ellipsis if
     * truncated. Returns an empty string for {@code null} input.
     *
     * @param s the string to truncate (may be null)
     * @param max maximum length before truncation
     * @return the original or truncated string
     */
    public static String truncate(@Nullable String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
