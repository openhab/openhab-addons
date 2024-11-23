/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.types;

/**
 * Reasons why Miniserver may be not reachable
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public enum LxErrorCode {
    /**
     * No error at all
     */
    OK,
    /**
     * User name or password incorrect or user not authorized
     */
    USER_UNAUTHORIZED,
    /**
     * Too many failed login attempts and server's temporary ban of the user
     */
    TOO_MANY_FAILED_LOGIN_ATTEMPTS,
    /**
     * Communication error with the Miniserv
     */
    COMMUNICATION_ERROR,
    /**
     * Timeout of user authentication procedure
     */
    USER_AUTHENTICATION_TIMEOUT,
    /**
     * No activity from Miniserver's client
     */
    WEBSOCKET_IDLE_TIMEOUT,
    /**
     * Internal error, sign of something wrong with the program
     */
    INTERNAL_ERROR,
    /**
     * Error code is missing - reason for failure is unknown
     */
    ERROR_CODE_MISSING;

    /**
     * Converts Miniserver status code to enumerated error value
     *
     * @param code status code received in message response from the Miniserver
     * @return converted error code
     */
    public static LxErrorCode getErrorCode(Integer code) {
        if (code == null) {
            return ERROR_CODE_MISSING;
        }
        switch (code) {
            case 420:
                return USER_AUTHENTICATION_TIMEOUT;
            case 401:
            case 500:
                return USER_UNAUTHORIZED;
            case 4003:
                return TOO_MANY_FAILED_LOGIN_ATTEMPTS;
            case 1001:
                return WEBSOCKET_IDLE_TIMEOUT;
            case 200:
                return OK;
            default:
                return COMMUNICATION_ERROR;
        }
    }
}
