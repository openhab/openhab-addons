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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Queries or updates a Robonect timer.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class TimerCommand implements Command {
    private @Nullable Integer timer;
    private @Nullable String parameter;
    private @Nullable String value;

    /**
     * Selects the timer for this command.
     *
     * @param timer non-negative timer number
     * @return this command
     * @throws IllegalArgumentException if the timer number is negative
     */
    public TimerCommand withTimer(int timer) {
        if (timer < 0)
            throw new IllegalArgumentException("Timer must be greater than 0");
        this.timer = timer;
        return this;
    }

    /**
     * Sets a timer parameter and its value.
     *
     * @param parameter timer parameter name
     * @param value parameter value
     * @return this command
     */
    public TimerCommand withValue(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
        return this;
    }

    /**
     * Builds the timer configuration URL for this {@link Command}.
     *
     * @param baseURL base URL to which the command parameters are appended
     * @return the complete request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        String result = baseURL + "?cmd=timer";
        if (timer != null)
            result += "&timer=" + timer;
        if (parameter != null)
            result += "&" + parameter + "=" + value;
        return result;
    }
}
