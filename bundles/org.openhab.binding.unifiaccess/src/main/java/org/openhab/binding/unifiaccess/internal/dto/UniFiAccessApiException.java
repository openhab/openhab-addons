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
package org.openhab.binding.unifiaccess.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for UniFi Access API errors.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UniFiAccessApiException extends Exception {
    private static final long serialVersionUID = 1L;
    private final boolean authFailure;

    public UniFiAccessApiException(String message) {
        super(message);
        this.authFailure = false;
    }

    public UniFiAccessApiException(String message, @Nullable Throwable cause) {
        super(message, cause);
        this.authFailure = false;
    }

    public UniFiAccessApiException(String message, boolean authFailure) {
        super(message);
        this.authFailure = authFailure;
    }

    /**
     * Returns true if this exception was caused by an authentication or
     * authorization failure (HTTP 401/403).
     */
    public boolean isAuthFailure() {
        return authFailure;
    }
}
