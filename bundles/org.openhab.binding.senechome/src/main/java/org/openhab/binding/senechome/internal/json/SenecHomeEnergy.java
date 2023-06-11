/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Section is "ENERGY"
 *
 * @author Steven Schwarznau - Initial Contribution
 */
public class SenecHomeEnergy implements Serializable {

    private static final long serialVersionUID = -5491226594672777034L;

    /**
     * House power consumption (W).
     */
    public @SerializedName("GUI_HOUSE_POW") String housePowerConsumption;

    /**
     * Total inverter power (W).
     * Named "energyProduction" on channel/thing-type side.
     */
    public @SerializedName("GUI_INVERTER_POWER") String inverterPowerGeneration;

    /**
     * Battery power in W (+values loading, -values unloading)
     */
    public @SerializedName("GUI_BAT_DATA_POWER") String batteryPower;

    /**
     * Battery current (A).
     */
    public @SerializedName("GUI_BAT_DATA_CURRENT") String batteryCurrent;

    /**
     * Battery voltage (V).
     */
    public @SerializedName("GUI_BAT_DATA_VOLTAGE") String batteryVoltage;

    /**
     * Battery charge rate (%).
     */
    public @SerializedName("GUI_BAT_DATA_FUEL_CHARGE") String batteryFuelCharge;

    /**
     * Encoded system state.
     */
    public @SerializedName("STAT_STATE") String systemState;

    @Override
    public String toString() {
        return "SenecHomeEnergy [housePowerConsumption=" + housePowerConsumption + ", inverterPowerGeneration="
                + inverterPowerGeneration + ", batteryPower=" + batteryPower + ", batteryVoltage=" + batteryVoltage
                + ", batteryCurrent=" + batteryCurrent + ", batteryFuelCharge=" + batteryFuelCharge + ", systemState="
                + systemState + "]";
    }
}
