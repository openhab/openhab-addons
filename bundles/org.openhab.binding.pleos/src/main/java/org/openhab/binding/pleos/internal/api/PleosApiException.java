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
package org.openhab.binding.pleos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception for Pleos API API errors.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosApiException extends Exception {

    private static final long serialVersionUID = 1L;
    private final @Nullable String errorCode;
    private final boolean authError;

    public PleosApiException(String message) {
        this(message, null, false);
    }

    public PleosApiException(String message, @Nullable String errorCode, boolean authError) {
        super(message);
        this.errorCode = errorCode;
        this.authError = authError;
    }

    public PleosApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.authError = false;
    }

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public boolean isAuthError() {
        return authError;
    }

    /**
     * Checks whether the VIN is not registered for data sharing (code 4101).
     */
    public boolean isUnregisteredVehicle() {
        return "4101".equals(errorCode);
    }

    /**
     * Check whether there is simply no telematics data available yet for this VIN (code 4045).
     */
    public boolean isNoDataAvailable() {
        return "4045".equals(errorCode);
    }

    /**
     * Checks whether the request failed due to an invalid parameter (code 40001).
     */
    public boolean isInvalidParameter() {
        return "40001".equals(errorCode);
    }
}
