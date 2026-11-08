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

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Sends signed drive values to both mower wheels for a limited time.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault(DefaultLocation.RETURN_TYPE)
public record DirectCommand(int left, int right, int timeout) implements Command {

    /**
     * Creates a direct wheel command with the binding's supported limits.
     *
     * @param left the left wheel value from -1000 to 1000
     * @param right the right wheel value from -1000 to 1000
     * @param timeout the duration from 1 to 5000 milliseconds
     * @throws IllegalArgumentException if any value is outside its supported range
     */
    public DirectCommand {
        if (left < -1000 || left > 1000 || right < -1000 || right > 1000) {
            throw new IllegalArgumentException("Wheel values must be between -1000 and 1000");
        }
        if (timeout < 1 || timeout > 5000) {
            throw new IllegalArgumentException("Timeout must be between 1 and 5000 milliseconds");
        }
    }

    /**
     * Builds the direct wheel command URL.
     *
     * @param baseURL the mower's API base URL
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=direct&left=" + left + "&right=" + right + "&timeout=" + timeout;
    }
}
