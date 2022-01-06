/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
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
public class OrbitBhyveSocket extends WebSocketAdapter {
    private final Logger logger = LoggerFactory.getLogger(OrbitBhyveSocket.class);
    private OrbitBhyveBridgeHandler handler;

    public OrbitBhyveSocket(OrbitBhyveBridgeHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onWebSocketText(@Nullable String message) {
        super.onWebSocketText(message);
        if (message != null) {
            logger.trace("Got message: {}", message);
            handler.processStatusResponse(message);
        }
    }
}
