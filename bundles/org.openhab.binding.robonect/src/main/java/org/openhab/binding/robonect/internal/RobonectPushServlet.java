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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives Robonect push notifications through the OSGi HTTP Whiteboard.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { Servlet.class, RobonectPushServlet.class })
@HttpWhiteboardServletName("/robonect")
@HttpWhiteboardServletPattern("/robonect/*")
public class RobonectPushServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(RobonectPushServlet.class);

    private final Map<String, Registration> handlers = new ConcurrentHashMap<>();

    /**
     * Registers a Thing handler for notifications at its Thing ID path.
     *
     * @param thingUID the Thing UID whose final segment is used in the request path
     * @param handler the handler to notify
     * @param user the callback username
     * @param password the callback password
     */
    public void registerHandler(ThingUID thingUID, Runnable handler, String user, String password) {
        String credentials = user + ":" + password;
        byte[] utf8 = credentials.getBytes(StandardCharsets.UTF_8);
        // Older Basic authentication clients can use ISO-8859-1 instead of UTF-8.
        byte[] legacy = StandardCharsets.ISO_8859_1.newEncoder().canEncode(credentials)
                ? credentials.getBytes(StandardCharsets.ISO_8859_1)
                : utf8;
        handlers.put(thingUID.getId(), new Registration(handler, utf8, legacy));
    }

    /**
     * Removes the registration if it still belongs to the supplied handler.
     *
     * @param thingUID the Thing identifier whose handler is removed
     * @param handler the handler to remove
     */
    public void unregisterHandler(ThingUID thingUID, Runnable handler) {
        String key = thingUID.getId();
        Registration registration = handlers.get(key);
        if (registration != null && handler.equals(registration.handler())) {
            handlers.remove(key, registration);
        }
    }

    /**
     * Dispatches a GET push notification to the registered callback.
     *
     * @param request the incoming push request
     * @param response the response to populate
     * @throws IOException if the error response cannot be written
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dispatch(request, response, true);
    }

    /**
     * Dispatches a POST push notification to the registered callback.
     *
     * @param request the incoming push request
     * @param response the response to populate
     * @throws IOException if the error response cannot be written
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dispatch(request, response, true);
    }

    /**
     * Validates a HEAD request without triggering a refresh. The default servlet implementation invokes GET.
     *
     * @param request the incoming probe
     * @param response the response to populate
     * @throws IOException if the error response cannot be written
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        dispatch(request, response, false);
    }

    /**
     * Resolves and authenticates the push registration before optionally notifying its handler.
     *
     * @param request the incoming HTTP request
     * @param response the response for the result of routing and authentication
     * @param notify whether a valid request should invoke the registered handler
     * @throws IOException if the error response cannot be written
     */
    private void dispatch(HttpServletRequest request, HttpServletResponse response, boolean notify) throws IOException {
        @Nullable
        String fullpath = getFullURL(request);
        logger.debug("Received push notification: {} {}", request.getMethod(), fullpath);
        String path = request.getPathInfo();
        request.getPathTranslated();
        if (path == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Registration registration = handlers.get(path.substring(1));
        if (registration == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!registration.authenticates(request.getHeader("Authorization"))) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Robonect Push\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (notify) {
            registration.handler().run();
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Reconstructs the request URL with any supplied query string for the debug log.
     *
     * @param request the incoming request
     * @return the complete URL, including the query string when present
     */
    private String getFullURL(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(
                Optional.ofNullable(request.getRequestURL()).orElseThrow().toString());
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    /**
     * Represents a registered push handler with its associated credentials.
     *
     * @param handler the push handler to be invoked
     * @param utf8 the UTF-8 encoded Basic credentials
     * @param legacy the legacy encoded Basic credentials
     */
    private record Registration(Runnable handler, byte[] utf8, byte[] legacy) {
        /**
         * Compares the supplied Basic credentials with the registered UTF-8 and legacy encodings.
         *
         * @param authorization the Authorization header, or {@code null} if absent
         * @return {@code true} if the header contains matching credentials
         */
        private boolean authenticates(@Nullable String authorization) {
            if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
                return false;
            }
            try {
                byte[] supplied = Base64.getDecoder().decode(authorization.substring(6).stripLeading());
                return MessageDigest.isEqual(utf8, supplied) || MessageDigest.isEqual(legacy, supplied);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
