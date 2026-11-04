/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model.cmd;

/**
 * Queries or updates a Robonect timer.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class TimerCommand implements Command {
    private Integer timer;
    private String parameter;
    private String value;

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
