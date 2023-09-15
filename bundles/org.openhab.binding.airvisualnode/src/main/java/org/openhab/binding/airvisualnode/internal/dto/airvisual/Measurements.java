/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.airvisualnode.internal.dto.airvisual;

import org.openhab.binding.airvisualnode.internal.dto.MeasurementsInterface;

import com.google.gson.annotations.SerializedName;

/**
 * Measurements data.
 *
 * @author Victor Antonovich - Initial contribution
 */
public class Measurements implements MeasurementsInterface {

    private int co2Ppm;
    @SerializedName("humidity_RH")
    private int humidityRH;
    @SerializedName("pm25_AQICN")
    private int pm25AQICN;
    @SerializedName("pm25_AQIUS")
    private int pm25AQIUS;
    private float pm25Ugm3;
    @SerializedName("temperature_C")
    private float temperatureC;
    @SerializedName("temperature_F")
    private float temperatureF;
    private int vocPpb;

    public Measurements(int co2Ppm, int humidityRH, int pm25AQICN, int pm25AQIUS, float pm25Ugm3, float temperatureC,
            float temperatureF, int vocPpb) {
        this.co2Ppm = co2Ppm;
        this.humidityRH = humidityRH;
        this.pm25AQICN = pm25AQICN;
        this.pm25AQIUS = pm25AQIUS;
        this.pm25Ugm3 = pm25Ugm3;
        this.temperatureC = temperatureC;
        this.temperatureF = temperatureF;
        this.vocPpb = vocPpb;
    }

    @Override
    public int getCo2Ppm() {
        return co2Ppm;
    }

    public void setCo2Ppm(int co2Ppm) {
        this.co2Ppm = co2Ppm;
    }

    @Override
    public int getHumidityRH() {
        return humidityRH;
    }

    public void setHumidityRH(int humidityRH) {
        this.humidityRH = humidityRH;
    }

    @Override
    public int getPm25AQICN() {
        return pm25AQICN;
    }

    public void setPm25AQICN(int pm25AQICN) {
        this.pm25AQICN = pm25AQICN;
    }

    @Override
    public int getPm25AQIUS() {
        return pm25AQIUS;
    }

    public void setPm25AQIUS(int pm25AQIUS) {
        this.pm25AQIUS = pm25AQIUS;
    }

    @Override
    public float getPm01Ugm3() {
        return 0;
    }

    @Override
    public float getPm10Ugm3() {
        return 0;
    }

    @Override
    public float getPm25Ugm3() {
        return pm25Ugm3;
    }

    public void setPm25Ugm3(float pm25Ugm3) {
        this.pm25Ugm3 = pm25Ugm3;
    }

    @Override
    public float getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(float temperatureC) {
        this.temperatureC = temperatureC;
    }

    @Override
    public float getTemperatureF() {
        return temperatureF;
    }

    public void setTemperatureF(float temperatureF) {
        this.temperatureF = temperatureF;
    }

    @Override
    public int getVocPpb() {
        return vocPpb;
    }

    public void setVocPpb(int vocPpb) {
        this.vocPpb = vocPpb;
    }
}
