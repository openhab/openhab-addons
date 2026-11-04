/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.robonect.internal;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.handler.RobonectHandler;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

/** Receives Robonect push notifications through the OSGi HTTP Whiteboard. */
@NonNullByDefault
@Component(immediate = true, service = { Servlet.class, RobonectPushServlet.class })
@HttpWhiteboardServletName("/robonect")
@HttpWhiteboardServletPattern("/robonect/*")
public class RobonectPushServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final Map<String, RobonectHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Registers a Thing handler for notifications at its Thing UID path.
     *
     * @param thingUID the Thing identifier used in the request path
     * @param handler the handler to notify
     */
    public void registerHandler(ThingUID thingUID, RobonectHandler handler) {
        handlers.put(thingUID.getAsString(), handler);
    }

    /**
     * Removes the handler registered for a Thing.
     *
     * @param thingUID the Thing identifier whose handler is removed
     */
    public void unregisterHandler(ThingUID thingUID) {
        handlers.remove(thingUID.getAsString());
    }

    /**
     * Dispatches a push notification to the handler identified by the request path.
     *
     * @param request the incoming servlet request, or {@code null} if unavailable
     * @param response the servlet response, or {@code null} if unavailable
     * @throws IOException if writing the response fails
     */
    @Override
    protected void doPost(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        if (request == null || response == null || request.getPathInfo() == null) {
            if (response != null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }
        RobonectHandler handler = handlers.get(request.getPathInfo().substring(1));
        if (handler == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        handler.handlePushNotification();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
