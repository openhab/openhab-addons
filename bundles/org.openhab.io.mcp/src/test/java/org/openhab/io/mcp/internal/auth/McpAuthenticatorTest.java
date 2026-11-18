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
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.auth.Authentication;
import org.openhab.core.auth.AuthenticationException;
import org.openhab.core.auth.Role;
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

    private static final String ADMIN_PROBE_URL = "http://localhost:8080/rest/logging/";

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

    private HttpServletRequest ohTokenRequest() {
        return requestWithBearer("oh.validtoken12345678");
    }

    private HttpServletRequest jwtRequest() {
        return requestWithBearer("eyJhbGciOiJSUzI1NiJ9.jwt.sig");
    }

    private HttpServletRequest requestWithBearer(String token) {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer " + token);
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");
        return req;
    }

    private void stubProbe(int status) throws Exception {
        Request jettyRequest = mock(Request.class);
        ContentResponse response = mock(ContentResponse.class);
        when(Objects.requireNonNull(httpClient).newRequest(ADMIN_PROBE_URL)).thenReturn(jettyRequest);
        when(jettyRequest.method(any(HttpMethod.class))).thenReturn(jettyRequest);
        when(jettyRequest.header(anyString(), anyString())).thenReturn(jettyRequest);
        when(jettyRequest.timeout(anyLong(), any(TimeUnit.class))).thenReturn(jettyRequest);
        when(jettyRequest.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(status);
        lenient().when(response.getContentAsString()).thenReturn("{}");
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
    void testAuthenticateOhTokenAdmin() throws Exception {
        HttpServletRequest req = ohTokenRequest();
        when(Objects.requireNonNull(userRegistry).authenticate(any(UserApiTokenCredentials.class)))
                .thenReturn(new Authentication("alice", Role.ADMIN, Role.USER));

        assertEquals(new McpUser("alice", true), auth().authenticate(req));
        verify(Objects.requireNonNull(userRegistry)).authenticate(any(UserApiTokenCredentials.class));
        verifyNoInteractions(Objects.requireNonNull(httpClient));
    }

    @Test
    void testAuthenticateOhTokenNonAdmin() throws Exception {
        HttpServletRequest req = ohTokenRequest();
        when(Objects.requireNonNull(userRegistry).authenticate(any(UserApiTokenCredentials.class)))
                .thenReturn(new Authentication("bob", Role.USER));

        assertEquals(new McpUser("bob", false), auth().authenticate(req));
    }

    @Test
    void testAuthenticateOhTokenFailure() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer oh.invalidtoken1234");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        doThrow(new AuthenticationException("bad token")).when(Objects.requireNonNull(userRegistry))
                .authenticate(any(UserApiTokenCredentials.class));

        assertNull(auth().authenticate(req));
    }

    @Test
    void testAuthenticateJwtAdmin() throws Exception {
        HttpServletRequest req = jwtRequest();
        stubProbe(200);

        assertEquals(new McpUser("", true), auth().authenticate(req));
    }

    @Test
    void testAuthenticateJwtNonAdmin() throws Exception {
        HttpServletRequest req = jwtRequest();
        stubProbe(403);

        assertEquals(new McpUser("", false), auth().authenticate(req));
    }

    @ParameterizedTest
    @ValueSource(ints = { 401, 404, 500 })
    void testAuthenticateJwtRejectedOnOtherStatus(int status) throws Exception {
        HttpServletRequest req = jwtRequest();
        stubProbe(status);

        assertNull(auth().authenticate(req));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Bearer a b", "Bearer ", "Bearer a\tb" })
    void testTokenWithWhitespaceIsRejectedWithoutProbing(String header) {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn(header);
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        assertNull(auth().authenticate(req));
        verifyNoInteractions(Objects.requireNonNull(httpClient), Objects.requireNonNull(userRegistry));
    }

    @Test
    void testAltHeaderWithWhitespaceIsRejected() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getHeader("X-OPENHAB-TOKEN")).thenReturn("a b");
        assertNull(McpAuthenticator.extractToken(req));
    }

    @Test
    void testAuthenticateJwtRestProbeException() throws Exception {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.jwt.sig");
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        when(Objects.requireNonNull(httpClient).newRequest(anyString()))
                .thenThrow(new RuntimeException("connection refused"));

        assertNull(auth().authenticate(req));
        verify(Objects.requireNonNull(httpClient), times(1)).newRequest(anyString());
    }

    @Test
    void testAuthenticateNoTokenReturnsFalse() {
        HttpServletRequest req = Objects.requireNonNull(request);
        when(req.getHeader("Authorization")).thenReturn(null);
        when(req.getHeader("X-OPENHAB-TOKEN")).thenReturn(null);
        lenient().when(req.getMethod()).thenReturn("POST");
        lenient().when(req.getRequestURI()).thenReturn("/mcp");

        assertNull(auth().authenticate(req));
    }

    @Test
    void testBaseUrlTrailingSlashNormalized() {
        McpAuthenticator authWithSlash = new McpAuthenticator(Objects.requireNonNull(userRegistry),
                Objects.requireNonNull(httpClient), "http://localhost:8080/");
        assertNotNull(authWithSlash);
    }
}
