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
package org.openhab.binding.solaredge.internal.oauth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeGenericHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests invalidation of pending OAuth callbacks.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeOAuthServletTest {

    @Test
    public void unregisterRejectsPreviouslyIssuedCallback() throws Exception {
        SolarEdgeOAuthServlet servlet = new SolarEdgeOAuthServlet();
        SolarEdgeGenericHandler handler = mock(SolarEdgeGenericHandler.class);
        String url = servlet.register(handler, "client-id");
        String externalId = URI.create(url).getQuery().split("external_id=")[1];
        servlet.unregister(handler);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getParameter("code")).thenReturn("authorization-code");
        when(request.getParameter("site_id")).thenReturn("site-id");
        when(request.getParameter("external_id")).thenReturn(externalId);

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown or expired SolarEdge authorization");
        verify(handler, never()).onOAuthAuthorized("authorization-code", "site-id");
    }
}
