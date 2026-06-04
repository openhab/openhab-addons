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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.io.mcp.internal.util.SemanticModelBuilder;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * MCP tool for retrieving the openHAB semantic model.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SemanticTools {

    private final ItemRegistry itemRegistry;
    private final @Nullable MetadataRegistry metadataRegistry;
    private final McpJsonMapper jsonMapper;
    private final boolean exposeUntaggedItems;

    /**
     * @param itemRegistry source for all items in the home
     * @param metadataRegistry source for semantic metadata (hasLocation, etc.)
     * @param jsonMapper MCP JSON mapper for serializing responses
     * @param exposeUntaggedItems whether to include non-semantic items
     */
    public SemanticTools(ItemRegistry itemRegistry, @Nullable MetadataRegistry metadataRegistry,
            McpJsonMapper jsonMapper, boolean exposeUntaggedItems) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        this.jsonMapper = jsonMapper;
        this.exposeUntaggedItems = exposeUntaggedItems;
    }

    /**
     * Returns the {@code get_semantic_model} tool schema. Retrieves the hierarchical
     * Location/Equipment/Point tree.
     *
     * @return the tool definition
     */
    public McpSchema.Tool getSemanticModelTool() {
        return McpSchema.Tool.builder().name("get_semantic_model").description(
                "Retrieve the semantic model of the smart home. Returns a hierarchical view of locations (rooms/floors/buildings), the equipment in each location (lights, sensors, appliances), and the controllable points on each equipment (switches, dimmers, measurements). Call this first to understand the home layout and find the correct item names for controlling devices.")
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null)).build();
    }

    /**
     * Handles a {@code get_semantic_model} call. Builds and returns the full home
     * model with locations, equipment, and points.
     *
     * @param request the tool call request (no arguments expected)
     * @return the semantic model as JSON
     */
    public CallToolResult handleGetSemanticModel(McpSchema.CallToolRequest request) {
        try {
            SemanticModelBuilder builder = new SemanticModelBuilder(itemRegistry, metadataRegistry);
            Map<String, Object> model = builder.buildModel(exposeUntaggedItems);
            String json = jsonMapper.writeValueAsString(model);
            return CallToolResult.builder().content(List.of(new McpSchema.TextContent(json))).build();
        } catch (IOException e) {
            return errorResult("Error building semantic model: " + e.getMessage());
        }
    }
}
