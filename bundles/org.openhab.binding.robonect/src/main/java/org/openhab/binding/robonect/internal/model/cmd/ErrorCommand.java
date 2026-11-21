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
package org.openhab.binding.robonect.internal.model.cmd;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation of the error command allowing to retrieve the list of errors or resetting the list.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class ErrorCommand implements Command {

    private boolean reset = false;

    /**
     * Selects whether to reset errors or retrieve the error list.
     * 
     * @param reset - {@code true} if errors should be reset, {@code false} if the list of errors should be retrieved.
     * @return this command
     */
    public ErrorCommand withReset(boolean reset) {
        this.reset = reset;
        return this;
    }

    /**
     * Builds the error query or reset URL for this {@link Command}.
     *
     * @param baseURL base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        if (reset) {
            return baseURL + "?cmd=error&reset";
        } else {
            return baseURL + "?cmd=error";
        }
    }
}
