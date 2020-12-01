/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.service;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The class represents the results of the requested command and will be serialized back to the webpage
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class CommandResponse {
    /** True if the call was successful, false otherwise */
    private final boolean success;

    /** The optional message if not successful */
    private final @Nullable String message;

    /** The optional message if not successful */
    private final @Nullable String results;

    /**
     * Constructs a successful result with the results
     * 
     * @param results a non-null, non-empty results string
     */
    public CommandResponse(String results) {
        Validate.notEmpty(results, "results cannot be empty");
        this.success = true;
        this.message = null;
        this.results = results;
    }

    /**
     * Constructs a (generally unsuccessful) result with the message
     * 
     * @param success true if success, false otherwise
     * @param message a non-null, non-empty message
     */
    public CommandResponse(boolean success, String message) {
        Validate.notEmpty(message, "message cannot be empty");
        this.success = success;
        this.message = message;
        this.results = null;
    }

    @Override
    public String toString() {
        return "CommandResponse [success=" + success + ", message=" + message + ", results=" + results + "]";
    }
}
