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

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

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
public class EventSocket extends WebSocketAdapter {

    private final Logger logger = LoggerFactory.getLogger(EventSocket.class);

    private @Nullable FreeAtHomeBridgeHandler freeAtHomeBridge;

    private CountDownLatch closureLatch = null;

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);

        if (closureLatch != null) {
            session.setIdleTimeout(60 * 60 * 1000);

            logger.debug("Socket Connected - Timeout {} - latch [ {} ] - sesson: {}", session.getIdleTimeout(),
                    closureLatch.getCount(), session);
        } else {
            logger.debug("Socket Connected - but latch was not initialized - sesson: {}", session);
        }
    }

    @Override
    @SuppressWarnings("null")
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);

        if (message.toLowerCase(Locale.US).contains("bye")) {
            getSession().close(StatusCode.NORMAL, "Thanks");
            logger.debug("Websocket connection closed: {} ", message);
        } else {
            logger.info("Received websocket text: {} ", message);
            if (freeAtHomeBridge != null) {
                freeAtHomeBridge.processSocketEvent(message);
            } else {
                logger.debug("No brigde available to handle the event");
            }
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);

        logger.debug("Socket Closed: [ {} ] {}", statusCode, reason);

        if (closureLatch != null) {
            closureLatch.countDown();

            logger.debug("Socket Closed - Latch [ {} ]", closureLatch.getCount());
        } else {
            logger.debug("Socket Closed - Latch was not reseted");
        }
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        super.onWebSocketError(cause);

        logger.debug("Socket Error: {}", cause.getLocalizedMessage());

        if (closureLatch != null) {
            closureLatch.countDown();
        } else {
            logger.debug("Socket Error - Latch was not reseted");
        }
    }

    public void awaitEndCommunication() throws InterruptedException {
        if (closureLatch != null) {
            logger.debug("Awaiting ending the communication from remote or error");
            closureLatch.await();
        } else {
            logger.debug("Awaiting called - Latch was not reseted");
        }
    }

    public void resetEventSocket() {
        closureLatch = new CountDownLatch(1);
        logger.debug("Socket latch reseted - restart latch to [ {} ]", closureLatch.getCount());
    }

    public CountDownLatch getLatch() {
        return closureLatch;
    }

    public void setBridge(FreeAtHomeBridgeHandler bridge) {
        logger.debug("Set brigde to handle the events");

        freeAtHomeBridge = bridge;

        if (freeAtHomeBridge == null) {
            logger.debug("Incorrect brigde for event handling");
        }
    }
}
