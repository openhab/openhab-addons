/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.growatt.internal.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.growatt.internal.handler.GrowattBridgeHandler;

/**
 * The {@link GrowattHttpServlet} is an HttpServlet to handle data posted by the Grott application.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattHttpServlet extends HttpServlet {

    public static final String PATH = "/growatt";

    private static final String HTML = ""
    // @formatter:off
            + "<html>"
            + "<body>"
            + "<h1 style=\"font-family: Arial\">Growatt Binding Servlet</h1>"
            + "<p>&nbsp;</p>"
            + "<h3 style=\"font-family: Arial\">Status: <span style=\"color: #%s;\">%s</span></h3>"
            + "</body>"
            + "</html>";
    // @formatter:on

    private static final String COLOR_READY = "ff6600";
    private static final String COLOR_ONLINE = "339966";
    private static final String MESSAGE_READY = "Ready";
    private static final String MESSAGE_ONLINE = "Bridge Online";

    private static final long serialVersionUID = 36178542423191036L;

    private final Set<GrowattBridgeHandler> handlers = Collections.synchronizedSet(new HashSet<>());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.TEXT_HTML);
        response.getWriter().write(String.format(HTML, handlers.isEmpty() ? COLOR_READY : COLOR_ONLINE,
                handlers.isEmpty() ? COLOR_READY : MESSAGE_ONLINE));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(handlers.isEmpty() ? MESSAGE_READY : MESSAGE_ONLINE);
        if (request.getContentLength() > 0) {
            String content = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            handlers.forEach(handler -> handler.handleGrottContent(content));
        }
    }

    public void handlerAdd(GrowattBridgeHandler handler) {
        handlers.add(handler);
    }

    public void handlerRemove(GrowattBridgeHandler handler) {
        handlers.remove(handler);
    }
}
