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
package org.openhab.binding.openweathermap.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openweathermap.internal.dto.base.Clouds;
import org.openhab.binding.openweathermap.internal.dto.base.Coord;
import org.openhab.binding.openweathermap.internal.dto.base.Precipitation;
import org.openhab.binding.openweathermap.internal.dto.base.Weather;
import org.openhab.binding.openweathermap.internal.dto.base.Wind;
import org.openhab.binding.openweathermap.internal.dto.weather.Main;
import org.openhab.binding.openweathermap.internal.dto.weather.Sys;

/**
 * Holds the data from the deserialised JSON response of the <a href="https://openweathermap.org/current">Current
 * weather data API</a>
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class OpenWeatherMapJsonWeatherData {
    private Coord coord;
    private List<Weather> weather;
    private String base;
    private Main main;
    private @Nullable Integer visibility;
    private Wind wind;
    private Clouds clouds;
    private @Nullable Precipitation rain;
    private @Nullable Precipitation snow;
    private Integer dt;
    private Sys sys;
    private Integer id;
    private String name;
    private Integer cod;

    public Coord getCoord() {
        return coord;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public @Nullable Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
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

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }
}
