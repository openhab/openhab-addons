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

        fw = firmware;
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

    public @Nullable Double atmp; // The ambient temperature in celsius
    public @Nullable Double atmpCompensated; // The ambient temperature, compensated for sensor inaccuracies
    public @Nullable Long boot; // Number of times sensor has uploaded data since last reboot
    public @Nullable Long bootCount; // Same as boot, in firmwares > v3
    public @Nullable Integer datapoints; // The number of datapoints, present only for aggregated data
    public @Nullable String firmware; // The firmware version running on the device, e.g. "9.2.6", not present for
                                      // averages
    public @Nullable String firmwareVersion; // The firmware version running on the device, e.g. "9.2.6", not present
                                             // for averages
    public @Nullable String fwMode; // Model of sensor from local API
    public @Nullable String ledCo2Threshold1;
    public @Nullable String ledCo2Threshold2;
    public @Nullable String ledCo2ThresholdEnd;
    public @Nullable String ledMode; // co2, pm, off, default
    public @Nullable String locationId;
    public @Nullable String locationName;
    public @Nullable String model; // Model of sensor from cloud API
    public @Nullable Double noxIndex; // The value of the NOx index, sensor model dependent
    public @Nullable Double noxRaw; // Raw data from NOx sensor
    public @Nullable Double pm003Count; // The number of particles with a diameter beyond 0.3 microns in 1 deciliter of
                                        // air
    public @Nullable Double pm005Count;
    public @Nullable Double pm01; // The raw PM 1 value in ug
    public @Nullable Double pm01Count;
    public @Nullable Double pm01Standard;
    public @Nullable Double pm02; // The raw PM 2.5 value in ug
    public @Nullable Double pm02Compensated;
    public @Nullable Double pm02Count;
    public @Nullable Double pm02Standard;
    public @Nullable Double pm10; // The raw PM 10 value in ug
    public @Nullable Double pm10Count;
    public @Nullable Double pm10Standard;
    public @Nullable Double pm50Count;
    public @Nullable Double rco2; // The CO2 value in ppm
    public @Nullable Double rhum; // The relative humidity in percent
    public @Nullable Double rhumCompensated; // The relative humidity in percent, compensated for sensor inaccuracies
    public @Nullable String serialno;
    public @Nullable String timestamp; // Timestamp of the measures in ISO 8601 format with UTC offset, e.g.
                                       // 2022-03-28T12:07:40Z
    public @Nullable Double tvoc; // The TVOC value in ppb, provided in case that the sensor delivers an absolute value
    public @Nullable Double tvocIndex; // The value of the TVOC index, sensor model dependent
    public @Nullable Double tvocRaw; // Raw data from TVOC senosor
    public @Nullable Double wifi; // The wifi signal strength in dBm
}
