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
package org.openhab.binding.millheat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Wraps failures encountered while talking to the Mill cloud API.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Carry the HTTP status instead of the legacy error code
 */
@NonNullByDefault
public class MillheatCommunicationException extends Exception {
    private static final long serialVersionUID = 2L;

    /** Used when the failure was not an HTTP error response, for example a timeout. */
    public static final int NO_STATUS = 0;

    private final int httpStatus;

    public MillheatCommunicationException(final String message, final Throwable cause) {
        super(message, cause);
        this.httpStatus = NO_STATUS;
    }

    public MillheatCommunicationException(final String message) {
        super(message);
        this.httpStatus = NO_STATUS;
    }

    public MillheatCommunicationException(final int httpStatus, final String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    /**
     * The HTTP status the server responded with, or {@link #NO_STATUS} if the request never
     * produced a response.
     */
    public int getHttpStatus() {
        return httpStatus;
    }

    /** The access token was rejected and should be refreshed. */
    public boolean isUnauthorized() {
        return httpStatus == 401;
    }

    /** The account exceeded its request budget of 2500 requests per hour. */
    public boolean isRateLimited() {
        return httpStatus == 429;
    }
}
