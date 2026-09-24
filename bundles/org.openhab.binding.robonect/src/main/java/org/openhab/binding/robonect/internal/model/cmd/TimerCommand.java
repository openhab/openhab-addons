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

    public TimerCommand withTimer(int timer) {
        if (timer < 1 || timer > 14)
            throw new IllegalArgumentException("Timer must be from 1 to 14");
        this.timer = timer;
        return this;
    }

    public TimerCommand withValue(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
        return this;
    }

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
