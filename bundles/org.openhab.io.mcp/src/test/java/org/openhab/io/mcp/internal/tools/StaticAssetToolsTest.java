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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link StaticAssetTools}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class StaticAssetToolsTest {

    private static final String TOKEN = "test-token";
    private static final String BASE_URL = "http://localhost:8080";

    @TempDir
    @Nullable
    Path tempDir;

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
    McpSyncServerExchange exchange;

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();
    private final Function<String, @Nullable String> tokenSupplier = sid -> TOKEN;

    @BeforeEach
    void setUp() throws Exception {
        Request r = requireNonNull(request);
        lenient().when(r.method(any(HttpMethod.class))).thenReturn(r);
        lenient().when(r.header(anyString(), anyString())).thenReturn(r);
        lenient().when(r.timeout(anyLong(), any(TimeUnit.class))).thenReturn(r);
        lenient().when(r.send()).thenReturn(requireNonNull(response));
        lenient().when(httpClient.newRequest(any(URI.class))).thenReturn(r);
        lenient().when(exchange.sessionId()).thenReturn("session-1");
        // Default: admin probe returns 200 so tests focus on filesystem behaviour rather than auth plumbing.
        lenient().when(response.getStatus()).thenReturn(200);
    }

    private StaticAssetTools tools() {
        return new StaticAssetTools(requireNonNull(tempDir), BASE_URL, requireNonNull(httpClient), tokenSupplier,
                jsonMapper);
    }

    private static <T> T requireNonNull(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    private CallToolResult call(StaticAssetTools t, Map<String, Object> args) {
        return t.handleManageStaticAsset(requireNonNull(exchange), createRequest(args));
    }

    private static Map<String, Object> args(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }

    // ============ admin gate ============

    @Test
    void rejectsWhenAdminProbeReturns403() throws Exception {
        when(response.getStatus()).thenReturn(403);
        CallToolResult result = call(tools(), args("action", "list"));
        assertErrorContains(result, "administrator-scoped token");
    }

    @Test
    void rejectsWhenSessionHasNoToken() throws Exception {
        Function<String, @Nullable String> noToken = sid -> null;
        StaticAssetTools t = new StaticAssetTools(requireNonNull(tempDir), BASE_URL, requireNonNull(httpClient),
                noToken, jsonMapper);
        CallToolResult result = call(t, args("action", "list"));
        assertErrorContains(result, "No bearer token");
    }

    // ============ list ============

    @Test
    void listReturnsEmptyForFreshRoot() throws Exception {
        // Delete the @TempDir so we exercise the missing-root branch.
        Files.delete(requireNonNull(tempDir));
        CallToolResult result = call(tools(), args("action", "list"));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        assertEquals(0, parsed.get("total"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void listEnumeratesFiles() throws Exception {
        Files.createDirectories(requireNonNull(tempDir).resolve("plans"));
        Files.writeString(tempDir.resolve("plans/floor.png"), "x");
        Files.writeString(tempDir.resolve("style.css"), "body{}");
        CallToolResult result = call(tools(), args("action", "list"));
        assertSuccess(result);
        Map<String, Object> parsed = parseResult(result);
        List<Map<String, Object>> files = (List<Map<String, Object>>) parsed.get("files");
        assertNotNull(files);
        assertEquals(2, files.size());
        // sorted: plans/floor.png comes before style.css
        assertEquals("plans/floor.png", files.get(0).get("path"));
        assertEquals("/static/plans/floor.png", files.get(0).get("url"));
    }

    @Test
    void listSkipsHiddenFiles() throws Exception {
        Files.writeString(requireNonNull(tempDir).resolve(".hidden"), "x");
        Files.writeString(tempDir.resolve("visible.txt"), "x");
        CallToolResult result = call(tools(), args("action", "list"));
        Map<String, Object> parsed = parseResult(result);
        assertEquals(1, parsed.get("total"));
    }

    // ============ put / get round-trip ============

    @Test
    @SuppressWarnings("unchecked")
    void putAndGetRoundTripBinary() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 3, 4, 5 };
        String b64 = Base64.getEncoder().encodeToString(bytes);
        StaticAssetTools t = tools();
        CallToolResult put = call(t, args("action", "put", "path", "icons/x.png", "content", b64));
        assertSuccess(put);
        Map<String, Object> putParsed = parseResult(put);
        assertEquals(true, putParsed.get("success"));
        assertEquals("/static/icons/x.png", putParsed.get("url"));
        assertEquals(BASE_URL + "/static/icons/x.png", putParsed.get("viewUrl"));
        assertEquals(5, putParsed.get("sizeBytes"));
        // File actually on disk
        assertArrayEquals(bytes, Files.readAllBytes(requireNonNull(tempDir).resolve("icons/x.png")));

        CallToolResult get = call(t, args("action", "get", "path", "icons/x.png"));
        Map<String, Object> getParsed = parseResult(get);
        assertEquals("base64", getParsed.get("encoding"));
        assertEquals(b64, getParsed.get("content"));
    }

    @Test
    void putRefusesOverwriteByDefault() throws Exception {
        Files.writeString(requireNonNull(tempDir).resolve("existing.txt"), "old");
        CallToolResult result = call(tools(),
                args("action", "put", "path", "existing.txt", "content", "new", "encoding", "utf8"));
        assertErrorContains(result, "Refusing to overwrite");
        // Confirm we didn't corrupt the file.
        assertEquals("old", Files.readString(tempDir.resolve("existing.txt")));
    }

    @Test
    void putWithOverwriteSucceeds() throws Exception {
        Files.writeString(requireNonNull(tempDir).resolve("existing.txt"), "old");
        CallToolResult result = call(tools(),
                args("action", "put", "path", "existing.txt", "content", "new", "encoding", "utf8", "overwrite", true));
        assertSuccess(result);
        assertEquals("new", Files.readString(tempDir.resolve("existing.txt")));
    }

    @Test
    void putRejectsUnknownExtension() throws Exception {
        CallToolResult result = call(tools(), args("action", "put", "path", "thing.exe", "content", "AA=="));
        assertErrorContains(result, "extension 'exe' is not allowed");
    }

    @Test
    void putEnforcesSizeCap() throws Exception {
        // Encoded length much larger than 10MB * 4/3 cap.
        String giant = "A".repeat(20 * 1024 * 1024);
        CallToolResult result = call(tools(), args("action", "put", "path", "big.png", "content", giant));
        assertErrorContains(result, "per-call cap");
    }

    @Test
    void putFullDetailEchoesMetadata() throws Exception {
        CallToolResult result = call(tools(), args("action", "put", "path", "a.css", "content", "body{}", "encoding",
                "utf8", "responseDetail", "full"));
        Map<String, Object> parsed = parseResult(result);
        assertNotNull(parsed.get("mimeType"));
        assertNotNull(parsed.get("lastModified"));
    }

    // ============ path safety ============

    @Test
    void rejectsAbsolutePath() throws Exception {
        CallToolResult result = call(tools(), args("action", "put", "path", "/etc/passwd", "content", "x"));
        assertErrorContains(result, "must be relative");
    }

    @Test
    void rejectsParentTraversal() throws Exception {
        CallToolResult result = call(tools(), args("action", "put", "path", "../secret.png", "content", "x"));
        assertErrorContains(result, "must not contain '..'");
    }

    @Test
    void rejectsHiddenSegment() throws Exception {
        CallToolResult result = call(tools(),
                args("action", "put", "path", ".env/.config", "content", "x", "encoding", "utf8"));
        assertErrorContains(result, "hidden segments");
    }

    @Test
    void rejectsNullByteInPath() throws Exception {
        CallToolResult result = call(tools(), args("action", "put", "path", "foo\0bar.png", "content", "x"));
        assertErrorContains(result, "null byte");
    }

    @Test
    void rejectsReadThroughSymlinkEscape() throws Exception {
        // Plant a symlink in the html root that points to a sibling dir outside it, then try to
        // read a file through the symlink. The textual startsWith guard passes; the real-path check
        // must reject it.
        Path root = requireNonNull(tempDir);
        Path outside = Files.createTempDirectory("static-asset-outside-");
        try {
            Files.writeString(outside.resolve("secret.png"), "SECRET");
            try {
                Files.createSymbolicLink(root.resolve("escape"), outside);
            } catch (java.nio.file.FileSystemException e) {
                // Windows without symlink privileges — skip the test rather than fail spuriously.
                org.junit.jupiter.api.Assumptions.abort("Symlinks not supported on this filesystem: " + e.getMessage());
            }
            CallToolResult getResult = call(tools(), args("action", "get", "path", "escape/secret.png"));
            assertErrorContains(getResult, "outside the static asset folder");

            // Same defense for put — attacker shouldn't be able to clobber a file outside via a symlink.
            CallToolResult putResult = call(tools(),
                    args("action", "put", "path", "escape/poisoned.txt", "content", "x", "encoding", "utf8"));
            assertErrorContains(putResult, "outside the static asset folder");
            assertFalse(Files.exists(outside.resolve("poisoned.txt")));
        } finally {
            // Best-effort cleanup of the outside dir; the @TempDir cleanup will handle root.
            Files.deleteIfExists(outside.resolve("secret.png"));
            Files.deleteIfExists(outside);
        }
    }

    // ============ delete ============

    @Test
    void deleteRemovesFile() throws Exception {
        Path target = requireNonNull(tempDir).resolve("plans/p.png");
        Files.createDirectories(target.getParent());
        Files.writeString(target, "x");
        CallToolResult result = call(tools(), args("action", "delete", "path", "plans/p.png"));
        assertSuccess(result);
        assertFalse(Files.exists(target));
    }

    @Test
    void deleteMissingFails() throws Exception {
        CallToolResult result = call(tools(), args("action", "delete", "path", "missing.png"));
        assertErrorContains(result, "not found");
    }

    // ============ invalid action ============

    @Test
    void missingActionRejected() throws Exception {
        CallToolResult result = call(tools(), args());
        assertErrorContains(result, "'action' is required");
    }

    @Test
    void invalidActionRejected() throws Exception {
        CallToolResult result = call(tools(), args("action", "wat"));
        assertErrorContains(result, "Invalid action");
    }
}
