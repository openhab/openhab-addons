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
package org.openhab.binding.robonect.internal;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.thing.ThingUID;

/**
 * Tests the HTTP methods and registration used for Robonect push notifications.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
class RobonectPushServletTest {
    @ParameterizedTest
    @ValueSource(strings = { "GET", "POST", "HEAD" })
    void shouldRoutePushRequestsToTheCurrentRegistration(String method) throws ServletException, IOException {
        RobonectPushServlet servlet = new RobonectPushServlet();
        ThingUID uid = new ThingUID("robonect:mower:test");
        Runnable oldHandler = mock(Runnable.class);
        Runnable currentHandler = mock(Runnable.class);
        servlet.registerHandler(uid, oldHandler, "mower", "old");
        servlet.registerHandler(uid, currentHandler, "mower", "test:secret");
        servlet.unregisterHandler(uid, oldHandler);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn("/test");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/robonect/test"));
        when(request.getHeader("Authorization")).thenReturn(
                "basic " + Base64.getEncoder().encodeToString("mower:test:secret".getBytes(StandardCharsets.UTF_8)));

        servlet.service(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
        verify(currentHandler, times("HEAD".equals(method) ? 0 : 1)).run();
        verifyNoInteractions(oldHandler);

        servlet.unregisterHandler(uid, currentHandler);
        servlet.service(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @ParameterizedTest
    @CsvSource({ "GET,", "GET, Basic bW93ZXI6d3Jvbmc=", "POST,", "POST, Basic invalid!", "HEAD, Bearer token" })
    void shouldRejectUnauthenticatedRequests(String method, String authorization) throws ServletException, IOException {
        RobonectPushServlet servlet = new RobonectPushServlet();
        ThingUID uid = new ThingUID("robonect:mower:test");
        Runnable handler = mock(Runnable.class);
        servlet.registerHandler(uid, handler, "mower", "test:secret");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getPathInfo()).thenReturn("/test");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/robonect/test"));
        when(request.getHeader("Authorization")).thenReturn(authorization);

        servlet.service(request, response);

        verify(response).setHeader("WWW-Authenticate", "Basic realm=\"Robonect Push\"");
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(handler);
    }
}
