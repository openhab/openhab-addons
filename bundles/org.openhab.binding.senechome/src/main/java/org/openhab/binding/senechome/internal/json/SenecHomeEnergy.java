/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * Json model of senec home devices: This sub model contains values of current workload, i. e. current consumption and
 * battery charge.
 *
 * @author Steven Schwarznau - Initial Contribution
 */
public class SenecHomeEnergy implements Serializable {

    private static final long serialVersionUID = -6171687327416551070L;

    public @SerializedName("GUI_HOUSE_POW") String homePowerConsumption;
    public @SerializedName("GUI_INVERTER_POWER") String inverterPowerGeneration;
    public @SerializedName("GUI_BAT_DATA_POWER") String batteryPower;
    public @SerializedName("GUI_BAT_DATA_FUEL_CHARGE") String batteryFuelCharge;
    public @SerializedName("STAT_STATE") String batteryState;

    @Override
    public String toString() {
        return "SenecHomeEnergy [homePowerConsumption=" + homePowerConsumption + ", inverterPowerGeneration="
                + inverterPowerGeneration + ", batteryPower=" + batteryPower + ", batteryFuelCharge="
                + batteryFuelCharge + ", batteryState=" + batteryState + "]";
    }
}
