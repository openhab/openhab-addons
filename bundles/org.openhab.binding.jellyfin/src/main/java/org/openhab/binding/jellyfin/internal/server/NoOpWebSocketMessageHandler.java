/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.server;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of WebSocketMessageHandler for Task 2.
 *
 * This implementation simply logs received messages without parsing them.
 * It serves as a placeholder until Task 3 implements full message parsing
 * and event distribution logic.
 *
 * Task 3 will replace this with a full implementation that:
 * - Parses JSON messages using Gson
 * - Discriminates SessionsMessage from other message types
 * - Converts SessionInfoDto to SessionInfoTO
 * - Publishes events to SessionEventBus
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class NoOpWebSocketMessageHandler implements WebSocketMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(NoOpWebSocketMessageHandler.class);

    @Override
    public void handleMessage(String message) {
        // Log message receipt (truncated to avoid overwhelming logs)
        String truncated = message.length() > 200 ? message.substring(0, 200) + "..." : message;
        logger.debug("WebSocket message received (length={}): {}", message.length(), truncated);

        // TODO (Task 3): Parse message, discriminate type, publish to SessionEventBus
    }
}
