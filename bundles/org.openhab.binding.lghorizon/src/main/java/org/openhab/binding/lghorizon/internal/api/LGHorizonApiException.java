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
package org.openhab.binding.lghorizon.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thrown for any failure talking to the LG Horizon cloud backend: network errors, unexpected payloads, or the backend
 * rejecting our credentials.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class LGHorizonApiException extends Exception {

    private static final long serialVersionUID = 1L;

    private final boolean authenticationFailure;

    public LGHorizonApiException(String message) {
        this(message, false, null);
    }

    public LGHorizonApiException(String message, boolean authenticationFailure) {
        this(message, authenticationFailure, null);
    }

    public LGHorizonApiException(String message, @Nullable Throwable cause) {
        this(message, false, cause);
    }

    public LGHorizonApiException(String message, boolean authenticationFailure, @Nullable Throwable cause) {
        super(message, cause);
        this.authenticationFailure = authenticationFailure;
    }

    /**
     * @return true if this failure means the stored credentials/refresh token are no longer valid and the thing should
     *         go to an offline/configuration-error state instead of being retried automatically.
     */
    public boolean isAuthenticationFailure() {
        return authenticationFailure;
    }
}
