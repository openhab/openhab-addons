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
package org.openhab.binding.openweathermap.internal.dto.onecall;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>current</code> object of the JSON response of the One Call APIs.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class Current {
    private int dt;
    private int sunrise;
    private int sunset;
    private double temp;
    @SerializedName("feels_like")
    private double feelsLike;
    private int pressure;
    private int humidity;
    @SerializedName("dew_point")
    private double dewPoint;
    private double uvi;
    private int clouds;
    private int visibility;
    @SerializedName("wind_speed")
    private double windSpeed;
    @SerializedName("wind_deg")
    private int windDeg;
    @SerializedName("wind_gust")
    private double windGust;
    private List<Weather> weather = null;
    private Precipitation rain;
    private Precipitation snow;

    public int getDt() {
        return dt;
    }

    public int getSunrise() {
        return sunrise;
    }

    public int getSunset() {
        return sunset;
    }

    public double getTemp() {
        return temp;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public int getPressure() {
        return pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public double getUvi() {
        return uvi;
    }

    public int getClouds() {
        return clouds;
    }

    public int getVisibility() {
        return visibility;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWindDeg() {
        return windDeg;
    }

    public double getWindGust() {
        return windGust;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Precipitation getRain() {
        return rain;
    }

    public Precipitation getSnow() {
        return snow;
    }
}
