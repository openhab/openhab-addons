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
package org.openhab.binding.unifi.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.UpgradeException;

/**
 * Helpers shared by the family WebSocket clients (UniFi Protect private API, UniFi Access notifications).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class UniFiWebSocketUtil {

    private UniFiWebSocketUtil() {
    }

    /**
     * @return {@code true} if the throwable chain represents a WebSocket upgrade rejected with HTTP 401.
     */
    public static boolean isUnauthorizedUpgrade(@Nullable Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof UpgradeException ue && ue.getResponseStatusCode() == HttpStatus.UNAUTHORIZED_401) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
