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
package org.openhab.binding.unifiprotect.internal.api.priv.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when a bad request is made (4xx errors excluding auth)
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class BadRequestException extends UniFiProtectException {

    private static final long serialVersionUID = 1L;
    private final int statusCode;

    public BadRequestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public BadRequestException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
