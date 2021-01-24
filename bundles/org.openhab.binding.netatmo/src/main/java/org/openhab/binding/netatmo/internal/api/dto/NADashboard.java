/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.TrendDescription;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class NADashboard {
    private long timeUtc;

    @SerializedName("BoilerOn")
    private int boilerOn;

    @SerializedName("BoilerOff")
    private int boilerOff;

    @SerializedName("Temperature")
    private float temperature;

    private @Nullable TrendDescription pressureTrend;
    private @Nullable TrendDescription tempTrend;
    private int dateMaxTemp;
    private int dateMinTemp;
    private float minTemp;
    private float maxTemp;
    @SerializedName("AbsolutePressure")
    private float absolutePressure;

    @SerializedName("CO2")
    private float co2;

    @SerializedName("Humidity")
    private float humidity;

    @SerializedName("Noise")
    private float noise;

    @SerializedName("Pressure")
    private float pressure;

    @SerializedName("Rain")
    private float rain;
    @SerializedName("sum_rain_1")
    private float sumRain1;
    @SerializedName("sum_rain_24")
    private float sumRain24;

    @SerializedName("WindAngle")
    private int windAngle;

    @SerializedName("GustAngle")
    private int gustAngle;

    @SerializedName("WindStrength")
    private int windStrength;

    private int maxWindStr;
    private int dateMaxWindStr;

    @SerializedName("GustStrength")
    private int gustStrength;

    private int healthIdx;

    public long getTimeUtc() {
        return timeUtc;
    }

    public int getBoilerOn() {
        return boilerOn;
    }

    public int getBoilerOff() {
        return boilerOff;
    }

    public float getTemperature() {
        return temperature;
    }

    public TrendDescription getTempTrend() {
        TrendDescription trend = tempTrend;
        return trend != null ? trend : TrendDescription.UNKNOWN;
    }

    public int getDateMaxTemp() {
        return dateMaxTemp;
    }

    public int getDateMinTemp() {
        return dateMinTemp;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public float getAbsolutePressure() {
        return absolutePressure;
    }

    public float getCo2() {
        return co2;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getNoise() {
        return noise;
    }

    public float getPressure() {
        return pressure;
    }

    public TrendDescription getPressureTrend() {
        TrendDescription trend = pressureTrend;
        return trend != null ? trend : TrendDescription.UNKNOWN;
    }

    public float getRain() {
        return rain;
    }

    public float getSumRain1() {
        return sumRain1;
    }

    public float getSumRain24() {
        return sumRain24;
    }

    public int getWindAngle() {
        return windAngle;
    }

    public int getGustAngle() {
        return gustAngle;
    }

    public int getWindStrength() {
        return windStrength;
    }

    public int getMaxWindStr() {
        return maxWindStr;
    }

    public int getDateMaxWindStr() {
        return dateMaxWindStr;
    }

    public int getGustStrength() {
        return gustStrength;
    }

    public int getHealthIdx() {
        return healthIdx;
    }
}
