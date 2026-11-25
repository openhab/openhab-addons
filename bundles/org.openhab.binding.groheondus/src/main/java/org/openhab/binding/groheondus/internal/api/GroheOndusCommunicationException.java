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
package org.openhab.binding.groheondus.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thrown on network failures, timeouts, unexpected HTTP status codes and malformed responses. These are usually
 * transient and the operation may be retried later.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class GroheOndusCommunicationException extends GroheOndusApiException {
    private static final long serialVersionUID = 1L;

    private final int httpStatus;

    public GroheOndusCommunicationException(String message) {
        this(message, 0, null);
    }

    public GroheOndusCommunicationException(String message, @Nullable Throwable cause) {
        this(message, 0, cause);
    }

    public GroheOndusCommunicationException(String message, int httpStatus) {
        this(message, httpStatus, null);
    }

    private GroheOndusCommunicationException(String message, int httpStatus, @Nullable Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    /**
     * @return the HTTP status code of the failed response, or 0 if no response was received
     */
    public int getHttpStatus() {
        return httpStatus;
    }
}
