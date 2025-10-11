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
package org.openhab.binding.jellyfin.internal.handler.util;

import java.net.URI;
import java.net.URISyntaxException;

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
 * @author Patrik Gfeller - Extracted from ServerHandler for better maintainability
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

        // Check if we have a discovered server
        boolean isDiscovered = thing.getProperties().containsKey(Constants.ServerProperties.SERVER_URI);

        // Check if we have authentication token
        boolean hasToken = (configuration.token != null && !configuration.token.isEmpty());

        try {
            URI serverURI = configuration.getServerURI();

            if (isDiscovered && !hasToken) {
                logger.debug("Server state analysis: Discovered server without token -> DISCOVERED");
                return new StateAnalysis(ServerState.DISCOVERED, "Server discovered but not authenticated", serverURI);
            } else if (serverURI != null && !hasToken) {
                logger.debug("Server state analysis: Configured server without token -> NEEDS_AUTHENTICATION");
                return new StateAnalysis(ServerState.NEEDS_AUTHENTICATION, "Server configured but missing token",
                        serverURI);
            } else if (serverURI != null && hasToken) {
                logger.debug("Server state analysis: Configured server with token -> CONFIGURED");
                return new StateAnalysis(ServerState.CONFIGURED, "Server fully configured with authentication",
                        serverURI);
            }
        } catch (URISyntaxException e) {
            logger.warn("Invalid server URI configuration during state analysis: {}", e.getMessage());
            return new StateAnalysis(ServerState.ERROR, "Invalid server URI configuration: " + e.getMessage(), null);
        }

        logger.debug("Server state analysis: No clear state determined -> INITIALIZING");
        return new StateAnalysis(ServerState.INITIALIZING, "Server configuration incomplete", null);
    }

    /**
     * Determines if a state transition is valid and safe to perform.
     *
     * @param fromState The current state
     * @param toState The target state
     * @return true if the transition is valid, false otherwise
     */
    public boolean isValidStateTransition(ServerState fromState, ServerState toState) {
        // DISPOSED is a final state
        if (fromState == ServerState.DISPOSED) {
            return false;
        }

        // Any state can transition to ERROR or DISPOSED
        if (toState == ServerState.ERROR || toState == ServerState.DISPOSED) {
            return true;
        }

        // Define valid forward transitions
        return switch (fromState) {
            case INITIALIZING -> toState == ServerState.DISCOVERED || toState == ServerState.NEEDS_AUTHENTICATION
                    || toState == ServerState.CONFIGURED;
            case DISCOVERED -> toState == ServerState.NEEDS_AUTHENTICATION || toState == ServerState.CONFIGURED;
            case NEEDS_AUTHENTICATION -> toState == ServerState.CONFIGURED;
            case CONFIGURED -> toState == ServerState.CONNECTED;
            case CONNECTED -> toState == ServerState.CONFIGURED || toState == ServerState.NEEDS_AUTHENTICATION;
            case ERROR -> true; // Error state can transition to any state for recovery
            case DISPOSED -> false; // Already handled above
        };
    }

    /**
     * Gets a human-readable description of the given server state.
     *
     * @param state The server state
     * @return A descriptive string explaining the state
     */
    public String getStateDescription(ServerState state) {
        return switch (state) {
            case INITIALIZING -> "Server is initializing and analyzing configuration";
            case DISCOVERED -> "Server was discovered but requires authentication setup";
            case NEEDS_AUTHENTICATION -> "Server is configured but needs authentication token";
            case CONFIGURED -> "Server is configured and authenticated, attempting connection";
            case CONNECTED -> "Server is connected and operational";
            case ERROR -> "Server encountered an error and requires attention";
            case DISPOSED -> "Server handler has been disposed and is no longer active";
        };
    }
}
