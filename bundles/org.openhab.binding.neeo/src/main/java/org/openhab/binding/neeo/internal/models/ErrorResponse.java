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
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing an error response (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ErrorResponse {

    /** The error */
    @Nullable
    private String error;

    /** The message */
    @Nullable
    private String message;

    /**
     * Gets the error.
     *
     * @return the error
     */
    @Nullable
    public String getError() {
        return error;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    @Nullable
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorResponse [error=" + error + ", message=" + message + "]";
    }
}
