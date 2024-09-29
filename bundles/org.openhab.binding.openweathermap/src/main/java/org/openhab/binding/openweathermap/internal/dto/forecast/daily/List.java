/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.openweathermap.internal.dto.forecast.daily;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.dto.base.Weather;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>list</code> object of the JSON response of the Daily Forecast 16 Days API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class List {
    private Integer dt;
    private @Nullable Integer sunrise;
    private @Nullable Integer sunset;
    private Temp temp;
    @SerializedName("feels_like")
    private @Nullable FeelsLikeTemp feelsLikeTemp;
    private Double pressure;
    private Integer humidity;
    private java.util.List<Weather> weather;
    private Double speed;
    private Double deg;
    private @Nullable Double gust;
    private Integer clouds;
    private @Nullable Double rain;
    private @Nullable Double snow;
    private @Nullable Double pop;

    public Integer getDt() {
        return dt;
    }

    public @Nullable Integer getSunrise() {
        return sunrise;
    }

    public @Nullable Integer getSunset() {
        return sunset;
    }

    public Temp getTemp() {
        return temp;
    }

    public @Nullable FeelsLikeTemp getFeelsLike() {
        return feelsLikeTemp;
    }

    public Double getPressure() {
        return pressure;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public java.util.List<Weather> getWeather() {
        return weather;
    }

    public Double getSpeed() {
        return speed;
    }

    public Double getDeg() {
        return deg;
    }

    public @Nullable Double getGust() {
        return gust;
    }

    public Integer getClouds() {
        return clouds;
    }

    public @Nullable Double getRain() {
        return rain;
    }

    public @Nullable Double getSnow() {
        return snow;
    }

    public @Nullable Double getPop() {
        return pop;
    }
}
