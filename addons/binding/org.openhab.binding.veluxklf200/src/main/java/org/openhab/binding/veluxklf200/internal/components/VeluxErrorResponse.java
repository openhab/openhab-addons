/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.components;

/**
 * List of possible error responses that may be returned from the KLF200 unit.
 *
 * @author MFK - Initial Contribution
 */
public enum VeluxErrorResponse {

    /** Undefined error. */
    UNDEFINED(0, "An undefined error has occurred."),

    /** Unknown command sent. */
    UNKNOWN_COMMAND(1, "Unknown Command or command is not accepted at this state."),

    /** The structure of the command sent is incorrect. */
    STRUCTURE_ERROR(2, "Error on the frame structure."),

    /** The unit is busy. */
    BUSY_ERROR(7, "Busy. Try again later."),

    /** Bad systemtable. */
    BAD_SYSTEMTABLE(8, "Bad system table index."),

    /** Not authenticated. */
    NOT_AUTHENTICATED(12, "Not authenticated."),

    /** Unknown error. */
    UNKNOWN(255, "Unknown error.");

    /** The error code. */
    private int errorCode;

    /** The error reason. */
    private String errorReason;

    /**
     * Instantiates a new velux error response.
     *
     * @param code
     *                   the code
     * @param reason
     *                   the reason
     */
    private VeluxErrorResponse(int code, String reason) {
        this.errorCode = code;
        this.errorReason = reason;
    }

    /**
     * Gets the error reason.
     *
     * @return the error reason
     */
    public String getErrorReason() {
        return this.errorReason;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return this.errorCode;
    }

    /**
     * Creates the.
     *
     * @param code
     *                 the code
     * @return the velux error response
     */
    public static VeluxErrorResponse create(byte code) {
        switch (code) {
            case 0:
                return UNDEFINED;
            case 1:
                return UNKNOWN_COMMAND;
            case 2:
                return STRUCTURE_ERROR;
            case 7:
                return BUSY_ERROR;
            case 8:
                return BAD_SYSTEMTABLE;
            case 12:
                return NOT_AUTHENTICATED;
            default:
                return UNKNOWN;
        }
    }
}
