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

/**
 * Interface for handling WebSocket messages received from Jellyfin server.
 *
 * Implementations are responsible for:
 * - Parsing incoming JSON messages
 * - Discriminating message types (Sessions, Playstate, etc.)
 * - Converting Jellyfin DTOs to internal transfer objects
 * - Publishing events to SessionEventBus
 *
 * This interface provides separation of concerns between connection management
 * (WebSocketTask) and message processing logic (implementations in Task 3).
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public interface WebSocketMessageHandler {

    /**
     * Handles an incoming WebSocket message from the Jellyfin server.
     *
     * The message is a JSON string that must be parsed and processed according
     * to its MessageType discriminator field. Implementations should handle
     * parsing errors gracefully and log appropriate warnings.
     *
     * @param message The raw JSON message string received from WebSocket
     */
    void handleMessage(String message);
}
