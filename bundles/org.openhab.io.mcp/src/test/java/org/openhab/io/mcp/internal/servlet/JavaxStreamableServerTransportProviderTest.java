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
package org.openhab.io.mcp.internal.servlet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.io.mcp.internal.McpTestHelper;
import org.openhab.io.mcp.internal.auth.McpAuthenticator;
import org.openhab.io.mcp.internal.auth.McpUser;
import org.openhab.io.mcp.internal.tools.McpToolUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.HttpHeaders;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests for {@link JavaxStreamableServerTransportProvider}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class JavaxStreamableServerTransportProviderTest {

    private static final String ADMIN_AUTH = "Bearer admin-token";
    private static final String USER_AUTH = "Bearer user-token";
    private static final Map<String, McpUser> USERS = Map.of(ADMIN_AUTH, new McpUser("alice", true), USER_AUTH,
            new McpUser("bob", false));

    private final McpJsonMapper jsonMapper = McpTestHelper.newJsonMapper();
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    @Nullable
    McpAuthenticator authenticator;

    private @Nullable McpSyncServer server;
    private @Nullable JavaxStreamableServerTransportProvider transport;

    @BeforeEach
    void setUp() {
        McpAuthenticator auth = Objects.requireNonNull(authenticator);
        when(auth.authenticate(any()))
                .thenAnswer(inv -> USERS.get(((HttpServletRequest) inv.getArgument(0)).getHeader("Authorization")));
        JavaxStreamableServerTransportProvider t = new JavaxStreamableServerTransportProvider(jsonMapper, auth, null);
        transport = t;

        SyncToolSpecification whoami = new SyncToolSpecification(tool("whoami"), (exchange, req) -> {
            McpUser user = McpUser.of(exchange);
            return text(user == null ? "none" : user.name() + ":" + user.admin());
        });
        SyncToolSpecification adminTool = new SyncToolSpecification(tool("admin_tool"),
                (exchange, req) -> text("admin ran"));
        server = McpServer.sync(t).jsonMapper(jsonMapper).jsonSchemaValidator(new DefaultJsonSchemaValidator())
                .serverInfo("test", "0").capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .tools(McpToolUtils.adminByDefault(List.of(whoami, adminTool), Set.of("whoami"))).build();
    }

    @AfterEach
    void tearDown() {
        McpSyncServer s = server;
        if (s != null) {
            s.closeGracefully();
        }
    }

    @Test
    void eachToolCallSeesTheUserOfItsOwnRequest() throws Exception {
        String sessionId = initialize(ADMIN_AUTH);

        assertEquals("alice:true", callTool(ADMIN_AUTH, sessionId, "whoami"));
        assertEquals("bob:false", callTool(USER_AUTH, sessionId, "whoami"));
        assertEquals("alice:true", callTool(ADMIN_AUTH, sessionId, "whoami"));
    }

    @Test
    void adminToolFollowsTheRequestTokenNotTheSession() throws Exception {
        String sessionId = initialize(ADMIN_AUTH);

        assertTrue(callTool(USER_AUTH, sessionId, "admin_tool").contains("administrator"));
        assertEquals("admin ran", callTool(ADMIN_AUTH, sessionId, "admin_tool"));
    }

    @Test
    void rejectedTokenGets401() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        Objects.requireNonNull(transport).doPost(request("Bearer unknown", null, initializeBody()), response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private String initialize(String auth) throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        Objects.requireNonNull(transport).doPost(request(auth, null, initializeBody()), response);

        ArgumentCaptor<String> sessionId = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(HttpHeaders.MCP_SESSION_ID), sessionId.capture());
        return sessionId.getValue();
    }

    private String callTool(String auth, String sessionId, String toolName) throws Exception {
        StringWriter body = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        String call = """
                {"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"%s","arguments":{}}}"""
                .formatted(toolName);

        Objects.requireNonNull(transport).doPost(request(auth, sessionId, call), response);

        String data = body.toString().lines().filter(l -> l.startsWith("data: ")).findFirst().orElseThrow()
                .substring("data: ".length());
        JsonNode result = mapper.readTree(data).path("result");
        return result.path("content").get(0).path("text").asText();
    }

    private static String initializeBody() {
        return """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18",\
                "capabilities":{},"clientInfo":{"name":"test","version":"0"}}}""";
    }

    private static HttpServletRequest request(String auth, @Nullable String sessionId, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        HttpServletRequest request = mock(HttpServletRequest.class);
        lenient().when(request.getHeader("Authorization")).thenReturn(auth);
        lenient().when(request.getHeader("Accept")).thenReturn("application/json, text/event-stream");
        lenient().when(request.getHeader(HttpHeaders.MCP_SESSION_ID)).thenReturn(sessionId);
        lenient().when(request.getContentLengthLong()).thenReturn((long) bytes.length);
        lenient().when(request.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public int read() {
                return in.read();
            }

            @Override
            public boolean isFinished() {
                return in.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(@Nullable ReadListener listener) {
            }
        });
        lenient().when(request.startAsync()).thenReturn(mock(AsyncContext.class));
        lenient().when(request.getMethod()).thenReturn("POST");
        lenient().when(request.getRequestURI()).thenReturn("/mcp");
        return request;
    }

    private static McpSchema.Tool tool(String name) {
        return McpSchema.Tool.builder().name(name)
                .inputSchema(new McpSchema.JsonSchema("object", Map.of(), List.of(), null, null, null)).build();
    }

    private static CallToolResult text(String text) {
        return CallToolResult.builder().content(List.of(new McpSchema.TextContent(text))).build();
    }
}
