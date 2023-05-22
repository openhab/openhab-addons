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
package org.openhab.binding.sensorcommunity.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SensorData} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class SensorData {
    private long id;
    private String timestamp;
    @SerializedName("sampling_rate")
    private int samplingRate;
    @SerializedName("sensordatavalues")
    private List<SensorDataValue> sensorDataValues;
    private Location location;
    private Sensor sensor;

    @Override
    public String toString() {
        return id + timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timestamp = timeStamp;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public List<SensorDataValue> getSensorDataValues() {
        return sensorDataValues;
    }

    public void setSensorDataValues(List<SensorDataValue> sensorDataValues) {
        this.sensorDataValues = sensorDataValues;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }
}
