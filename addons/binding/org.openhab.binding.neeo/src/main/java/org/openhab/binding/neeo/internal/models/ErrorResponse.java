/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an error response (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class ErrorResponse {

    /** The error */
    private final String error;

    /** The message */
    private final String message;

    /**
     * Instantiates a new error response.
     *
     * @param error the error
     * @param message the message
     */
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public String getError() {
        return error;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorResponse [error=" + error + ", message=" + message + "]";
    }
}
