/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Signals that a generic communication issue with API gateway has occurred.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherGatewayException extends IOException {

    private static final long serialVersionUID = -3614645621941830547L;

    /**
     * Constructs a {@code SmartherGatewayException} with the specified detail message.
     *
     * @param message
     *            the error message returned from the API gateway
     */
    public SmartherGatewayException(@Nullable String message) {
        super(message);
    }

    /**
     * Constructs a {@code SmartherGatewayException} with the specified detail message and cause.
     *
     * @param message
     *            the error message returned from the API gateway
     * @param cause
     *            the cause (a null value is permitted, and indicates that the cause is nonexistent or unknown)
     */
    public SmartherGatewayException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code SmartherGatewayException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of
     * {@code cause}).
     * This constructor is useful for API gateway exceptions that are little more than wrappers for other throwables.
     *
     * @param cause
     *            the cause (a null value is permitted, and indicates that the cause is nonexistent or unknown)
     */
    public SmartherGatewayException(@Nullable Throwable cause) {
        super(cause);
    }
}
