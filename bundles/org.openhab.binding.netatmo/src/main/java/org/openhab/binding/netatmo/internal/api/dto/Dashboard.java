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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.TrendDescription;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Dashboard} holds data returned by API call supporting the dashboard functionality.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Dashboard {
    private @Nullable ZonedDateTime timeUtc;

    @SerializedName("BoilerOn")
    private int boilerOn;

    @SerializedName("BoilerOff")
    private int boilerOff;

    @SerializedName("Temperature")
    private double temperature;

    private TrendDescription pressureTrend = TrendDescription.UNKNOWN;
    private TrendDescription tempTrend = TrendDescription.UNKNOWN;
    private @Nullable ZonedDateTime dateMaxTemp;
    private @Nullable ZonedDateTime dateMinTemp;
    private double minTemp;
    private double maxTemp;
    @SerializedName("AbsolutePressure")
    private double absolutePressure;

    @SerializedName("CO2")
    private double co2;

    @SerializedName("Humidity")
    private double humidity;

    @SerializedName("Noise")
    private double noise;

    @SerializedName("Pressure")
    private double pressure;

    @SerializedName("Rain")
    private double rain;
    @SerializedName("sum_rain_1")
    private double sumRain1;
    @SerializedName("sum_rain_24")
    private double sumRain24;

    @SerializedName("WindAngle")
    private int windAngle;

    @SerializedName("GustAngle")
    private int gustAngle;

    @SerializedName("WindStrength")
    private int windStrength;

    private int maxWindStr;
    private @Nullable ZonedDateTime dateMaxWindStr;

    @SerializedName("GustStrength")
    private int gustStrength;

    private int healthIdx;

    public @Nullable ZonedDateTime getTimeUtc() {
        return timeUtc;
    }

    public int getBoilerOn() {
        return boilerOn;
    }

    public int getBoilerOff() {
        return boilerOff;
    }

    public double getTemperature() {
        return temperature;
    }

    public TrendDescription getTempTrend() {
        return tempTrend;
    }

    public @Nullable ZonedDateTime getDateMaxTemp() {
        return dateMaxTemp;
    }

    public @Nullable ZonedDateTime getDateMinTemp() {
        return dateMinTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getAbsolutePressure() {
        return absolutePressure;
    }

    public double getCo2() {
        return co2;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getNoise() {
        return noise;
    }

    public double getPressure() {
        return pressure;
    }

    public TrendDescription getPressureTrend() {
        return pressureTrend;
    }

    public double getRain() {
        return rain;
    }

    public double getSumRain1() {
        return sumRain1;
    }

    public double getSumRain24() {
        return sumRain24;
    }

    public double getWindAngle() {
        return windAngle;
    }

    public double getGustAngle() {
        return gustAngle;
    }

    public double getWindStrength() {
        return windStrength;
    }

    public double getMaxWindStr() {
        return maxWindStr;
    }

    public @Nullable ZonedDateTime getDateMaxWindStr() {
        return dateMaxWindStr;
    }

    public double getGustStrength() {
        return gustStrength;
    }

    public int getHealthIdx() {
        return healthIdx;
    }
}
