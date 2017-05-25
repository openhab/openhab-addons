/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather.internal.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openhab.binding.weather.internal.annotation.Provider;
import org.openhab.binding.weather.internal.annotation.ProviderMappings;

/**
 * Common provider model for temperature data.
 *
 * @author Gerhard Riegler
 * @since 1.6.0
 */
public class Temperature {

    @ProviderMappings({
            @Provider(name = ProviderName.Wunderground, property = "current_observation.temp_c"),
            @Provider(name = ProviderName.OpenWeatherMap, property = "main.temp"),
            @Provider(name = ProviderName.OpenWeatherMap, property = "temp.day"),
            @Provider(name = ProviderName.ForecastIO, property = "currently.temperature"),
            @Provider(name = ProviderName.WorldWeatherOnline, property = "temp_C"),
            @Provider(name = ProviderName.Yahoo, property = "condition.temp"),
            @Provider(name = ProviderName.HamWeather, property = "tempC"),
            @Provider(name = ProviderName.MeteoBlue, property = "temperature") })
    private Double current;

    @ProviderMappings({
            @Provider(name = ProviderName.Wunderground, property = "low.celsius"),
            @Provider(name = ProviderName.OpenWeatherMap, property = "main.temp_min"),
            @Provider(name = ProviderName.OpenWeatherMap, property = "temp.min"),
            @Provider(name = ProviderName.ForecastIO, property = "temperatureMin"),
            @Provider(name = ProviderName.WorldWeatherOnline, property = "tempMinC"),
            @Provider(name = ProviderName.Yahoo, property = "forecast.low"),
            @Provider(name = ProviderName.HamWeather, property = "minTempC"),
            @Provider(name = ProviderName.MeteoBlue, property = "temperature_min") })
    private Double min;

    @ProviderMappings({
            @Provider(name = ProviderName.Wunderground, property = "high.celsius"),
            @Provider(name = ProviderName.OpenWeatherMap, property = "main.temp_max"),
            @Provider(name = ProviderName.OpenWeatherMap, property = "temp.max"),
            @Provider(name = ProviderName.ForecastIO, property = "temperatureMax"),
            @Provider(name = ProviderName.WorldWeatherOnline, property = "tempMaxC"),
            @Provider(name = ProviderName.Yahoo, property = "forecast.high"),
            @Provider(name = ProviderName.HamWeather, property = "maxTempC"),
            @Provider(name = ProviderName.MeteoBlue, property = "temperature_max") })
    private Double max;

    @ProviderMappings({
            @Provider(name = ProviderName.Wunderground, property = "current_observation.feelslike_c"),
            @Provider(name = ProviderName.HamWeather, property = "feelslikeC") })
    private Double feel;

    @ProviderMappings({
            @Provider(name = ProviderName.Wunderground, property = "current_observation.dewpoint_c"),
            @Provider(name = ProviderName.ForecastIO, property = "dewPoint"),
            @Provider(name = ProviderName.HamWeather, property = "dewpointC"),
            @Provider(name = ProviderName.WorldWeatherOnline, property = "DewPointC") })
    private Double dewpoint;

    /**
     * Returns the current temperature in degrees.
     */
    public Double getCurrent() {
        return current;
    }

    /**
     * Sets the current temperature in degrees.
     */
    public void setCurrent(Double current) {
        this.current = current;
    }

    /**
     * Returns the min forecast temperature in degrees.
     */
    public Double getMin() {
        return min;
    }

    /**
     * Sets the min forecast temperature in degrees.
     */
    public void setMin(Double min) {
        this.min = min;
    }

    /**
     * Returns the max forecast temperature in degrees.
     */
    public Double getMax() {
        return max;
    }

    /**
     * Sets the min forecast temperature in degrees.
     */
    public void setMax(Double max) {
        this.max = max;
    }

    /**
     * Returns the current feels like temperature in degrees.
     */
    public Double getFeel() {
        return feel;
    }

    /**
     * Sets the current feels like temperature in degrees.
     */
    public void setFeel(Double feel) {
        this.feel = feel;
    }

    /**
     * Returns the dewpoint in degrees.
     */
    public Double getDewpoint() {
        return dewpoint;
    }

    /**
     * Sets the dewpoint in degrees.
     */
    public void setDewpoint(Double dewpoint) {
        this.dewpoint = dewpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("current", current).append("min", min)
                .append("max", max).append("feel", feel).append("dewpoint", dewpoint).toString();
    }

}
