/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Signals that a generic OAuth2 authorization issue with API gateway has occurred.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherAuthorizationException extends SmartherGatewayException {

    private static final long serialVersionUID = 2608406239134276285L;

    /**
     * Constructs a {@code SmartherAuthorizationException} with the specified detail message.
     *
     * @param message
     *            the error message returned from the API gateway
     */
    public SmartherAuthorizationException(@Nullable String message) {
        super(message);
    }

    /**
     * Constructs a {@code SmartherAuthorizationException} with the specified detail message and cause.
     *
     * @param message
     *            the error message returned from the API gateway
     * @param cause
     *            the cause (a null value is permitted, and indicates that the cause is nonexistent or unknown)
     */
    public SmartherAuthorizationException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
