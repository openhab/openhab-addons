/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Phaser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EventSocket} is responsible for handling socket events, which are
 * sent from the free@home bridge.
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class EventSocket extends WebSocketAdapter {

    private final Logger logger = LoggerFactory.getLogger(EventSocket.class);

    private @Nullable FreeAtHomeBridgeHandler freeAtHomeBridge;

    private Phaser phaser = new Phaser();

    int registeredParties = 1;

    @Override
    public void onWebSocketConnect(@Nullable Session session) {
        super.onWebSocketConnect(session);

        Session localSession = session;

        if (localSession != null) {
            localSession.setIdleTimeout(-1);

            logger.debug("Socket Connected - Timeout {} - phaser [ {} ] - sesson: {}", localSession.getIdleTimeout(),
                    phaser.getArrivedParties(), localSession);
        } else {
            logger.debug("Socket Connected - Timeout (invalid) - latch [ 1 ] - sesson: (invalid)");
        }
    }

    @Override
    public void onWebSocketText(@Nullable String message) {
        super.onWebSocketText(message);

        if (message != null) {
            if (message.toLowerCase(Locale.US).contains("bye")) {
                getSession().close(StatusCode.NORMAL, "Thanks");
                logger.debug("Websocket connection closed: {} ", message);
            } else {
                logger.info("Received websocket text: {} ", message);

                FreeAtHomeBridgeHandler bridge = freeAtHomeBridge;

                if (bridge != null) {
                    bridge.processSocketEvent(message);
                } else {
                    logger.debug("No bridge available to handle the event");
                }
            }
        } else {
            logger.debug("Invalid message string");
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, @Nullable String reason) {
        super.onWebSocketClose(statusCode, reason);

        logger.debug("Socket Closed: [ {} ] {}", statusCode, reason);

        phaser.arrive();
    }

    @Override
    public void onWebSocketError(@Nullable Throwable cause) {
        super.onWebSocketError(cause);

        if (cause != null) {
            logger.debug("Socket Error: {}", cause.getLocalizedMessage());
        } else {
            logger.debug("Socket Error: unknown");
        }

        phaser.arrive();
    }

    public void sendKeepAliveMessage(String message) throws IOException {
        getSession().getRemote().sendString(message);
    }

    public void resetEventSocket() {
        if (phaser.getRegisteredParties() > 0)

            registeredParties = phaser.register();
        logger.debug("Socket phaser reseted - restart phaser to [ {} ]", phaser.getRegisteredParties());
    }

    public boolean isSocketClosed() {
        return phaser.isTerminated();
    }

    public void setBridge(@Nullable FreeAtHomeBridgeHandler bridge) {
        logger.debug("Set bridge to handle the events");

        freeAtHomeBridge = bridge;

        if (freeAtHomeBridge == null) {
            logger.debug("Incorrect bridge for event handling");
        }
    }
}
