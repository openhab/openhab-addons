/* Copyright (c) 2010-2026 Contributors to the openHAB project
 * SPDX-License-Identifier: EPL-2.0 */
package org.openhab.binding.robonect.internal.model.cmd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Queries weather information from the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class WeatherCommand implements Command {
    private String parameter;
    private String value;

    /** Sets a weather-service property. */
    public WeatherCommand withValue(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
        return this;
    }

    @Override
    public String toCommandURL(String baseURL) {
        if (parameter == null || value == null) {
            return baseURL + "?cmd=weather";
        }
        return baseURL + "?cmd=weather&" + parameter + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
