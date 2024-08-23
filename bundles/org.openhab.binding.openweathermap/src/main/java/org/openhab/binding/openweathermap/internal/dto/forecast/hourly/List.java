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
package org.openhab.binding.openweathermap.internal.dto.forecast.hourly;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.dto.base.Clouds;
import org.openhab.binding.openweathermap.internal.dto.base.Precipitation;
import org.openhab.binding.openweathermap.internal.dto.base.Weather;
import org.openhab.binding.openweathermap.internal.dto.base.Wind;
import org.openhab.binding.openweathermap.internal.dto.weather.Main;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>list</code> object of the JSON response of the Hourly forecast API and the 5 day
 * weather forecast API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class List {
    private Integer dt;
    private Main main;
    private java.util.List<Weather> weather;
    private Clouds clouds;
    private Wind wind;
    private @Nullable Precipitation rain;
    private @Nullable Precipitation snow;
    private Sys sys;
    @SerializedName("dt_txt")
    private String dtTxt;

    public Integer getDt() {
        return dt;
    }

    public Main getMain() {
        return main;
    }

    public java.util.List<Weather> getWeather() {
        return weather;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public @Nullable Precipitation getRain() {
        return rain;
    }

    public @Nullable Precipitation getSnow() {
        return snow;
    }

    public Sys getSys() {
        return sys;
    }

    public String getDtTxt() {
        return dtTxt;
    }
}
