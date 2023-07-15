/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.asuswrt.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is used for handling errors.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtErrorHandler {
    private String errorMessage = "";
    private String infoMessage = "";

    public AsuswrtErrorHandler() {
    }

    public AsuswrtErrorHandler(Exception ex) {
        raiseError(ex);
    }

    /*
     * Public functions
     */

    /**
     * Raises a new error.
     *
     * @param exception the exception
     */
    public void raiseError(Exception ex) {
        raiseError(ex, "");
    }

    /**
     * Raises a new error.
     *
     * @param exception the exception
     * @param infoMessage optional info message
     */
    public void raiseError(Exception ex, @Nullable String infoMessage) {
        this.errorMessage = AsuswrtUtils.getValueOrDefault(ex.getMessage(), "");
        this.infoMessage = AsuswrtUtils.getValueOrDefault(infoMessage, "");
    }

    /**
     * Raises a new error.
     *
     * @param errorMessage the error message
     * @param infoMessage optional info message
     */
    public void raiseError(String errorMessage, @Nullable String infoMessage) {
        this.errorMessage = errorMessage;
        this.infoMessage = AsuswrtUtils.getValueOrDefault(infoMessage, "");
    }

    /**
     * Resets the error.
     */
    public void reset() {
        errorMessage = "";
        infoMessage = "";
    }

    /*
     * Getters
     */

    /**
     * Get the error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the info message.
     */
    public String getInfoMessage() {
        return infoMessage;
    }
}
