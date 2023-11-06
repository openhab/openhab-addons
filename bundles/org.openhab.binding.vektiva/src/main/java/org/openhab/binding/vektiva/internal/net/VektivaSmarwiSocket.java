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
package org.openhab.binding.vektiva.internal.net;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.openhab.binding.vektiva.internal.handler.VektivaSmarwiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VektivaSmarwiSocket} class defines websocket used for connection with
 * the Smarwi thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class VektivaSmarwiSocket extends WebSocketAdapter {
    private final Logger logger = LoggerFactory.getLogger(VektivaSmarwiSocket.class);
    private final VektivaSmarwiHandler handler;

    public VektivaSmarwiSocket(VektivaSmarwiHandler handler) {
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
