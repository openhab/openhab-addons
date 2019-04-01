/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.darksky.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.darksky.internal.handler.DarkSkyWeatherAndForecastHandler;

/**
 * The {@link DarkSkyWeatherAndForecastConfiguration} is the class used to match the
 * {@link DarkSkyWeatherAndForecastHandler}s configuration.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DarkSkyWeatherAndForecastConfiguration {

    private @NonNullByDefault({}) String location;
    private int forecastHours;
    private int forecastDays;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getForecastHours() {
        return forecastHours;
    }

    public void setForecastHours(int forecastHours) {
        this.forecastHours = forecastHours;
    }

    public int getForecastDays() {
        return forecastDays;
    }

    public void setForecastDays(int forecastDays) {
        this.forecastDays = forecastDays;
    }
}
