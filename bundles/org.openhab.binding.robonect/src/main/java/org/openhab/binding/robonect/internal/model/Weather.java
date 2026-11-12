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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Weather measurements returned by the Robonect API.
 *
 * @author Nico Brüttner - Initial contribution: weather telemetry model
 */
@NonNullByDefault
public class Weather {
    @SerializedName("break")
    private @Nullable Boolean weatherBreak;
    private @Nullable Integer rain;
    private @Nullable Integer temperature;
    private @Nullable Integer humidity;
    private @Nullable Boolean day;
    private @Nullable String city;
    private @Nullable Integer remaining;
    private @Nullable String sunrise;
    private @Nullable String sunset;
    private @Nullable String icon;
    private @Nullable WeatherCondition condition;
    private @Nullable Clock timestamp;

    /**
     * Reports whether weather is preventing mowing.
     *
     * @return whether weather is preventing mowing, or {@code null} if not reported
     */
    public @Nullable Boolean getWeatherBreak() {
        return weatherBreak;
    }

    /**
     * Returns the rain intensity in percent.
     *
     * @return the rain intensity in percent, or {@code null} if not reported
     */
    public @Nullable Integer getRain() {
        return rain;
    }

    /**
     * Returns the outdoor temperature in degrees Celsius.
     *
     * @return the outdoor temperature in degrees Celsius, or {@code null} if not reported
     */
    public @Nullable Integer getTemperature() {
        return temperature;
    }

    /**
     * Returns the relative humidity in percent.
     *
     * @return the relative humidity in percent, or {@code null} if not reported
     */
    public @Nullable Integer getHumidity() {
        return humidity;
    }

    /**
     * Reports whether the weather service reports daytime.
     *
     * @return whether the weather service reports daytime, or {@code null} if not reported
     */
    public @Nullable Boolean getDay() {
        return day;
    }

    /**
     * Returns the reported city.
     *
     * @return the reported city, or {@code null} if not reported
     */
    public @Nullable String getCity() {
        return city;
    }

    /**
     * Returns the remaining weather pause in seconds.
     *
     * @return the remaining weather pause in seconds, or {@code null} if not reported
     */
    public @Nullable Integer getRemaining() {
        return remaining;
    }

    /**
     * Returns the sunrise Unix timestamp in seconds.
     *
     * @return the sunrise Unix timestamp in seconds, or {@code null} if not reported
     */
    public @Nullable String getSunrise() {
        return sunrise;
    }

    /**
     * Returns the sunset Unix timestamp in seconds.
     *
     * @return the sunset Unix timestamp in seconds, or {@code null} if not reported
     */
    public @Nullable String getSunset() {
        return sunset;
    }

    /**
     * Returns the weather icon identifier.
     *
     * @return the weather icon identifier, or {@code null} if not reported
     */
    public @Nullable String getIcon() {
        return icon;
    }

    /**
     * Returns the reported weather restrictions.
     *
     * @return the reported weather restrictions, or {@code null} if not reported
     */
    public @Nullable WeatherCondition getCondition() {
        return condition;
    }

    /**
     * Returns the weather update timestamp.
     *
     * @return the weather update timestamp, or {@code null} if not reported
     */
    public @Nullable Clock getTimestamp() {
        return timestamp;
    }
}
