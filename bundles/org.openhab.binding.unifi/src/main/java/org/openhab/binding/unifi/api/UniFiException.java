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
package org.openhab.binding.unifi.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base exception thrown by the shared UniFi parent binding for errors talking to a UniFi console.
 * <p>
 * Subclasses (e.g. authentication failure, expired session, communication error) are defined in the internal
 * implementation and surface through this type so child bindings only need to handle a single exception class.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Classification of authentication-related failures so callers can decide whether to retry.
     * <ul>
     * <li>{@link #OK} — not an auth error; retry-worthy (network, 5xx, timeout)</li>
     * <li>{@link #REJECTED} — credentials definitively rejected (HTTP 401); user must fix config</li>
     * <li>{@link #THROTTLED} — request forbidden (HTTP 403/429); treat as transient, back off longer</li>
     * </ul>
     */
    public enum AuthState {
        OK,
        REJECTED,
        THROTTLED
    }

    private final AuthState authState;

    public UniFiException(String message) {
        super(message);
        this.authState = AuthState.OK;
    }

    public UniFiException(String message, Throwable cause) {
        super(message, cause);
        this.authState = AuthState.OK;
    }

    public UniFiException(Throwable cause) {
        super(cause);
        this.authState = AuthState.OK;
    }

    public UniFiException(String message, AuthState authState) {
        super(message);
        this.authState = authState;
    }

    public AuthState getAuthState() {
        return authState;
    }
}
