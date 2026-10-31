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
package org.openhab.binding.volvocars.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Indicates that a request to the Volvo Cars authorization server or API failed.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VolvoCarsException extends Exception {
    private static final long serialVersionUID = 1L;

    /** HTTP status code of the failed request, or 0 if no response was received. */
    private final int httpStatus;
    private final boolean authorizationRequired;

    public VolvoCarsException(String message) {
        this(message, 0, false);
    }

    public VolvoCarsException(String message, int httpStatus, boolean authorizationRequired) {
        super(message);
        this.httpStatus = httpStatus;
        this.authorizationRequired = authorizationRequired;
    }

    public VolvoCarsException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
        this.authorizationRequired = false;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * @return true if the user has to (re-)authorize openHAB to access the Volvo account
     */
    public boolean isAuthorizationRequired() {
        return authorizationRequired;
    }
}
