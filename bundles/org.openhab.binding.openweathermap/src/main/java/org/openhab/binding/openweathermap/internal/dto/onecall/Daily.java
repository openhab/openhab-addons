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

import org.openhab.binding.openweathermap.internal.dto.forecast.daily.FeelsLikeTemp;
import org.openhab.binding.openweathermap.internal.dto.forecast.daily.Temp;

import com.google.gson.annotations.SerializedName;

/**
 * Holds the data from the <code>daily</code> object of the JSON response of the One Call APIs.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
public class Daily {
    private int dt;
    private int sunrise;
    private int sunset;
    private int moonrise;
    private int moonset;
    @SerializedName("moon_phase")
    private double moonPhase;
    private Temp temp;
    @SerializedName("feels_like")
    private FeelsLikeTemp feelsLikeTemp;
    private int pressure;
    private int humidity;
    @SerializedName("dew_point")
    private double dewPoint;
    @SerializedName("wind_speed")
    private double windSpeed;
    @SerializedName("wind_deg")
    private int windDeg;
    @SerializedName("wind_gust")
    private double windGust;
    private List<Weather> weather = null;
    private int clouds;
    private double pop;
    private int visibility;
    private double rain;
    private double snow;
    private double uvi;

    public int getDt() {
        return dt;
    }

    public int getSunrise() {
        return sunrise;
    }

    public int getSunset() {
        return sunset;
    }

    public int getMoonrise() {
        return moonrise;
    }

    public int getMoonset() {
        return moonset;
    }

    public double getMoonPhase() {
        return moonPhase;
    }

    public Temp getTemp() {
        return temp;
    }

    public FeelsLikeTemp getFeelsLike() {
        return feelsLikeTemp;
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

    public int getClouds() {
        return clouds;
    }

    public double getPop() {
        return pop;
    }

    public double getRain() {
        return rain;
    }

    public double getUvi() {
        return uvi;
    }

    public int getVisibility() {
        return visibility;
    }

    public double getSnow() {
        return snow;
    }
}
