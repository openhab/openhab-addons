/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Exception for errors during calls of the iCloud API.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class ICloudApiAuthenticationException extends ICloudApiResponseException {

    private static final long serialVersionUID = 1L;

    /**
     * The constructor.
     *
     * @param url URL for which the exception occurred
     * @param statusCode HTTP status code which was reported
     * @param body Body of the response
     */
    public ICloudApiAuthenticationException(String url, int statusCode, String body) {
        super(url, statusCode, body);
    }

    @Override
    public String toString() {
        return "ICloudApiAuthenticationException [statusCode=" + statusCode + ", body=" + body + "]";
    }

    /**
     *
     * @return String representation of the authentication error reason.
     */
    public String getReason() {
        switch (this.statusCode) {
            case 421:
                return "LOGIN_TOKEN_EXPIRED";
            case 409:
                return "2FA_REQUIRED";
            case 450:
                return "FIND_MY_AUTH_REQUIRED";
            case 500:
                return "GENERAL_AUTH_ERROR";
            default:
                return "UNKNOWN_ERROR";
        }
    }

    /**
     * Checks if the given status code represents an authentication error.
     * 
     * @param statusCode HTTP status code
     * @return true if the status code indicates an authentication error, false otherwise.
     */
    public static boolean isAuthError(int statusCode) {
        return statusCode == 409 || statusCode == 421 || statusCode == 450 || statusCode == 500;
    }
}
