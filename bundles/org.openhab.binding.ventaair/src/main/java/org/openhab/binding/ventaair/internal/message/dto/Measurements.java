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
package org.openhab.binding.ventaair.internal.message.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Part of the {@link DeviceInfoMessage} containing the measurements of the device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class Measurements {
    @SerializedName(value = "Temperature")
    private double temperature;

    @SerializedName(value = "Humidity")
    private double humidity;

    @SerializedName(value = "Dust")
    private double dust;

    @SerializedName(value = "WaterLevel")
    private int waterLevel;

    @SerializedName(value = "FanRpm")
    private int fanRpm;

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getDust() {
        return dust;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public int getFanRpm() {
        return fanRpm;
    }
}
