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
package org.openhab.binding.hasslink.internal.api.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hasslink.internal.HassLinkBindingConstants;

/**
 * Utility class for constructing the Home Assistant WebSocket endpoint URI from
 * bridge connection settings (host, port, and security parameters).
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class HomeAssistantWebSocketUriResolver {

    private HomeAssistantWebSocketUriResolver() {
        // utility class
    }

    /**
     * Constructs a Home Assistant WebSocket endpoint URI from a host, port, and security setting.
     *
     * @param host hostname or IP address without a scheme, path, query, or fragment
     * @param port network port (1–65535)
     * @param secure {@code true} to use secure WebSockets ({@code wss://}), {@code false} for unencrypted
     *            ({@code ws://})
     * @return the resolved WebSocket endpoint URI
     * @throws IllegalArgumentException if the host or port configuration is invalid
     */
    public static URI resolveWebSocketUri(String host, int port, boolean secure) {
        if (host.isBlank() || host.contains("://") || host.contains("/") || host.contains("?") || host.contains("#")
                || host.contains("@") || port < 1 || port > 65535) {
            throw new IllegalArgumentException("Host must be a plain hostname or IP address and port must be valid");
        }
        try {
            return new URI(secure ? "wss" : "ws", null, host.trim(), port, HassLinkBindingConstants.WEBSOCKET_PATH,
                    null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid Home Assistant host '" + host + "': " + e.getMessage(), e);
        }
    }
}
