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
package org.openhab.binding.homeconnectdirect.internal.service.websocket;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Handler for incoming WebSocket messages.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface WebSocketHandler {
    /**
     * Called when the WebSocket connection has been established.
     */
    void onWebSocketConnect();

    /**
     * Called when a message has been received from the appliance.
     *
     * @param rawMessage the raw message received
     * @param websocketClientService the client service the message was received on
     */
    void onWebSocketMessage(String rawMessage, WebSocketClientService websocketClientService);

    /**
     * Called when the WebSocket connection has been closed.
     */
    void onWebSocketClose();

    /**
     * Called when an error occurred on the WebSocket connection.
     *
     * @param throwable the error that occurred
     */
    void onWebSocketError(Throwable throwable);
}
