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
package org.openhab.binding.jellyfin.internal.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum representing the possible states of the server handler
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public enum ServerState {
    /**
     * Initial state when the handler is created
     */
    INITIALIZING,

    /**
     * State for a newly discovered server that needs configuration
     */
    DISCOVERED,

    /**
     * State when server URI is known but no authentication token is available
     */
    NEEDS_AUTHENTICATION,

    /**
     * State when server is fully configured with URI and token
     */
    CONFIGURED,

    /**
     * State when server is connected and online
     */
    CONNECTED,

    /**
     * State when server is in error state
     */
    ERROR,

    /**
     * State when the handler has been disposed
     */
    DISPOSED
}
