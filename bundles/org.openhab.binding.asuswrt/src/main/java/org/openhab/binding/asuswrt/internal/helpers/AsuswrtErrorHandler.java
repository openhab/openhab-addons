/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Class Handling Errors
 * 
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtErrorHandler {
    private String errorMessage = "";
    private String infoMessage = "";

    /**
     * Constructor
     *
     */
    public AsuswrtErrorHandler() {
    }

    /**
     * Constructor
     * 
     * @param exception Exception
     */
    public AsuswrtErrorHandler(Exception ex) {
        raiseError(ex);
    }

    /***********************************
     *
     * Public Functions
     *
     ************************************/

    /**
     * Raises new error
     * 
     * @param exception Exception
     */
    public void raiseError(Exception ex) {
        raiseError(ex, "");
    }

    /**
     * Raises new error
     * 
     * @param exception Exception
     * @param infoMessage optional info-message
     */
    public void raiseError(Exception ex, @Nullable String infoMessage) {
        this.errorMessage = AsuswrtUtils.getValueOrDefault(ex.getMessage(), "");
        this.infoMessage = AsuswrtUtils.getValueOrDefault(infoMessage, "");
    }

    /**
     * Raises new error
     * 
     * @param errorMessage error message
     * @param infoMessage optional info-message
     */
    public void raiseError(String errorMessage, @Nullable String infoMessage) {
        this.errorMessage = errorMessage;
        this.infoMessage = AsuswrtUtils.getValueOrDefault(infoMessage, "");
    }

    /**
     * Reset Error
     */
    public void reset() {
        this.errorMessage = "";
        this.infoMessage = "";
    }

    /***********************************
     *
     * GETs
     *
     ************************************/

    /**
     * Get Error Message
     * 
     * @return
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Get Error Message
     * 
     * @return
     */
    public String getInfoMessage() {
        return this.infoMessage;
    }
}
