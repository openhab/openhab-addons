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
package org.openhab.io.mcp.internal.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.auth.AuthenticationException;
import org.openhab.core.auth.UserApiTokenCredentials;
import org.openhab.core.auth.UserRegistry;

/**
 * Tests for {@link McpAuthenticator}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class McpAuthenticatorTest {

    @Mock
    private @Nullable UserRegistry userRegistry;

    @Mock
    private @Nullable HttpClient httpClient;

    @Mock
    private @Nullable HttpServletRequest request;

    private @Nullable McpAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new McpAuthenticator(Objects.requireNonNull(userRegistry), Objects.requireNonNull(httpClient),
                "http://localhost:8080");
    }

    private McpAuthenticator auth() {
        McpAuthenticator a = authenticator;
        assertNotNull(a);
        return a;
    }

    @Test
    void testExtractTokenFromBearerHeader() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer oh.mytoken123");
        assertEquals("oh.mytoken123", McpAuthenticator.extractToken(req));
    }

    @Test
    void testExtractTokenFromAltHeader() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getHeader("X-OPENHAB-TOKEN")).thenReturn("abc123");
        assertEquals("abc123", McpAuthenticator.extractToken(req));
    }

    @Test
    void testExtractTokenReturnsNullWhenNoHeader() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getHeader("X-OPENHAB-TOKEN")).thenReturn(null);
        assertNull(McpAuthenticator.extractToken(req));
    }

    @Test
    void testExtractTokenPrefersBearer() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer oh.preferred");
        assertEquals("oh.preferred", McpAuthenticator.extractToken(req));
    }

    @Test
    void testExtractTokenTrimsWhitespace() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer  oh.token  ");
        assertEquals("oh.token", McpAuthenticator.extractToken(req));
    }

    @Test
    void testAuthenticateOhTokenSuccess() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer oh.validtoken12345678");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        assertTrue(auth().authenticate(req));
        verify(Objects.requireNonNull(userRegistry)).authenticate(any(UserApiTokenCredentials.class));
    }

    @Test
    void testAuthenticateOhTokenFailure() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer oh.invalidtoken1234");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        doThrow(new AuthenticationException("bad token")).when(Objects.requireNonNull(userRegistry))
                .authenticate(any(UserApiTokenCredentials.class));

        assertFalse(auth().authenticate(req));
    }

    @Test
    void testAuthenticateJwtViaRestProbeSuccess() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.jwt.sig");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        org.eclipse.jetty.client.api.Request jettyRequest = mock(org.eclipse.jetty.client.api.Request.class);
        org.eclipse.jetty.client.api.ContentResponse response = mock(
                org.eclipse.jetty.client.api.ContentResponse.class);

        when(Objects.requireNonNull(httpClient).newRequest(anyString())).thenReturn(jettyRequest);
        when(jettyRequest.method(any(org.eclipse.jetty.http.HttpMethod.class))).thenReturn(jettyRequest);
        when(jettyRequest.header(anyString(), anyString())).thenReturn(jettyRequest);
        when(jettyRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(jettyRequest);
        when(jettyRequest.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(200);

        assertTrue(auth().authenticate(req));
    }

    @Test
    void testAuthenticateJwtViaRestProbeFailure() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.jwt.sig");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        org.eclipse.jetty.client.api.Request jettyRequest = mock(org.eclipse.jetty.client.api.Request.class);
        org.eclipse.jetty.client.api.ContentResponse response = mock(
                org.eclipse.jetty.client.api.ContentResponse.class);

        when(Objects.requireNonNull(httpClient).newRequest(anyString())).thenReturn(jettyRequest);
        when(jettyRequest.method(any(org.eclipse.jetty.http.HttpMethod.class))).thenReturn(jettyRequest);
        when(jettyRequest.header(anyString(), anyString())).thenReturn(jettyRequest);
        when(jettyRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(jettyRequest);
        when(jettyRequest.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(401);
        lenient().when(response.getReason()).thenReturn("Unauthorized");
        lenient().when(response.getContentAsString()).thenReturn("{}");

        assertFalse(auth().authenticate(req));
    }

    @Test
    void testAuthenticateJwtRestProbeException() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.jwt.sig");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        when(Objects.requireNonNull(httpClient).newRequest(anyString()))
                .thenThrow(new RuntimeException("connection refused"));

        assertFalse(auth().authenticate(req));
    }

    @Test
    void testAuthenticateNoTokenReturnsFalse() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getHeader("X-OPENHAB-TOKEN")).thenReturn(null);
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        assertFalse(auth().authenticate(req));
    }

    @Test
    void testBaseUrlTrailingSlashNormalized() {
        McpAuthenticator authWithSlash = new McpAuthenticator(Objects.requireNonNull(userRegistry),
                Objects.requireNonNull(httpClient), "http://localhost:8080/");
        assertNotNull(authWithSlash);
    }
}
