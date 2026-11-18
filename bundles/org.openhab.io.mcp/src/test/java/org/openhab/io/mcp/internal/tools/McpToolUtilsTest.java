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
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;
import org.openhab.io.mcp.internal.McpTestHelper;
import org.openhab.io.mcp.internal.auth.McpUser;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link McpToolUtils}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class McpToolUtilsTest {

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();

    @Test
    void testAdminOnlyRunsHandlerForAdmin() {
        CallToolResult ok = McpToolUtils.errorResult("handler ran");

        CallToolResult result = McpToolUtils.adminOnly((ex, req) -> ok).apply(exchangeFor(new McpUser("alice", true)),
                new CallToolRequest("manage_item", Map.of()));

        assertSame(ok, result);
    }

    @Test
    void testAdminOnlyRefusesNonAdminWithoutRunningHandler() {
        AtomicBoolean ran = new AtomicBoolean();

        CallToolResult result = McpToolUtils.adminOnly((ex, req) -> {
            ran.set(true);
            return McpToolUtils.errorResult("handler ran");
        }).apply(exchangeFor(new McpUser("bob", false)), new CallToolRequest("manage_item", Map.of()));

        assertFalse(ran.get());
        assertEquals(Boolean.TRUE, result.isError());
        String text = extractText(result);
        assertTrue(text.contains("'manage_item'"), text);
        assertTrue(text.contains("administrator"), text);
    }

    @Test
    void testAdminOnlyRefusesRequestWithoutUser() {
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.transportContext()).thenReturn(McpTransportContext.EMPTY);

        CallToolResult result = McpToolUtils.adminOnly((ex, req) -> McpToolUtils.errorResult("handler ran"))
                .apply(exchange, new CallToolRequest("manage_item", Map.of()));

        assertTrue(extractText(result).contains("administrator"));
    }

    @Test
    void testAdminByDefaultOnlyOpensListedTools() {
        CallToolResult ok = McpToolUtils.errorResult("handler ran");
        List<SyncToolSpecification> tools = McpToolUtils
                .adminByDefault(List.of(new SyncToolSpecification(tool("open_tool"), (ex, req) -> ok),
                        new SyncToolSpecification(tool("new_tool"), (ex, req) -> ok)), Set.of("open_tool"));
        McpSyncServerExchange user = exchangeFor(new McpUser("bob", false));

        assertSame(ok, tools.get(0).callHandler().apply(user, new CallToolRequest("open_tool", Map.of())));
        assertNotSame(ok, tools.get(1).callHandler().apply(user, new CallToolRequest("new_tool", Map.of())));
    }

    private static McpSyncServerExchange exchangeFor(McpUser user) {
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.transportContext()).thenReturn(McpTransportContext.create(Map.of(McpUser.CONTEXT_KEY, user)));
        return exchange;
    }

    private static McpSchema.Tool tool(String name) {
        return McpSchema.Tool.builder().name(name)
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null)).build();
    }

    @Test
    void testGetStringArgReturnsValue() {
        assertEquals("hello", McpToolUtils.getStringArg(Map.of("key", "hello"), "key"));
    }

    @Test
    void testGetStringArgReturnsNullWhenMissing() {
        assertNull(McpToolUtils.getStringArg(Map.of(), "key"));
    }

    @Test
    void testGetStringArgConvertsNonString() {
        assertEquals("42", McpToolUtils.getStringArg(Map.of("key", 42), "key"));
    }

    @Test
    void testGetIntArgReturnsValue() {
        assertEquals(42, McpToolUtils.getIntArg(Map.of("key", 42), "key", 0));
    }

    @Test
    void testGetIntArgReturnsDefaultWhenMissing() {
        assertEquals(10, McpToolUtils.getIntArg(Map.of(), "key", 10));
    }

    @Test
    void testGetIntArgReturnsDefaultForNonNumber() {
        assertEquals(10, McpToolUtils.getIntArg(Map.of("key", "not a number"), "key", 10));
    }

    @Test
    void testGetBooleanArgReturnsValue() {
        assertTrue(McpToolUtils.getBooleanArg(Map.of("key", true), "key", false));
    }

    @Test
    void testGetBooleanArgReturnsDefaultWhenMissing() {
        assertTrue(McpToolUtils.getBooleanArg(Map.of(), "key", true));
    }

    @Test
    void testGetBooleanArgReturnsDefaultForNonBoolean() {
        assertTrue(McpToolUtils.getBooleanArg(Map.of("key", "true"), "key", true));
    }

    @Test
    void testGetNullableBooleanArgReturnsValue() {
        assertEquals(Boolean.TRUE, McpToolUtils.getNullableBooleanArg(Map.of("key", true), "key"));
    }

    @Test
    void testGetNullableBooleanArgReturnsNullWhenMissing() {
        assertNull(McpToolUtils.getNullableBooleanArg(Map.of(), "key"));
    }

    @Test
    void testGetStringListArgReturnsList() {
        List<String> result = McpToolUtils.getStringListArg(Map.of("key", List.of("a", "b")), "key");
        assertNotNull(result);
        assertEquals(List.of("a", "b"), result);
    }

    @Test
    void testGetStringListArgReturnsNullWhenMissing() {
        assertNull(McpToolUtils.getStringListArg(Map.of(), "key"));
    }

    @Test
    void testGetStringListArgFiltersNonStrings() {
        List<String> result = McpToolUtils.getStringListArg(Map.of("key", List.of("a", 42, "b")), "key");
        assertNotNull(result);
        assertEquals(List.of("a", "b"), result);
    }

    @Test
    void testGetObjectMapArgReturnsMap() {
        Map<String, Object> inner = Map.of("a", "b");
        Map<String, Object> result = McpToolUtils.getObjectMapArg(Map.of("key", inner), "key");
        assertNotNull(result);
        assertEquals("b", result.get("a"));
    }

    @Test
    void testGetObjectMapArgReturnsNullWhenMissing() {
        assertNull(McpToolUtils.getObjectMapArg(Map.of(), "key"));
    }

    @Test
    void testTextResultSerializesToJson() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "test");
        CallToolResult result = McpToolUtils.textResult(jsonMapper, data);
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("test", parsed.get("name"));
    }

    @Test
    void testErrorResultSetsIsError() {
        CallToolResult result = McpToolUtils.errorResult("something went wrong");
        assertError(result);
        assertErrorContains(result, "something went wrong");
    }

    @Test
    void testBuildItemMapIncludesAllFields() {
        var item = createMockItem("Kitchen_Light", "Kitchen Light", "Switch", OnOffType.ON, Set.of("Light"),
                List.of("gLights"));
        Map<String, Object> map = McpToolUtils.buildItemMap(item);
        assertEquals("Kitchen_Light", map.get("name"));
        assertEquals("Kitchen Light", map.get("label"));
        assertEquals("Switch", map.get("type"));
        assertEquals("ON", map.get("state"));
        assertNotNull(map.get("tags"));
        assertNotNull(map.get("groups"));
        assertNotNull(map.get("acceptedCommands"));
    }

    @Test
    void testBuildItemMapHandlesGroupItem() {
        var member = createMockItem("Light1", "Light 1", "Switch", OnOffType.ON, Set.of(), List.of());
        var group = createMockGroupItem("gLights", "All Lights", Set.of(member));
        Map<String, Object> map = McpToolUtils.buildItemMap(group);
        assertEquals(1, map.get("memberCount"));
    }

    @Test
    void testBuildItemMapNullLabel() {
        Item item = mock(Item.class);
        when(item.getName()).thenReturn("Item1");
        when(item.getLabel()).thenReturn(null);
        when(item.getType()).thenReturn("Switch");
        when(item.getState()).thenReturn(OnOffType.ON);
        when(item.getTags()).thenReturn(Set.of());
        when(item.getGroupNames()).thenReturn(List.of());
        when(item.getAcceptedCommandTypes()).thenReturn(List.of());
        Map<String, Object> map = McpToolUtils.buildItemMap(item);
        assertEquals("", map.get("label"));
    }

    @Test
    void testBuildItemMapUndefState() {
        var item = createMockItem("Item1", "Item", "Switch", UnDefType.NULL, Set.of(), List.of());
        Map<String, Object> map = McpToolUtils.buildItemMap(item);
        assertEquals("no value", map.get("state"));
    }

    @Test
    void testTruncateShortString() {
        assertEquals("short", McpToolUtils.truncate("short", 100));
    }

    @Test
    void testTruncateLongString() {
        String result = McpToolUtils.truncate("abcdefghij", 5);
        assertEquals(6, result.length());
        assertTrue(result.endsWith("…"));
    }

    @Test
    void testTruncateNull() {
        assertEquals("", McpToolUtils.truncate(null, 100));
    }

    @Test
    void testJacksonInstance() {
        assertNotNull(McpToolUtils.jackson());
    }

    @Test
    void testTextResultWithStringState() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("state", new StringType("hello").toString());
        CallToolResult result = McpToolUtils.textResult(jsonMapper, data);
        Map<String, Object> parsed = parseResult(result);
        assertEquals("hello", parsed.get("state"));
    }

    @Test
    void testWriteAnnotationsSerializeExplicitDestructiveHint() throws Exception {
        String json = jsonMapper.writeValueAsString(McpToolUtils.writeAnnotations(false, true));
        assertTrue(json.contains("\"readOnlyHint\":false"), json);
        assertTrue(json.contains("\"destructiveHint\":false"), json);
        assertTrue(json.contains("\"idempotentHint\":true"), json);
    }

    @Test
    void testReadOnlyAnnotationsOmitWriteHints() throws Exception {
        String json = jsonMapper.writeValueAsString(McpToolUtils.readOnlyAnnotations());
        assertTrue(json.contains("\"readOnlyHint\":true"), json);
        assertFalse(json.contains("destructiveHint"), json);
        assertFalse(json.contains("idempotentHint"), json);
    }
}
