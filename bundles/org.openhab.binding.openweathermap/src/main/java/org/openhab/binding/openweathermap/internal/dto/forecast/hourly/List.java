/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Generated Plain Old Java Objects class for {@link List} from JSON.
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

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public java.util.List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(java.util.List<Weather> weather) {
        this.weather = weather;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public @Nullable Precipitation getRain() {
        return rain;
    }

    public void setRain(Precipitation rain) {
        this.rain = rain;
    }

    public @Nullable Precipitation getSnow() {
        return snow;
    }

    public void setSnow(Precipitation snow) {
        this.snow = snow;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public String getDtTxt() {
        return dtTxt;
    }

    public void setDtTxt(String dtTxt) {
        this.dtTxt = dtTxt;
    }
}
