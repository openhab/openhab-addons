/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal.model;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds status of particular features of the Air Purifier thing
 *
 * @author Michał Boroński - Initial contribution
 * @Nullable
 *
 */

public class PhilipsAirPurifierDataDTO extends PhilipsAirPurifierWritableDataDTO {
    @SerializedName("dtrs")
    @Expose
    private int timerLeft;
    @SerializedName("pm25")
    @Expose
    private int pm25;
    @SerializedName("iaql")
    @Expose
    private int allergenLevel;
    @SerializedName("err")
    @Expose
    private int errorCode;
    @SerializedName("rh")
    @Expose
    private @Nullable Float humidity;
    @SerializedName("temp")
    @Expose
    private @Nullable Float temperature;
    @SerializedName("wl")
    @Expose
    private @Nullable Integer waterLevel;

    public int getTimerLeft() {
        return timerLeft;
    }

    public void setTimerLeft(int timerLeft) {
        this.timerLeft = timerLeft;
    }

    public int getPm25() {
        return pm25;
    }

    public void setPm25(int pm25) {
        this.pm25 = pm25;
    }

    public int getAllergenLevel() {
        return allergenLevel;
    }

    public void setAllergenLevel(int allergenLevel) {
        this.allergenLevel = allergenLevel;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public @Nullable Float getHumidity() {
        return humidity;
    }

    public void setHumidity(@Nullable Float humidity) {
        this.humidity = humidity;
    }

    public @Nullable Float getTemperature() {
        return temperature;
    }

    public void setTemperature(@Nullable Float temperature) {
        this.temperature = temperature;
    }

    public @Nullable Integer getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(@Nullable Integer waterLevel) {
        this.waterLevel = waterLevel;
    }
}
