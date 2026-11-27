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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Holds status of particular features of the Air Purifier thing
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Add null handling and code cleanup
 */
@NonNullByDefault
public class PhilipsAirPurifierDataDTO extends PhilipsAirPurifierWritableDataDTO {
    @SerializedName("dtrs")
    @Expose
    private @Nullable Integer timerLeft;
    @SerializedName("pm25")
    @Expose
    private @Nullable Integer pm25;
    @SerializedName("iaql")
    @Expose
    private @Nullable Integer allergenLevel;
    @SerializedName("err")
    @Expose
    private @Nullable Integer errorCode;
    @SerializedName("rh")
    @Expose
    private @Nullable Float humidity;
    @SerializedName("temp")
    @Expose
    private @Nullable Float temperature;
    @SerializedName("wl")
    @Expose
    private @Nullable Integer waterLevel;
    @SerializedName("tvoc")
    @Expose
    private @Nullable Integer tvoc;
    @SerializedName("rssi")
    @Expose
    private @Nullable Integer rssi;

    public @Nullable Integer getTimerLeft() {
        return timerLeft;
    }

    public @Nullable Integer getPm25() {
        return pm25;
    }

    public @Nullable Integer getAllergenLevel() {
        return allergenLevel;
    }

    public @Nullable Integer getErrorCode() {
        return errorCode;
    }

    public @Nullable Float getHumidity() {
        return humidity;
    }

    public @Nullable Float getTemperature() {
        return temperature;
    }

    public @Nullable Integer getWaterLevel() {
        return waterLevel;
    }

    public @Nullable Integer getTvoc() {
        return tvoc;
    }

    public @Nullable Integer getRssi() {
        return rssi;
    }
}
