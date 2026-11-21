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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.WeatherInfo;
import org.openhab.binding.robonect.internal.model.cmd.WeatherCommand;

/**
 * API operations for weather endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectWeatherApi {
    private final RobonectTransport transport;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectWeatherApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /**
     * Returns weather information calculated by the module.
     *
     * @return the weather information including its success status
     */
    public WeatherInfo getWeatherInfo() {
        return ModelParser.parse(transport.sendCommand(new WeatherCommand()), WeatherInfo.class);
    }

    /**
     * Reads the weather interval from the unofficial web interface.
     *
     * @return the interval in minutes, or {@code null} if absent or invalid
     */
    public @Nullable Integer getWeatherIntervalViaWebInterface() {
        // The weather settings page contains an API key, so its response body must not be logged.
        String html = transport.sendCommand(base -> base + "/weather", transport.webBaseUrl, false);
        Element input = Jsoup.parse(html).selectFirst("input#interval");
        if (input == null) {
            return null;
        }
        try {
            int interval = Integer.parseInt(input.attr("value"));
            return interval > 0 ? interval : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Updates one weather-service property.
     *
     * @param parameter the API parameter name
     * @param value the value to send
     * @return the weather-service response and its success status
     */
    public WeatherInfo updateWeather(String parameter, String value) {
        return ModelParser.parse(transport.sendCommand(new WeatherCommand().withValue(parameter, value)),
                WeatherInfo.class);
    }
}
