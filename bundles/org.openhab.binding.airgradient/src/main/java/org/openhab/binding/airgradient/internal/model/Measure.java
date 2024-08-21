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
package org.openhab.binding.airgradient.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Data model class for a single measurement from AirGradients API.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class Measure {

    /**
     * Returns a location id that is guaranteed to not be null.
     * 
     * @return A non null location id.
     */
    public String getLocationId() {
        String loc = locationId;
        if (loc != null) {
            return loc;
        }

        return "";
    }

    /**
     * Returns a location name that is guaranteed to not be null.
     *
     * @return A non null location name.
     */
    public String getLocationName() {
        String name = locationName;
        return (name != null) ? name : "";
    }

    /**
     * Returns a serial number that is guaranteed to not be null.
     *
     * @return A non null serial number.
     */
    public String getSerialNo() {
        String serial = serialno;
        if (serial != null) {
            return serial;
        }

        return "";
    }

    /**
     * Returns a firmware version that is guaranteed to not be null.
     *
     * @return A non null firmware version.
     */
    public String getFirmwareVersion() {
        String fw = firmwareVersion;
        if (fw != null) {
            return fw;
        }

        return "";
    }

    public @Nullable String getModel() {
        // model from cloud API
        String m = model;
        if (m != null) {
            return m;
        }

        // model from local API
        m = fwMode;
        if (m != null) {
            return m;
        }

        return null;
    }

    public @Nullable Long getBootCount() {
        if (bootCount == null) {
            return boot;
        }

        return bootCount;
    }

    public @Nullable Double getTemperature() {
        if (atmpCompensated == null) {
            return atmp;
        }

        return atmpCompensated;
    }

    public @Nullable Double getHumidity() {
        if (rhumCompensated == null) {
            return rhum;
        }

        return rhumCompensated;
    }

    @Nullable
    public String locationId;

    @Nullable
    public String locationName;

    @Nullable
    public String serialno;

    @Nullable
    public Double pm01; // The raw PM 1 value in ug

    @Nullable
    public Double pm02; // The raw PM 2.5 value in ug

    @Nullable
    public Double pm10; // The raw PM 10 value in ug

    @Nullable
    public Double pm003Count; // The number of particles with a diameter beyond 0.3 microns in 1 deciliter of air

    @Nullable
    public Double atmp; // The ambient temperature in celsius

    @Nullable
    public Double atmpCompensated; // The ambient temperature, compensated for sensor inaccuracies

    @Nullable
    public Double rhum; // The relative humidity in percent

    @Nullable
    public Double rhumCompensated; // The relative humidity in percent, compensated for sensor inaccuracies

    @Nullable
    public Double rco2; // The CO2 value in ppm

    @Nullable
    public Double tvoc; // The TVOC value in ppb, provided in case that the sensor delivers an absolute value

    @Nullable
    public Double tvocIndex; // The value of the TVOC index, sensor model dependent

    @Nullable
    public Double tvocRaw; // Raw data from TVOC senosor

    @Nullable
    public Double noxIndex; // The value of the NOx index, sensor model dependent

    @Nullable
    public Double noxRaw; // Raw data from NOx sensor

    @Nullable
    public Double wifi; // The wifi signal strength in dBm

    @Nullable
    public Integer datapoints; // The number of datapoints, present only for aggregated data

    @Nullable
    public String timestamp; // Timestamp of the measures in ISO 8601 format with UTC offset, e.g. 2022-03-28T12:07:40Z

    @Nullable
    public String firmwareVersion; // The firmware version running on the device, e.g. "9.2.6", not present for averages

    @Nullable
    public String ledMode; // co2, pm, off, default

    @Nullable
    public String ledCo2Threshold1;

    @Nullable
    public String ledCo2Threshold2;

    @Nullable
    public String ledCo2ThresholdEnd;

    @Nullable
    public Long boot; // Number of times sensor has uploaded data since last reboot

    @Nullable
    public Long bootCount; // Same as boot, in firmwares > v3

    @Nullable
    public String fwMode; // Model of sensor from local API

    @Nullable
    public String model; // Model of sensor from cloud API
}
