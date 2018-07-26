/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
