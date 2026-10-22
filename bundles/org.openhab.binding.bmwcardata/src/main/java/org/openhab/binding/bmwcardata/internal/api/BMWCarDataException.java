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
package org.openhab.binding.bmwcardata.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link BMWCarDataException} signals a failed communication with the BMW CarData services.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataException extends Exception {

    private static final long serialVersionUID = 1L;

    /** HTTP status code of the failed request, or 0 if no response was received. */
    private final int httpStatus;

    public BMWCarDataException(String message) {
        this(message, 0, null);
    }

    public BMWCarDataException(String message, @Nullable Throwable cause) {
        this(message, 0, cause);
    }

    public BMWCarDataException(String message, int httpStatus) {
        this(message, httpStatus, null);
    }

    public BMWCarDataException(String message, int httpStatus, @Nullable Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public boolean isRateLimited() {
        return httpStatus == 429;
    }
}
