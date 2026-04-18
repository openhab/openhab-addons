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
package org.openhab.binding.unifi.access.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for UniFi Access API errors.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessApiException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Classification of authentication-related failures.
     * <ul>
     * <li>{@link #OK} — not an auth error</li>
     * <li>{@link #REJECTED} — credentials definitively rejected (HTTP 401); user must fix config</li>
     * <li>{@link #THROTTLED} — request forbidden (HTTP 403); usually NVR-side rate limiting, treat as transient</li>
     * </ul>
     */
    public enum AuthState {
        OK,
        REJECTED,
        THROTTLED
    }

    private final AuthState authState;

    public UnifiAccessApiException(String message) {
        super(message);
        this.authState = AuthState.OK;
    }

    public UnifiAccessApiException(String message, @Nullable Throwable cause) {
        super(message, cause);
        this.authState = AuthState.OK;
    }

    public UnifiAccessApiException(String message, AuthState authState) {
        super(message);
        this.authState = authState;
    }

    public AuthState getAuthState() {
        return authState;
    }
}
