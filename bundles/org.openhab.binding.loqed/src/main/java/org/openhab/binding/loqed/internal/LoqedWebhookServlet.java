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
package org.openhab.binding.loqed.internal;

import static org.openhab.binding.loqed.internal.LoqedBindingConstants.WEBHOOK_PATH;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * Receives signed outgoing webhook calls from local LOQED bridges.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { Servlet.class, LoqedWebhookServlet.class }, property = {
        HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=" + WEBHOOK_PATH + "/*",
        HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME + "=loqed-webhook" })
public class LoqedWebhookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MAX_BODY_SIZE = 65536;

    private final Map<String, LoqedLocalBridgeHandler> handlers = new ConcurrentHashMap<>();

    public void addHandler(String routeId, LoqedLocalBridgeHandler handler) {
        handlers.put(routeId, handler);
    }

    public void removeHandler(LoqedLocalBridgeHandler handler) {
        handlers.values().removeIf(value -> value.equals(handler));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String routeId = request.getPathInfo();
        if (routeId == null || routeId.length() < 2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        LoqedLocalBridgeHandler handler = handlers.get(routeId.substring(1));
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (request.getContentLengthLong() > MAX_BODY_SIZE) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        byte[] body = request.getInputStream().readNBytes(MAX_BODY_SIZE + 1);
        if (body.length > MAX_BODY_SIZE) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        @Nullable
        String timestamp = request.getHeader("TIMESTAMP");
        @Nullable
        String hash = request.getHeader("HASH");
        if (timestamp == null || hash == null || !handler.handleWebhook(body, timestamp, hash)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
