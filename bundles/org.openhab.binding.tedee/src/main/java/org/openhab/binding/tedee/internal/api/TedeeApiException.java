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

package org.openhab.binding.tedee.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an error encountered when communicating with the Tedee API or
 * processing an API response.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeApiException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int status;

    /**
     * Creates an exception with a message and an associated status code.
     *
     * @param message a description of the error
     * @param status the HTTP status code or corresponding status code associated with the error
     */
    public TedeeApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    /**
     * Creates an exception with a message, an associated status code, and the
     * underlying cause.
     *
     * @param message a description of the error
     * @param status the HTTP status code or corresponding status code associated with the error
     * @param cause the underlying cause of the error
     */
    public TedeeApiException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Returns the status code associated with this error.
     *
     * @return the HTTP status code or corresponding status code associated with the error
     */
    public int getStatus() {
        return status;
    }
}
