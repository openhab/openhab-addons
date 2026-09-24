/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model.cmd;

/**
 * Queries weather information from the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class WeatherCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=weather";
    }
}
