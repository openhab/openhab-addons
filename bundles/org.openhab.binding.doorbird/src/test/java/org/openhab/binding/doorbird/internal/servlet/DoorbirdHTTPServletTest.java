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
package org.openhab.binding.doorbird.internal.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link DoorbirdHTTPServlet}.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class DoorbirdHTTPServletTest {
    private @NonNullByDefault({}) DoorbirdHTTPServlet servlet;
    private @NonNullByDefault({}) DoorbellHandler handler;
    private @NonNullByDefault({}) Thing thing;
    private @NonNullByDefault({}) ThingUID thingUID;

    @BeforeEach
    public void setUp() {
        servlet = new DoorbirdHTTPServlet();
        handler = mock(DoorbellHandler.class);
        thing = mock(Thing.class);
        thingUID = new ThingUID("doorbird:d101:doorbell");

        when(handler.getThing()).thenReturn(thing);
        when(thing.getUID()).thenReturn(thingUID);
    }

    @Test
    public void testDoorbellWebhook() throws Exception {
        servlet.registerHandler(thingUID, handler);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        when(req.getPathInfo()).thenReturn("/doorbird:d101:doorbell/doorbell");
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        verify(handler).updateDoorbellChannel(anyLong());
        verify(resp).setStatus(HttpServletResponse.SC_OK);
        assertEquals("OK", sw.toString());
    }

    @Test
    public void testMotionWebhook() throws Exception {
        servlet.registerHandler(thingUID, handler);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        when(req.getPathInfo()).thenReturn("/doorbird:d101:doorbell/motion");
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);

        verify(handler).updateMotionChannel(anyLong());
        verify(resp).setStatus(HttpServletResponse.SC_OK);
        assertEquals("OK", sw.toString());
    }

    @Test
    public void testInvalidPathStructureReturnsNotFound() throws Exception {
        servlet.registerHandler(thingUID, handler);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/doorbell");

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(handler, never()).updateDoorbellChannel(anyLong());
        verify(handler, never()).updateMotionChannel(anyLong());
    }

    @Test
    public void testInvalidEventReturnsBadRequest() throws Exception {
        servlet.registerHandler(thingUID, handler);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn("/doorbird:d101:doorbell/unknown_event");

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid event type");
        verify(handler, never()).updateDoorbellChannel(anyLong());
        verify(handler, never()).updateMotionChannel(anyLong());
    }
}
