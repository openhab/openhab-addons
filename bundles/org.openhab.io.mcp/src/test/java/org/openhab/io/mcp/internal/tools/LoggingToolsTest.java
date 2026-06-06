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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.io.mcp.internal.McpTestHelper.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.io.mcp.internal.McpTestHelper;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogReaderService;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link LoggingTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class LoggingToolsTest {

    private static final String TOKEN = "test-token";
    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    @Nullable
    LogReaderService logReaderService;

    @Mock
    @Nullable
    HttpClient httpClient;

    @Mock
    @Nullable
    Request request;

    @Mock
    @Nullable
    ContentResponse response;

    @Mock
    @Nullable
    ScheduledExecutorService scheduler;

    @Mock
    @Nullable
    McpSyncServerExchange exchange;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();
    private final Function<String, @Nullable String> tokenSupplier = sid -> TOKEN;

    private LoggingTools tools() {
        return new LoggingTools(requireNonNull(logReaderService), requireNonNull(httpClient), BASE_URL, tokenSupplier,
                requireNonNull(scheduler), jsonMapper);
    }

    private static <T> T requireNonNull(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Fluent Request chain: every builder method returns the same Request mock.
        Request r = requireNonNull(request);
        lenient().when(r.method(any(HttpMethod.class))).thenReturn(r);
        lenient().when(r.header(anyString(), anyString())).thenReturn(r);
        lenient().when(r.content(any(), anyString())).thenReturn(r);
        lenient().when(r.send()).thenReturn(requireNonNull(response));
        lenient().when(httpClient.newRequest(any(URI.class))).thenReturn(r);
        lenient().when(exchange.sessionId()).thenReturn("session-1");
    }

    private LogEntry mockEntry(long sequence, long timeMs, LogLevel level, String loggerName, String message,
            @Nullable Throwable t) {
        LogEntry e = mock(LogEntry.class);
        lenient().when(e.getSequence()).thenReturn(sequence);
        lenient().when(e.getTime()).thenReturn(timeMs);
        lenient().when(e.getLogLevel()).thenReturn(level);
        lenient().when(e.getLoggerName()).thenReturn(loggerName);
        lenient().when(e.getMessage()).thenReturn(message);
        lenient().when(e.getException()).thenReturn(t);
        return e;
    }

    /** Returns an enumeration with the newest-first ordering that PaxLogging produces. */
    private Enumeration<LogEntry> newestFirst(LogEntry... entries) {
        List<LogEntry> list = new ArrayList<>(List.of(entries));
        return Collections.enumeration(list);
    }

    // ============ get_logs ============

    @Test
    void getLogsReturnsChronologicallyAndCapturesLastSequence() throws Exception {
        LogEntry e1 = mockEntry(102, 2_000L, LogLevel.WARN, "org.openhab.binding.foo", "second", null);
        LogEntry e2 = mockEntry(101, 1_000L, LogLevel.WARN, "org.openhab.binding.foo", "first", null);
        when(logReaderService.getLog()).thenReturn(newestFirst(e1, e2));

        CallToolResult result = tools().handleGetLogs(createRequest(Map.of()));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) parsed.get("entries");
        assertEquals(2, entries.size());
        assertEquals("first", entries.get(0).get("message"));
        assertEquals("second", entries.get(1).get("message"));
        assertEquals(102, parsed.get("lastSequence"));
        assertEquals(2, parsed.get("returned"));
    }

    @Test
    void getLogsMinLevelDropsBelow() throws Exception {
        LogEntry warn = mockEntry(10, 1L, LogLevel.WARN, "x", "warn", null);
        LogEntry info = mockEntry(9, 1L, LogLevel.INFO, "x", "info", null);
        LogEntry debug = mockEntry(8, 1L, LogLevel.DEBUG, "x", "debug", null);
        when(logReaderService.getLog()).thenReturn(newestFirst(warn, info, debug));

        CallToolResult result = tools().handleGetLogs(createRequest(Map.of("minLevel", "WARN")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) parsed.get("entries");
        assertEquals(1, entries.size());
        assertEquals("warn", entries.get(0).get("message"));
    }

    @Test
    void getLogsLoggerFilterIsRegex() throws Exception {
        LogEntry zigbee = mockEntry(10, 1L, LogLevel.INFO, "org.openhab.binding.zigbee.handler", "z", null);
        LogEntry zwave = mockEntry(9, 1L, LogLevel.INFO, "org.openhab.binding.zwave", "zw", null);
        LogEntry other = mockEntry(8, 1L, LogLevel.INFO, "org.eclipse.jetty.server", "j", null);
        when(logReaderService.getLog()).thenReturn(newestFirst(zigbee, zwave, other));

        CallToolResult result = tools()
                .handleGetLogs(createRequest(Map.of("loggerFilter", "org\\.openhab\\.binding\\..*")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) parsed.get("entries");
        assertEquals(2, entries.size());
    }

    @Test
    void getLogsSinceSequenceFiltersOutOlder() throws Exception {
        LogEntry e3 = mockEntry(13, 3L, LogLevel.INFO, "x", "c", null);
        LogEntry e2 = mockEntry(12, 2L, LogLevel.INFO, "x", "b", null);
        LogEntry e1 = mockEntry(11, 1L, LogLevel.INFO, "x", "a", null);
        when(logReaderService.getLog()).thenReturn(newestFirst(e3, e2, e1));

        CallToolResult result = tools().handleGetLogs(createRequest(Map.of("sinceSequence", 11)));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) parsed.get("entries");
        assertEquals(2, entries.size());
        assertEquals("b", entries.get(0).get("message"));
        assertEquals("c", entries.get(1).get("message"));
    }

    @Test
    void getLogsIncludesStackTrace() throws Exception {
        Throwable t = new IllegalStateException("kaboom");
        LogEntry e = mockEntry(1, 1L, LogLevel.ERROR, "x", "oops", t);
        when(logReaderService.getLog()).thenReturn(newestFirst(e));

        CallToolResult result = tools().handleGetLogs(createRequest(Map.of("minLevel", "ERROR")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) parsed.get("entries");
        assertEquals(1, entries.size());
        String trace = (String) entries.get(0).get("stackTrace");
        assertNotNull(trace);
        assertTrue(trace.contains("IllegalStateException"));
        assertTrue(trace.contains("kaboom"));
    }

    @Test
    void getLogsInvalidRegexReportsError() {
        CallToolResult result = tools().handleGetLogs(createRequest(Map.of("loggerFilter", "[unclosed")));
        assertErrorContains(result, "Invalid loggerFilter");
    }

    @Test
    void getLogsRespectsLimit() throws Exception {
        LogEntry[] arr = new LogEntry[5];
        for (int i = 0; i < 5; i++) {
            arr[i] = mockEntry(100 - i, 1L, LogLevel.INFO, "x", "m" + i, null);
        }
        when(logReaderService.getLog()).thenReturn(newestFirst(arr));

        CallToolResult result = tools().handleGetLogs(createRequest(Map.of("limit", 2)));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) parsed.get("entries");
        assertEquals(2, entries.size());
    }

    // ============ manage_log_level (action=get) ============

    @Test
    void getLogLevelsForwardsBearerToken() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\"loggers\":[{\"loggerName\":\"org.openhab.binding.zigbee\",\"level\":\"DEBUG\"}]}");

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "get")));
        assertSuccess(result);
        verify(httpClient).newRequest(URI.create("http://localhost:8080/rest/logging/"));
        verify(request).method(HttpMethod.GET);
        verify(request).header("Authorization", "Bearer " + TOKEN);
    }

    @Test
    void getLogLevelsFiltersBySubstring() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("""
                {"loggers":[
                  {"loggerName":"org.openhab.binding.zigbee","level":"DEBUG"},
                  {"loggerName":"org.openhab.binding.zwave","level":"INFO"},
                  {"loggerName":"org.eclipse.jetty.server","level":"WARN"}
                ]}""");

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "get", "loggerFilter", "binding")));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(3, parsed.get("total"));
        assertEquals(2, parsed.get("returned"));
    }

    @Test
    void getLogLevelsForwards403AsAuthError() throws Exception {
        when(response.getStatus()).thenReturn(403);

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "get")));
        assertErrorContains(result, "ADMIN");
    }

    @Test
    void getLogLevelsNoTokenReturnsError() {
        Function<String, @Nullable String> noToken = sid -> null;
        LoggingTools t = new LoggingTools(requireNonNull(logReaderService), requireNonNull(httpClient), BASE_URL,
                noToken, requireNonNull(scheduler), jsonMapper);

        CallToolResult result = t.handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "get")));
        assertErrorContains(result, "bearer token");
    }

    @Test
    void manageLogLevelMissingAction() {
        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange), createRequest(Map.of()));
        assertErrorContains(result, "action");
    }

    @Test
    void manageLogLevelUnknownAction() {
        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "wipe")));
        assertErrorContains(result, "Invalid action");
    }

    // ============ manage_log_level (action=set) ============

    @Test
    void setLogLevelSchedulesRevertWithCapturedPreviousLevel() throws Exception {
        // 1) GET for previous level returns INFO; 2) PUT returns 200.
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\"loggers\":[{\"loggerName\":\"org.openhab.binding.foo\",\"level\":\"INFO\"}]}");
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenAnswer(inv -> future);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set");
        args.put("loggerName", "org.openhab.binding.foo");
        args.put("level", "DEBUG");
        args.put("revertAfterSeconds", 300);

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);

        Map<String, Object> parsed = parseResult(result);
        assertEquals("INFO", parsed.get("previousLevel"));
        assertEquals("DEBUG", parsed.get("newLevel"));
        assertEquals(300, parsed.get("revertAfterSeconds"));

        ArgumentCaptor<Long> delay = ArgumentCaptor.forClass(Long.class);
        verify(scheduler).schedule(any(Runnable.class), delay.capture(), eq(TimeUnit.SECONDS));
        assertEquals(300L, delay.getValue());
    }

    @Test
    void setLogLevelPersistentSkipsRevert() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"loggers\":[{\"loggerName\":\"x\",\"level\":\"INFO\"}]}");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set");
        args.put("loggerName", "x");
        args.put("level", "DEBUG");
        args.put("revertAfterSeconds", 0);

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void setLogLevelReplacesPriorRevertForSameLogger() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"loggers\":[{\"loggerName\":\"x\",\"level\":\"INFO\"}]}");
        ScheduledFuture<?> first = mock(ScheduledFuture.class);
        ScheduledFuture<?> second = mock(ScheduledFuture.class);
        ScheduledFuture<?>[] queue = new ScheduledFuture<?>[] { first, second };
        int[] idx = new int[] { 0 };
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenAnswer(inv -> queue[idx[0]++]);

        LoggingTools t = tools();
        Map<String, Object> args = new HashMap<>();
        args.put("action", "set");
        args.put("loggerName", "x");
        args.put("level", "DEBUG");
        args.put("revertAfterSeconds", 300);

        t.handleManageLogLevel(requireNonNull(exchange), createRequest(args));
        t.handleManageLogLevel(requireNonNull(exchange), createRequest(args));
        verify(first).cancel(false);
    }

    @Test
    void setLogLevelDefaultIssuesDelete() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"loggers\":[{\"loggerName\":\"x\",\"level\":\"DEBUG\"}]}");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set");
        args.put("loggerName", "x");
        args.put("level", "DEFAULT");
        args.put("revertAfterSeconds", 0); // skip the auto-revert path (no scheduler stub in this test)

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange), createRequest(args));
        assertSuccess(result);
        // Two HTTP calls happen: GET for previous level, then DELETE for the change.
        verify(request, Mockito.atLeastOnce()).method(HttpMethod.DELETE);
    }

    @Test
    void setLogLevelReturnsAuthErrorOn403() throws Exception {
        when(response.getStatus()).thenReturn(403);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set");
        args.put("loggerName", "x");
        args.put("level", "DEBUG");

        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange), createRequest(args));
        assertErrorContains(result, "ADMIN");
    }

    @Test
    void setLogLevelRequiresNameAndLevel() {
        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "set")));
        assertErrorContains(result, "required");
    }

    @Test
    void setLogLevelRejectsInvalidLevel() {
        CallToolResult result = tools().handleManageLogLevel(requireNonNull(exchange),
                createRequest(Map.of("action", "set", "loggerName", "x", "level", "PARTY")));
        assertErrorContains(result, "Invalid level");
    }

    @Test
    void cancelPendingRevertsClearsAll() throws Exception {
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\"loggers\":[{\"loggerName\":\"x\",\"level\":\"INFO\"}]}");
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenAnswer(inv -> future);

        LoggingTools t = tools();
        Map<String, Object> args = new HashMap<>();
        args.put("action", "set");
        args.put("loggerName", "x");
        args.put("level", "DEBUG");
        args.put("revertAfterSeconds", 300);
        t.handleManageLogLevel(requireNonNull(exchange), createRequest(args));

        t.cancelPendingReverts();
        verify(future).cancel(false);
    }
}
