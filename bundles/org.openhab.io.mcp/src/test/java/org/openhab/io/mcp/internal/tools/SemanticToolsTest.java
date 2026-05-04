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
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link SemanticTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class SemanticToolsTest {

    @Mock
    @Nullable
    ItemRegistry itemRegistry;

    @Mock
    @Nullable
    MetadataRegistry metadataRegistry;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    @Test
    @SuppressWarnings("unchecked")
    void getSemanticModelReturnsLocations() throws Exception {
        GroupItem room = mock(GroupItem.class);
        lenient().when(room.getName()).thenReturn("Kitchen");
        lenient().when(room.getLabel()).thenReturn("Kitchen");
        lenient().when(room.getType()).thenReturn("Group");
        lenient().when(room.getState()).thenReturn(UnDefType.NULL);
        lenient().when(room.getTags()).thenReturn(Set.of("Location"));
        lenient().when(room.getGroupNames()).thenReturn(List.of());
        lenient().when(room.getMembers()).thenReturn(Set.of());

        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(room));

        SemanticTools tools = new SemanticTools(Objects.requireNonNull(itemRegistry), metadataRegistry, jsonMapper,
                false);
        CallToolResult result = tools.handleGetSemanticModel(McpTestHelper.createRequest(Map.of()));
        McpTestHelper.assertSuccess(result);

        String text = McpTestHelper.extractText(result);
        assertNotNull(text);
        assertTrue(text.contains("locations"));
        assertTrue(text.contains("Kitchen"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSemanticModelEmptyRegistryReturnsEmptyLocations() throws Exception {
        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of());

        SemanticTools tools = new SemanticTools(Objects.requireNonNull(itemRegistry), metadataRegistry, jsonMapper,
                false);
        CallToolResult result = tools.handleGetSemanticModel(McpTestHelper.createRequest(Map.of()));
        McpTestHelper.assertSuccess(result);

        String text = McpTestHelper.extractText(result);
        assertTrue(text.contains("locations"));
        // The JSON should contain an empty locations array
        assertTrue(text.contains("\"locations\":[]") || text.contains("\"locations\" : []")
                || text.contains("\"locations\": []"));
    }

    @Test
    void exposeUntaggedItemsFlagPassedThrough() throws Exception {
        Item orphan = mock(org.openhab.core.items.Item.class);
        lenient().when(orphan.getName()).thenReturn("Orphan");
        lenient().when(orphan.getLabel()).thenReturn("Orphan Item");
        lenient().when(orphan.getType()).thenReturn("Switch");
        lenient().when(orphan.getState()).thenReturn(OnOffType.ON);
        lenient().when(orphan.getTags()).thenReturn(Set.of());
        lenient().when(orphan.getGroupNames()).thenReturn(List.of());

        when(Objects.requireNonNull(itemRegistry).getItems()).thenReturn(List.of(orphan));

        // With exposeUntaggedItems=true, the model should contain unassignedItems
        SemanticTools toolsExposed = new SemanticTools(Objects.requireNonNull(itemRegistry), metadataRegistry,
                jsonMapper, true);
        CallToolResult resultExposed = toolsExposed.handleGetSemanticModel(McpTestHelper.createRequest(Map.of()));
        McpTestHelper.assertSuccess(resultExposed);
        String textExposed = McpTestHelper.extractText(resultExposed);
        assertTrue(textExposed.contains("unassignedItems"), "Should contain unassignedItems when flag is true");
        assertTrue(textExposed.contains("Orphan"));

        // With exposeUntaggedItems=false, unassignedItems should not appear
        SemanticTools toolsHidden = new SemanticTools(Objects.requireNonNull(itemRegistry), metadataRegistry,
                jsonMapper, false);
        CallToolResult resultHidden = toolsHidden.handleGetSemanticModel(McpTestHelper.createRequest(Map.of()));
        McpTestHelper.assertSuccess(resultHidden);
        String textHidden = McpTestHelper.extractText(resultHidden);
        assertFalse(textHidden.contains("unassignedItems"), "Should not contain unassignedItems when flag is false");
    }
}
