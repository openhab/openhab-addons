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
 * Stops the mower if it was started.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class StopCommand implements Command {

    /**
     * Builds the stop URL for this {@link Command}.
     *
     * @param baseURL base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=stop";
    }
}
