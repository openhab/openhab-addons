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
package org.openhab.binding.jellyfin.internal.util.state;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.types.ServerState;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing server state determination and transitions.
 * Encapsulates the logic for analyzing configuration and determining appropriate server states.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ServerStateManager {

    private final Logger logger = LoggerFactory.getLogger(ServerStateManager.class);

    /**
     * State determination result containing the determined state and any relevant context.
     */
    public record StateAnalysis(ServerState recommendedState, String reason, URI serverUri) {
    }

    /**
     * Analyzes the current configuration and thing properties to determine the appropriate server state.
     *
     * @param currentState The current server state
     * @param configuration The server configuration
     * @param thing The openHAB thing representing the server
     * @return StateAnalysis containing the recommended state and reasoning
     */
    public StateAnalysis analyzeServerState(ServerState currentState, Configuration configuration, Thing thing) {
        // If the current state is DISPOSED, return it immediately
        if (currentState == ServerState.DISPOSED) {
            logger.debug("Server state analysis: DISPOSED state is final");
            return new StateAnalysis(ServerState.DISPOSED, "Server is disposed", null);
        }

        // If configuration has a token, consider the server configured (priority)
        if (configuration != null && configuration.token != null && !configuration.token.isBlank()) {
            logger.debug("Server state analysis: configuration has token, state is CONFIGURED");
            return new StateAnalysis(ServerState.CONFIGURED, "Configuration has token", null);
        }

        // Check if we have a discovered server
        boolean isDiscovered = thing.getProperties().containsKey(Constants.ServerProperties.SERVER_URI);
        if (isDiscovered) {
            String uriString = thing.getProperties().get(Constants.ServerProperties.SERVER_URI);
            try {
                URI serverUri = new URI(uriString);
                logger.debug("Server state analysis: discovered server at {}", serverUri);
                return new StateAnalysis(ServerState.DISCOVERED, "Discovered server", serverUri);
            } catch (Exception e) {
                logger.warn("Invalid server URI in thing properties: {}", uriString);
                return new StateAnalysis(ServerState.ERROR, "Invalid server URI", null);
            }
        }
        // Default: return current state
        return new StateAnalysis(currentState, "No state change", null);
    }
    // ...existing code...
}
