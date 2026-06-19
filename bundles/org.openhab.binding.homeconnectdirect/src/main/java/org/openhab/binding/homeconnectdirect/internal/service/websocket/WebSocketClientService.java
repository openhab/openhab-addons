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
import org.openhab.binding.homeconnectdirect.internal.service.websocket.exception.WebSocketClientServiceException;

/**
 * WebSocket client service interface.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface WebSocketClientService {
    /**
     * Establish the WebSocket connection to the appliance.
     *
     * @throws WebSocketClientServiceException if the connection could not be established
     */
    void connect() throws WebSocketClientServiceException;

    /**
     * Close the connection and release all associated resources.
     */
    void dispose();

    /**
     * Send a message to the appliance.
     *
     * @param message the raw message to send
     */
    void send(String message);
}
