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
package org.openhab.binding.orbitbhyve.internal.net;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.openhab.binding.orbitbhyve.internal.handler.OrbitBhyveBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OrbitBhyveSocket} class defines websocket used for connection with
 * the Orbit B-Hyve cloud.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class OrbitBhyveSocket {
    private final Logger logger = LoggerFactory.getLogger(OrbitBhyveSocket.class);
    private OrbitBhyveBridgeHandler handler;

    public OrbitBhyveSocket(OrbitBhyveBridgeHandler handler) {
        this.handler = handler;
    }

    @OnWebSocketOpen
    public void onOpen(Session session) {
        logger.debug("WebSocket connected");
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.trace("Got message: {}", message);
        handler.processStatusResponse(message);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.debug("WebSocket closed: {} {}", statusCode, reason);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.debug("WebSocket error: {}", cause.getMessage());
    }
}
