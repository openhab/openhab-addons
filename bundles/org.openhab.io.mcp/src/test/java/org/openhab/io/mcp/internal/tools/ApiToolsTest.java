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
import java.util.Map;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.io.mcp.internal.McpTestHelper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Tests for {@link ApiTools} request {@code Content-Type} selection.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class ApiToolsTest {

    private static final String BASE_URL = "http://localhost:8080";

    private static final String SPEC = """
            {
              "paths": {
                "/rules/{ruleUID}/enable": { "post": { "requestBody": { "content": { "text/plain": {} } } } },
                "/items/{itemname}":       { "put":  { "requestBody": { "content": { "application/json": {} } } } },
                "/items/{itemname}/x":     { "post": { "requestBody": { "content": {
                                              "application/json": {}, "text/plain": {} } } } }
              }
            }
            """;

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
    private final Function<String, @Nullable String> tokenSupplier = sid -> "test-token";

    @BeforeEach
    void setUp() throws Exception {
        Request r = requireNonNull(request);
        lenient().when(r.method(any(HttpMethod.class))).thenReturn(r);
        lenient().when(r.header(anyString(), anyString())).thenReturn(r);
        lenient().when(r.content(any(), anyString())).thenReturn(r);
        lenient().when(r.send()).thenReturn(requireNonNull(response));
        lenient().when(httpClient.newRequest(any(URI.class))).thenReturn(r);
        lenient().when(exchange.sessionId()).thenReturn("session-1");
    }

    private ApiTools tools() {
        return new ApiTools(requireNonNull(httpClient), BASE_URL, tokenSupplier, jsonMapper);
    }

    private void stubSpecAvailable() {
        ContentResponse resp = requireNonNull(response);
        lenient().when(resp.getStatus()).thenReturn(200);
        lenient().when(resp.getContentAsString()).thenReturn(SPEC);
    }

    private void stubSpecUnavailable() {
        ContentResponse resp = requireNonNull(response);
        lenient().when(resp.getStatus()).thenReturn(500);
        lenient().when(resp.getContentAsString()).thenReturn("");
    }

    private String capturedContentType() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(requireNonNull(request)).content(any(), captor.capture());
        return captor.getValue();
    }

    private CallToolResult callApi(Map<String, Object> args) {
        return tools().handleCallApi(requireNonNull(exchange), createRequest(args));
    }

    private static <T> T requireNonNull(@Nullable T value) {
        assertNotNull(value);
        return value;
    }

    @Test
    void textPlainEndpointSendsTextPlain() {
        stubSpecAvailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/rules/{ruleUID}/enable", "pathParams",
                Map.of("ruleUID", "myRule"), "body", "true"));
        assertSuccess(result);
        assertEquals("text/plain", capturedContentType());
    }

    @Test
    void textPlainEndpointMatchedWhenPathParamsAreInlined() {
        stubSpecAvailable();
        // No pathParams: value inlined in the path, so the spec template is matched structurally.
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/rules/myRule/enable", "body", "false"));
        assertSuccess(result);
        assertEquals("text/plain", capturedContentType());
    }

    @Test
    void jsonEndpointSendsJson() {
        stubSpecAvailable();
        CallToolResult result = callApi(Map.of("method", "PUT", "path", "/items/{itemname}", "pathParams",
                Map.of("itemname", "Light"), "body", Map.of("type", "Switch")));
        assertSuccess(result);
        assertEquals("application/json", capturedContentType());
    }

    @Test
    void endpointAcceptingBothPrefersTextForStringBody() {
        stubSpecAvailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/items/{itemname}/x", "pathParams",
                Map.of("itemname", "Light"), "body", "ON"));
        assertSuccess(result);
        assertEquals("text/plain", capturedContentType());
    }

    @Test
    void endpointAcceptingBothPrefersJsonForStructuredBody() {
        stubSpecAvailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/items/{itemname}/x", "pathParams",
                Map.of("itemname", "Light"), "body", Map.of("a", 1)));
        assertSuccess(result);
        assertEquals("application/json", capturedContentType());
    }

    @Test
    void explicitContentTypeOverrideWins() {
        stubSpecAvailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/rules/{ruleUID}/enable", "pathParams",
                Map.of("ruleUID", "myRule"), "body", "true", "contentType", "application/xml"));
        assertSuccess(result);
        assertEquals("application/xml", capturedContentType());
    }

    @Test
    void fallbackUsesTextPlainForStringBodyWhenSpecUnavailable() {
        stubSpecUnavailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/some/unknown/path", "body", "raw"));
        assertSuccess(result);
        assertEquals("text/plain", capturedContentType());
    }

    @Test
    void fallbackUsesJsonForStructuredBodyWhenSpecUnavailable() {
        stubSpecUnavailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/some/unknown/path", "body", Map.of("a", 1)));
        assertSuccess(result);
        assertEquals("application/json", capturedContentType());
    }

    @Test
    void textPlainEndpointSendsTextPlainForBooleanBody() {
        stubSpecAvailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/rules/{ruleUID}/enable", "pathParams",
                Map.of("ruleUID", "myRule"), "body", true));
        assertSuccess(result);
        assertEquals("text/plain", capturedContentType());
    }

    @Test
    void fallbackUsesTextPlainForScalarBodyWhenSpecUnavailable() {
        stubSpecUnavailable();
        CallToolResult result = callApi(Map.of("method", "POST", "path", "/some/unknown/path", "body", true));
        assertSuccess(result);
        assertEquals("text/plain", capturedContentType());
    }
}
