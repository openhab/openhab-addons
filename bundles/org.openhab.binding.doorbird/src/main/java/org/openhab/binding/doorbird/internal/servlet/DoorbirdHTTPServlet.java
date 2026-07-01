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

import static org.openhab.binding.doorbird.internal.DoorbirdBindingConstants.CHANNEL_DOORBELL;
import static org.openhab.binding.doorbird.internal.DoorbirdBindingConstants.CHANNEL_MOTION;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet to handle incoming webhooks from DoorBird devices or the openHAB Cloud.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
@HttpWhiteboardServletAsyncSupported
@HttpWhiteboardServletName("/doorbird")
@HttpWhiteboardServletPattern("/doorbird/*")
@Component(immediate = true, service = { Servlet.class, DoorbirdHTTPServlet.class })
public class DoorbirdHTTPServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(DoorbirdHTTPServlet.class);

    private final Map<String, DoorbellHandler> handlers = new ConcurrentHashMap<>();

    public void registerHandler(ThingUID thingUID, DoorbellHandler handler) {
        handlers.put(thingUID.getAsString(), handler);
        logger.debug("Registered Doorbird callback handler for {}", thingUID);
    }

    public void unregisterHandler(ThingUID thingUID) {
        handlers.remove(thingUID.getAsString());
        logger.debug("Unregistered Doorbird callback handler for {}", thingUID);
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null || resp == null) {
            return;
        }

        String pathInfo = req.getPathInfo(); // E.g., "/doorbird:doorbird:mydevice/doorbell"
        logger.trace("Doorbird webhook request: pathInfo={}", pathInfo);

        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing target path segments");
            return;
        }

        // pathInfo begins with a leading slash, so substring(1) ignores it.
        // Expected segments: ["<thingUID>", "<event>"]
        String[] segments = pathInfo.substring(1).split("/");

        if (segments.length != 2) {
            logger.warn("Doorbird webhook rejected: Invalid path structure '{}'", pathInfo);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String thingUidParam = segments[0];
        String eventParam = segments[1];

        @Nullable
        DoorbellHandler handler = handlers.get(thingUidParam);
        if (handler == null) {
            logger.warn("No Doorbird handler found for thing UID: '{}'", thingUidParam);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No matching Doorbird device found");
            return;
        }

        if (CHANNEL_DOORBELL.equalsIgnoreCase(eventParam)) {
            logger.debug("Triggering doorbell channel via webhook for thing {}", handler.getThing().getUID());
            handler.updateDoorbellChannel(System.currentTimeMillis() / 1000L);
        } else if (CHANNEL_MOTION.equalsIgnoreCase(eventParam)) {
            logger.debug("Triggering motion channel via webhook for thing {}", handler.getThing().getUID());
            handler.updateMotionChannel(System.currentTimeMillis() / 1000L);
        } else {
            logger.warn("Doorbird webhook rejected: Invalid event type '{}'", eventParam);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid event type");
            return;
        }

        sendOkResponse(resp);
    }

    private void sendOkResponse(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        try (PrintWriter writer = resp.getWriter()) {
            writer.print("OK");
            writer.flush();
        }
    }
}
