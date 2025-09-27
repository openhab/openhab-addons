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
package org.openhab.binding.metofficedatahub.internal.api;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AuthTokenException} should be thrown when the endpoint being communicated with
 * does not appear to be a Tap Link Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class AuthTokenException extends I18Exception {
    @Serial
    private static final long serialVersionUID = -7786449325604153947L;

    public AuthTokenException() {
    }

    public AuthTokenException(final String message) {
        super(message);
    }

    public AuthTokenException(final Throwable cause) {
        super(cause);
    }

    public AuthTokenException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public String getI18Key() {
        return getI18Key("exception.bad-auth-token");
    }
}
