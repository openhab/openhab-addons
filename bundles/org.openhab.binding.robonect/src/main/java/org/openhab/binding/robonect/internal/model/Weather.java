/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * @author Nico Brüttner - Initial contribution
 * @author Nico Brüttner - Weather conditions and timestamps
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

    public @Nullable Boolean getWeatherBreak() {
        return weatherBreak;
    }

    public @Nullable Integer getRain() {
        return rain;
    }

    public @Nullable Integer getTemperature() {
        return temperature;
    }

    public @Nullable Integer getHumidity() {
        return humidity;
    }

    public @Nullable Boolean getDay() {
        return day;
    }

    public @Nullable String getCity() {
        return city;
    }

    public @Nullable Integer getRemaining() {
        return remaining;
    }

    public @Nullable String getSunrise() {
        return sunrise;
    }

    public @Nullable String getSunset() {
        return sunset;
    }

    public @Nullable String getIcon() {
        return icon;
    }

    public @Nullable WeatherCondition getCondition() {
        return condition;
    }

    public @Nullable Clock getTimestamp() {
        return timestamp;
    }
}
