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
package org.openhab.binding.atv.internal.client.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when an HTTP error occurs.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HttpError extends ProtocolError {

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    /**
     * Creates a new HTTP error.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code that triggered the error
     */
    public HttpError(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code that triggered the error.
     *
     * @return the HTTP status code
     */
    public int statusCode() {
        return statusCode;
    }
}
