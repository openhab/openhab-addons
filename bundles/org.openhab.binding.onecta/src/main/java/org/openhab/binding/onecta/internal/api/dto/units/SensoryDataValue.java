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
package org.openhab.binding.onecta.internal.api.dto.units;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

/**
 * @author Alexander Drent - Initial contribution
 */
public class SensoryDataValue {
    @SerializedName("roomTemperature")
    private IconID roomTemperature;
    @SerializedName("roomHumidity")
    private IconID roomHumidity;
    @SerializedName("outdoorTemperature")
    private IconID outdoorTemperature;
    @SerializedName("leavingWaterTemperature")
    private IconID leavingWaterTemperature;
    @SerializedName("tankTemperature")
    private IconID tankTemperature;
    @SerializedName("deltaD")
    private IconID deltaD;
    @SerializedName("fanMotorRotationSpeed")
    private IconID fanMotorRotationSpeed;
    @SerializedName("heatExchangerTemperature")
    private IconID heatExchangerTemperature;
    @SerializedName("suctionTemperature")
    private IconID suctionTemperature;

    public IconID getRoomTemperature() {
        return roomTemperature;
    }

    public IconID getRoomHumidity() {
        return roomHumidity;
    }

    public IconID getOutdoorTemperature() {
        return outdoorTemperature;
    }

    public IconID getLeavingWaterTemperature() {
        return leavingWaterTemperature;
    }

    public IconID getTankTemperature() {
        return tankTemperature;
    }

    public IconID getDeltaD() {
        return deltaD;
    }

    public IconID getFanMotorRotationSpeed() {
        return fanMotorRotationSpeed;
    }

    public IconID getHeatExchangerTemperature() {
        return heatExchangerTemperature;
    }

    public IconID getSuctionTemperature() {
        return suctionTemperature;
    }

    public IconID getSensorData(Enums.SensorData sensorData) {

        if (sensorData.equals(Enums.SensorData.ROOMTEMP)) {
            return this.roomTemperature;
        } else if (sensorData.equals(Enums.SensorData.ROOMHUMINITY)) {
            return this.roomHumidity;
        } else if (sensorData.equals(Enums.SensorData.OUTDOORTEMP)) {
            return this.outdoorTemperature;
        } else if (sensorData.equals(Enums.SensorData.LEAVINGWATERTEMP)) {
            return this.leavingWaterTemperature;
        } else
            return null;
    }
}
