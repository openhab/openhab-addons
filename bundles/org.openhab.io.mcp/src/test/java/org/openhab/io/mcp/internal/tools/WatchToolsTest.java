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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.io.mcp.internal.McpTestHelper;
import org.openhab.io.mcp.internal.util.SubscriptionManager;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link WatchTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class WatchToolsTest {

    @Mock
    @Nullable
    McpSyncServerExchange exchange;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();
    private @Nullable SubscriptionManager subscriptions;
    private @Nullable WatchTools watchTools;

    @BeforeEach
    void setUp() {
        subscriptions = new SubscriptionManager();
        watchTools = new WatchTools(Objects.requireNonNull(subscriptions), jsonMapper);
        lenient().when(Objects.requireNonNull(exchange).sessionId()).thenReturn("session1");
    }

    private WatchTools tools() {
        WatchTools t = watchTools;
        assertNotNull(t);
        return t;
    }

    private SubscriptionManager subs() {
        SubscriptionManager s = subscriptions;
        assertNotNull(s);
        return s;
    }

    private McpSyncServerExchange ex() {
        McpSyncServerExchange e = exchange;
        assertNotNull(e);
        return e;
    }

    @Test
    @SuppressWarnings("unchecked")
    void watchItemsSuccess() throws Exception {
        CallToolResult result = tools().handleWatchItems(ex(),
                createRequest(Map.of("itemNames", List.of("Item1", "Item2"))));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<String> added = (List<String>) parsed.get("added");
        assertNotNull(added);
        assertEquals(2, added.size());
        assertTrue(added.contains("Item1"));
        assertTrue(added.contains("Item2"));

        List<String> allWatched = (List<String>) parsed.get("allWatched");
        assertNotNull(allWatched);
        assertEquals(2, allWatched.size());
    }

    @Test
    void watchItemsMissingParam() throws Exception {
        CallToolResult result = tools().handleWatchItems(ex(), createRequest(Map.of()));
        assertErrorContains(result, "itemNames");
    }

    @Test
    void watchItemsEmptyList() throws Exception {
        CallToolResult result = tools().handleWatchItems(ex(), createRequest(Map.of("itemNames", List.of())));
        assertErrorContains(result, "itemNames");
    }

    @Test
    @SuppressWarnings("unchecked")
    void unwatchItemsSpecific() throws Exception {
        subs().watch("session1", List.of("Item1", "Item2", "Item3"));

        CallToolResult result = tools().handleUnwatchItems(ex(), createRequest(Map.of("itemNames", List.of("Item2"))));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<String> removed = (List<String>) parsed.get("removed");
        assertNotNull(removed);
        assertEquals(1, removed.size());
        assertEquals("Item2", removed.get(0));

        List<String> remaining = (List<String>) parsed.get("remaining");
        assertNotNull(remaining);
        assertEquals(2, remaining.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void unwatchItemsAll() throws Exception {
        subs().watch("session1", List.of("Item1", "Item2"));

        CallToolResult result = tools().handleUnwatchItems(ex(), createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<String> removed = (List<String>) parsed.get("removed");
        assertNotNull(removed);
        assertEquals(2, removed.size());

        List<String> remaining = (List<String>) parsed.get("remaining");
        assertNotNull(remaining);
        assertTrue(remaining.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEventsReturnsBufferedEvents() throws Exception {
        subs().watch("session1", List.of("Item1"));
        subs().recordIfWatched("Item1", "ON", "OFF", Instant.parse("2026-01-01T00:00:00Z"));

        CallToolResult result = tools().handleGetEvents(ex(), createRequest(Map.of()));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> events = (List<Map<String, Object>>) parsed.get("events");
        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("Item1", events.get(0).get("itemName"));
        assertEquals("ON", events.get(0).get("state"));
        assertEquals("OFF", events.get(0).get("oldState"));
        assertEquals(1, parsed.get("count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEventsDrainsBuffer() throws Exception {
        subs().watch("session1", List.of("Item1"));
        subs().recordIfWatched("Item1", "ON", "OFF", Instant.now());

        tools().handleGetEvents(ex(), createRequest(Map.of()));

        CallToolResult result2 = tools().handleGetEvents(ex(), createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result2);
        List<Map<String, Object>> events = (List<Map<String, Object>>) parsed.get("events");
        assertNotNull(events);
        assertTrue(events.isEmpty());
        assertEquals(0, parsed.get("count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEventsIncludesWatchedList() throws Exception {
        subs().watch("session1", List.of("Item1", "Item2"));

        CallToolResult result = tools().handleGetEvents(ex(), createRequest(Map.of()));
        Map<String, Object> parsed = parseResult(result);
        List<String> watched = (List<String>) parsed.get("watched");
        assertNotNull(watched);
        assertEquals(2, watched.size());
        assertTrue(watched.contains("Item1"));
        assertTrue(watched.contains("Item2"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void sessionIsolation() throws Exception {
        subs().watch("session1", List.of("Item1"));
        subs().recordIfWatched("Item1", "ON", "OFF", Instant.now());

        McpSyncServerExchange exchange2 = mock(McpSyncServerExchange.class);
        when(exchange2.sessionId()).thenReturn("session2");

        subs().watch("session2", List.of("Item2"));

        CallToolResult result1 = tools().handleGetEvents(ex(), createRequest(Map.of()));
        Map<String, Object> parsed1 = parseResult(result1);
        List<Map<String, Object>> events1 = (List<Map<String, Object>>) parsed1.get("events");
        assertNotNull(events1);
        assertEquals(1, events1.size());

        CallToolResult result2 = tools().handleGetEvents(exchange2, createRequest(Map.of()));
        Map<String, Object> parsed2 = parseResult(result2);
        List<Map<String, Object>> events2 = (List<Map<String, Object>>) parsed2.get("events");
        assertNotNull(events2);
        assertTrue(events2.isEmpty());
    }
}
