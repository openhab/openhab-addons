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
package org.openhab.binding.jeelink.internal.lacrosse;

import org.openhab.binding.jeelink.internal.Reading;

/**
 * Reading of sensors directly connected to a LGW.
 *
 * @author Volker Bier - Initial contribution
 */
public class LgwReading implements Reading {
    private String sensorId;
    private Float temp;
    private Integer humidity;
    private Integer pressure;

    public LgwReading(int sensorId, Float temp, Integer humidity, Integer pressure) {
        this.sensorId = String.valueOf(sensorId);
        this.temp = temp;
        this.humidity = humidity;
        this.pressure = pressure;
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }

    public Float getTemperature() {
        return temp;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Integer getPressure() {
        return pressure;
    }

    public boolean hasPressure() {
        return pressure != null;
    }

    public boolean hasHumidity() {
        return humidity != null;
    }

    public boolean hasTemperature() {
        return temp != null;
    }

    @Override
    public String toString() {
        return "sensorId=" + sensorId + ": temp=" + temp + (hasHumidity() ? ", hum=" + humidity : "")
                + (hasPressure() ? ", pressure=" + pressure : "");
    }
}
