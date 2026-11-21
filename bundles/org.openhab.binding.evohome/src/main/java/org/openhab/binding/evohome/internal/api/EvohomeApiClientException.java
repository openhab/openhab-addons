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
package org.openhab.binding.evohome.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for errors from the API Client.
 *
 * @author Jasper van Zuijlen - Initial contribution
 * @author Kovacs Istvan - HTTP failure and retry information
 *
 */
@NonNullByDefault
public class EvohomeApiClientException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int statusCode;
    private final long retryAfterSeconds;

    public EvohomeApiClientException(String message) {
        this(message, 0, 0);
    }

    public EvohomeApiClientException(String message, Throwable cause) {
        super(message, cause);
        statusCode = 0;
        retryAfterSeconds = 0;
    }

    public EvohomeApiClientException(String message, int statusCode, long retryAfterSeconds) {
        super(message);
        this.statusCode = statusCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
